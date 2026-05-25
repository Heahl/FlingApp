package com.example.flingapp.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Implementierung der Sensor-Datenerfassung.
 *
 * Verarbeitet Beschleunigungsdaten vom LINEAR_ACCELERATION-Sensor, um Fling-Bewegungen zu erkennen
 * und deren Geschwindigkeit zu berechnen.
 *
 * ### Funktion:
 * - Integriert Beschleunigungswerte über die Zeit, um Geschwindigkeit zu berechnen.
 * - Verfolgt die maximale Geschwindigkeit während eine Bewegungssequenz
 * - Erkennt das Ende eine Fling-Bewegung durch Abnahme der Beschleunigung
 * - Filtert Rauschen durch Schwellenwerte und Geschwindigkeitsdämpfung
 *
 * ### Physikalische Grundlagen:
 * $Geschwindigkeit = \int(Beschleunigung) dt$
 * Die Klasse implementiert eine numerische Integration der Beschleunigungswerte, um die momentane
 * Geschwindigkeit zu berechnen (Euler-Integration).
 *
 * @param context Android-Kontext für den Zugriff auf SensorManager
 * @param config Konfigurationsparameter für die Fling-Erkennung
 * @param onFlingDetected Callback, der bei erkannter Fling-Bewegung aufgerufen wird
 */
class FlingSensorDataSource(
    context: Context,
    private val config: Config = Config(),
    private val onFlingDetected: (Float) -> Unit,
) : SensorEventListener {
    /**
     * Konfigurationsklasse für die Fling-Erkennung.
     *
     * Enthält anpassbare Parameter für die Erkennungsgenauigkeit und -empfindlichkeit.
     * Kann bei Bedarf angepasst werden, um das Verhalten an verschiedene Anforderungen anzupassen.
     *
     * @property accelerationThreshold Minimale Beschleunigung, um als Bewegung zu gelten
     * @property speedThreshold Minimale Geschwindigkeit, um als Fling zu gelten
     * @property velocityDecayFactor Faktor für die Geschwindigkeitsdämpfung bei Inaktivität
     * @property speedScalingFactor Faktor zur Skalierung der berechneten Geschwindigkeit
     */
    data class Config(
        val accelerationThreshold: Float = .8f,
        val speedThreshold: Float = 60f,
        val velocityDecayFactor: Float = .5f,
        val speedScalingFactor: Float = 200f,
    )

    /**
     * Fhysikalische Konstanten für die SensorVerarbeitung.
     *
     * Wird als object deklariert, um Speicherplatz zu sparen, da diese Werte statisch sind.
     */
    private object PhysicsConstants {
        const val NANOSECONDS_PER_SECOND = 1_000_000_000f
        const val VELOCITY_RESET_THRESHOLD = 0.2f
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    // velocity components in m/s (integrated from acceleration) - represent the current velocity
    // vector in 3D Space
    private var vx = 0f
    private var vy = 0f
    private var vz = 0f

    // timestamp of last sensor event in nanoseconds
    private var lastTimestamp: Long = 0

    // tracks maximum speed within current movement sequence
    private var maxSpeedInSequence = 0f

    /**
     * Aktiviert die Sensorüberwachung.
     *
     * Registriert den Listener beim SensorManager und setzt den internen Zustand zurück.
     */
    fun start() {
        accelerometer?.let {
            reset()
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Deaktiviert die Sensorüberwachung.
     *
     * Entfernt den Listener und setzt den internen Zustand zurück.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        reset()
    }

    /**
     * Setzt den internen Zustand zurück.
     *
     * Wird nach dem Stoppen der Überwachung oder beim Starten aufgerufen, um verbleibende Geschwindigkeitswerte
     * zu löschen.
     */
    private fun reset() {
        vx = 0f
        vy = 0f
        vz = 0f
        lastTimestamp = 0
    }

    /**
     * Wird bei jedem Sensor-Event aufgerufen.
     *
     * Verarbeitet die Beschleunigungsdaten, berechnet die Geschwindigkeit und erkennt Fling-Bewegungen basierend
     * auf den Konfigurationsparametern.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        // only process valid acceleration events
        if (event == null || event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return

        // initialize timestamp on first event
        if (lastTimestamp == 0L) {
            lastTimestamp = event.timestamp
            return
        }

        // calc time delta in seconds - sensor timestamps are in nanoseconds, so we divide by 1e9
        val dt = (event.timestamp - lastTimestamp) / PhysicsConstants.NANOSECONDS_PER_SECOND
        lastTimestamp = event.timestamp

        // extract acceleration components - with phone lying on a table, screen facing upwards:
        val az = event.values[0] // up / down: positive when moving up
        val ax = event.values[1] // left / right: positive when moving left
        val ay = event.values[2] // forward / backward: positive when moving backward

        // calc magnitude of acceleration vector
        val currentAcceleration = sqrt(ax * ax + ay * ay + az * az)

        // check if acceleration exceeds threshold (movement detected)
        if (currentAcceleration > config.accelerationThreshold) {
            // integrate acceleration to get velocity
            vx += ax * dt
            vy += ay * dt
            vz += az * dt

            // calc speed (magnitude of velocity vector)
            val currentSpeed = sqrt(vx * vx + vy * vy + vz * vz) * config.speedScalingFactor

            // track maximum speed in current movement sequence
            if (currentSpeed > maxSpeedInSequence) {
                maxSpeedInSequence = currentSpeed
            }
        } else {
            // acceleration below threshold - movement likely ended
            if (maxSpeedInSequence > 0) {
                // check if max speed exceeded threshold to qualify as fling
                if (maxSpeedInSequence > config.speedThreshold) {
                    onFlingDetected(maxSpeedInSequence)
                }
                // reset max speed tracker for next potential fling
                maxSpeedInSequence = 0f
            }

            // apply velocity decay (damping) to prevent false positives from residual movement
            vx *= config.velocityDecayFactor
            vy *= config.velocityDecayFactor
            vz *= config.velocityDecayFactor

            // if velocity is very low, reset completely
            if (sqrt(vx * vx + vy * vy + vz * vz) < PhysicsConstants.VELOCITY_RESET_THRESHOLD) {
                reset()
            }
        }
    }

    /**
     * Wird bei Genauigkeitsänderungen des Sensors aufgerufen.
     *
     * Nicht implementiert, da für diese Anwendung nicht benötigt.
     *
     * @param sensor Sensor, dessen Genauigkeit sich geändert hat.
     * @param accuracy Neue Genauigkeitsstufe
     */
    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {
    }
}
