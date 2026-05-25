package com.example.flingapp.domain.repository

import com.example.flingapp.domain.model.FlingState
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface für den Zugriff auf Fling-Daten und Spielsteuerung.
 *
 * Definiert den Vertrag zwischen Domain-Schicht und den konkreten Implementierungen.
 * Entkoppelt die Geschäftslogik von der konkreten Implementierung (Sensoren, Speicher).
 * Stellt sicher, dass die Präsentationsschicht nur mit abstrakten Schnittstellen interagiert
 * und keine Kenntnis von plattformspezifischen Implementierungen hat.
 */
interface FlingRepository {
    /**
     * Liefert den aktuellen Spielzustand als reaktiven Stream.
     *
     * Der StateFlow emitted bei jeder Änderung des Spielzustands einen neuen Wert des
     * [FlingState]. Der Zustand enthält:
     * - Aktuellen Highscore
     * - Geschwindigkeit des letzten Versuchs
     * - Aktivitätsstatus der Schwerkraft
     *
     * @return StateFlow mit dem aktuellen [FlingState]
     */
    val flingState: StateFlow<FlingState>

    /**
     * Startet die Erfassung von Fling-Gesten über Sensoren.
     */
    fun startSensing()

    /**
     * Stoppt die Erfassung von Fling-Gesten.
     */
    fun stopSensing()

    /**
     * Aktiviert oder deaktiviert die Gravitationsmechanik.
     *
     * @param active `true`, um Schwerkraft zu aktivieren, `false` für Deaktivierung
     */
    fun toggleGravity(active: Boolean)

    /**
     * Setzt den Highscore zurück und deaktiviert die Schwerkraft.
     */
    fun resetHighscore()

    /**
     * Verarbeitet eine neue Geschwindigkeit (für Sensoren oder Tests).
     *
     * @param speed Erkannte Geschwindigkeit der Fling-Bewegung
     */
    fun processFling(speed: Float)
}
