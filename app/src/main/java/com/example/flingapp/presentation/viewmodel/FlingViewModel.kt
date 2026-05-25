package com.example.flingapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flingapp.domain.model.FlingState
import com.example.flingapp.domain.usecase.GetFlingStateUseCase
import com.example.flingapp.domain.usecase.ProcessFlingUseCase
import com.example.flingapp.domain.usecase.ResetHighscoreUseCase
import com.example.flingapp.domain.usecase.StartSensingUseCase
import com.example.flingapp.domain.usecase.StopSensingUseCase
import com.example.flingapp.domain.usecase.ToggleGravityUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel für den FlingScreen.
 *
 * Verwaltet den Zustand des FlingApp-UIs und koordiniert die Ineraktion mit:
 * - Domain-Use-Cases für Spiel-Operationen
 * - Reaktive Überwachung des Spielzustands
 * - Sensor-Steuerung
 *
 * @param getFlingStateUseCase Use Case für den Zugriff auf den Spielzustand
 * @param toggleGravityUseCase Use Case zum Steuern der Schwerkraft
 * @param resetHighscoreUseCase Use Case zum Zurücksetzen der Highscores
 * @param startSensingUseCase Use Case zum Aktivieren der Sensorüberwachung
 * @param stopSensingUseCase Use Case zum Deaktivieren der Sensorüberwachung
 * @param processFlingUseCase Use Case zur Verarbeitung von Fling-Bewegungen
 */
class FlingViewModel(
    private val getFlingStateUseCase: GetFlingStateUseCase,
    private val toggleGravityUseCase: ToggleGravityUseCase,
    private val resetHighscoreUseCase: ResetHighscoreUseCase,
    private val startSensingUseCase: StartSensingUseCase,
    private val stopSensingUseCase: StopSensingUseCase,
    private val processFlingUseCase: ProcessFlingUseCase,
) : ViewModel() {
    /**
     * Beobachtbarer UI-Zustand als StateFlow.
     */
    val uiState: StateFlow<FlingState> =
        getFlingStateUseCase()
            .stateIn(
                scope = viewModelScope,
                // WhileSubscribed(5000) stops collecting when UI is not visible and restarts when UI
                // becomes visible again after 5s delay
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FlingState(),
            )

    /**
     * Aktiviert die Sensorüberwachung für Fling-Gesten.
     */
    fun startSensing() = startSensingUseCase()

    /**
     * Deaktiviert die Sensorüberwachung für Fling-Gesten.
     */
    fun stopSensing() = stopSensingUseCase()

    /**
     * Aktiviert oder deaktiviert die Schwerkraft.
     *
     * @param active `true`, um Schwerkraft zu aktivieren, `false` für Deaktivierung
     */
    fun toggleGravity(active: Boolean) = toggleGravityUseCase(active)

    /**
     * Setzt den Highscore zurück und deaktiviert die Schwerkraft.
     */
    fun resetHighscore() = resetHighscoreUseCase()

    /**
     * Verarbeitet einen Fling-Versuch mit der angegebenen Geschwindigkeit.
     *
     * @param speed Erkannte Geschwindigkeit der Fling-Bewegung
     * @param onHighscoreBroken Optionaler Callback, der bei neuem Highscore aufgerufen wird.
     */
    fun updateAttempt(
        speed: Float,
        onHighscoreBroken: (Float) -> Unit = {},
    ) {
        processFlingUseCase(speed)
        // Note: The callback functionality is now partially handled by the Repository/Notification
        // but I keep the signature for compatibility with existing tests if possible.
        // The flying nofitfication mechanism is now through the FlingNotificationDataSource
        // which is triggered internally by the repository when a new highscore is detected
    }
}

