package com.example.flingapp.data.repository

import android.content.Context
import com.example.flingapp.data.notification.FlingNotificationDataSource
import com.example.flingapp.data.sensor.FlingSensorDataSource
import com.example.flingapp.domain.model.FlingState
import com.example.flingapp.domain.repository.FlingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementierung des Fling-Repositories
 *
 * Verwaltet den Zustand des Fling-Spiels, einschließlich:
 * - Sensor-Datenverarbeitung
 * - Highscore-Tracking
 * - Schwerkraft-Effekt auf den Highscore
 * - Benachrichtigungen bei neuen Highscores
 *
 * @param context Android-Kontext für Sensor- und Benachrichtigungsdienste
 * @param scope CoroutineScope für asynchrone Operationen 
 */
class FlingRepositoryImpl(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : FlingRepository {

    /**
     * Interne StateFlow-Referenz für den Fling-Zustand.file
     *
     * Wird verwendet, um den aktuellen Spielzustand (Highscore, letzter Versuch, Schwerkraft) zu speichern.
     * Der Zustand wird durch Sensor-Ergebnisse und den Schwerkraft-Effekt aktualisiert.
     */
    private val _flingState = MutableStateFlow(FlingState())

    /**
     * Öffentlicher StateFlow für den Fling-Zustand
     *
     * Dieser Flow emitted bei jeder Änderung des Spielzustands einen neuen Wert und ermöglicht die
     * reaktive Bindung an die UI-Komponenten. Die UI sollte diesen Flow beobachten, um automatisch
     * auf Zustandsänderungen zu reagieren.
     *
     * @return StateFlow mit dem aktuellen [FlingState]
     */
    override val flingState: StateFlow<FlingState> = _flingState.asStateFlow()

    /**
     * Datenquelle für Sensor-Daten.
     *
     * Verarbeitet Beschleunigungssensordaten und ruft bei erkannten Fling-Bewegungen den Callback
     * aus, der die neue Geschwindigkeit verarbeitet.
     */
    private val sensorDataSource = FlingSensorDataSource(context) { speed ->
        handleNewFling(speed)
    }

    /**
     * Datenquelle für Benachrichtigungen
     *
     * Wird verwendet, um Benachrichtigungen bei neuen Highscores zu senden.
     */
    private val notificationDataSource = FlingNotificationDataSource(context)

    /**
     * Hintergrund-Job für den Schwerkraft-Effekt.
     *
     * Simuliert den natürlichen Verfall des Highscores über Zeit, wenn der Schwerkraft-Effekt 
     * eingeschaltet ist.
     * Wird gestartet / angehalten durch toggleGravity().
     */
    private var gravityJob: Job? = null

    /**
     * Zeitstempel der letzten Schwerkraft-Aktualisierung.
     *
     * Wird verwendet, um den exakten Zeitraum zwischen Aktualisierungen zu berechnen, unabhängig von der
     * Update-Intervall-Zeit.
     */
    private var lastDecayTime: Long = System.currentTimeMillis()

    /**
     * Konstanten für den Schwerkraft-Effekt.
     *
     * GRAVITY_DECAY_RATE: Geschwindigkeit des Highscore-Verfalls pro Sekunde
     * UPDATE_INTERVAL_MS: Frequenz der Schwerkraft-Aktualisierungen
     */
    private companion object {
        const val GRAVITY_DECAY_RATE = 100f
        const val UPDATE_INTERVAL_MS = 500L
    }

    /**
     * Verarbeitet eine neue Fling-Bewegung mit der angegebenen Geschwindigkeit.
     *
     * Aktualisiert den Highscore, wenn die neue Geschwindigkeit höher ist, und sendet bei neuem Highscore 
     * eine Benachrichtigung.
     *
     * @param speed Erkannte Geschwindigkeit bei Fling-Bewegung 
     */
    private fun handleNewFling(speed: Float) {
        val currentState = _flingState.value
        val validSpeed = speed.coerceAtLeast(0f)

        // check if new highscore and send notification if applicable
        val newHighscore = if (validSpeed > currentState.highscore) {
            notificationDataSource.sendHighscoreNotification(validSpeed)
            validSpeed
        } else {
            currentState.highscore
        }

        // update state with new values - .copy() to ensure immutability and trigger flow emissions
        _flingState.value = currentState.copy(
            highscore = newHighscore,
            lastAttempt = validSpeed
        )
    }

    /**
     * Startet die Sensor-Überwachung.file
     * 
     * Aktiviert die Beschleunigungssensoren, um Fling-Bewegungen zu erkennen. 
     */
    override fun startSensing() {
        sensorDataSource.start()
    }

    /**
     * Stoppt die Sensor-Überwachung.
     *
     * Deaktiviert die Beschleunigungssensoren, um Batterie zu sparen.
     */
    override fun stopSensing() {
        sensorDataSource.stop()
    }

    /**
     * Aktiviert oder deaktiviert den Schwerkraft-Effekt.
     *
     * Wenn aktiviert, verringert sich der Highscore kontinuierlich über Zeit.
     * Kann nur aktiviert werden, wenn ein positiver Highscore vorhanden ist.
     *
     * @param active `true`, um Schwerkraft zu aktivieren, `false` für Deaktivierung
     */
    override fun toggleGravity(active: Boolean) {
      // don't enable gravity, if there is no score to decay
        if (active && _flingState.value.highscore <= 0f) {
            _flingState.value = _flingState.value.copy(isGravityActive = false)
            return
        }

        // update state first, then handle gravity job
        _flingState.value = _flingState.value.copy(isGravityActive = active)
        if (active) {
            startGravity()
        } else {
            stopGravity()
        }
    }

    /**
     * Setzt den Highscore zurück und deaktiviert die Schwerkraft.
     *
     * Stellt den Anfangszustand des Spiels wieder her.
     */
    override fun resetHighscore() {
        stopGravity()
        _flingState.value = _flingState.value.copy(
            highscore = 0f,
            isGravityActive = false
        )
    }

    /**
     * Verarbeitet eine Fling-Bewegung mit der angegebenen Geschwindigkeit.
     *
     * Öffentliche API für das manuelle Verarbeiten von Fling-Bewegungen.
     * Wird von der Sensor-Datenquelle aufgerufen.
     *
     * @param speed Erkannte Geschwindigkeit der Fling-Bewegung
     */
    override fun processFling(speed: Float) {
        handleNewFling(speed)
    }

    /**
     * Startet den Schwerkraft-Effekt.
     *
     * Erstellte einen Coroutine-Job, der in regelmäßigen Abständen den Highscore 
     * verringert, bis dieser 0 erreicht oder die Schwerkraft deaktiviert wird.
     */
    private fun startGravity() {
        gravityJob?.cancel()
        lastDecayTime = System.currentTimeMillis()

        // create new gravityJob with time tracking
        gravityJob = scope.launch {
            while (isActive) {
                delay(UPDATE_INTERVAL_MS)
                val now = System.currentTimeMillis()
                val timeDelta = (now - lastDecayTime) / 1_000f
                lastDecayTime = now

                // calc decay based on actual elapsed time for consistent decay rate
                val decayAmount = GRAVITY_DECAY_RATE * timeDelta
                val current = _flingState.value
                val newHighscore = (current.highscore - decayAmount).coerceAtLeast(0f)

                _flingState.value = current.copy(highscore = newHighscore)

                // automatically disable gravity if highscore <= 0
                if (newHighscore <= 0) {
                    _flingState.value = _flingState.value.copy(isGravityActive = false)
                    break
                }
            }
        }
    }

    /**
     * Stoppt den Schwerkraft-Effekt.
     *
     * Bricht den Hintergrund-Job für den Schwerkraft-Effekt ab.
     */
    private fun stopGravity() {
        gravityJob?.cancel()
        gravityJob = null
    }
}
