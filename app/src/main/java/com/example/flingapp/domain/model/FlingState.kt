package com.example.flingapp.domain.model

/**
 * Repräsentiert den aktuellen Zustand des Fling-Spiels.
 *
 * Diese unveränderliche Data-Class kapselt alle relevanten Informationen über den aktuellen Spielzustand,
 * die für die Prasentationsschicht benötigt werden.
 *
 * @property highscore  Der aktuelle Highscore (höchste erreichte Geschwindigkeit in willkürlicher Einheit)
 *                      Standardwert: 0f (kein Highscore gesetzt)
 * @property lastAttempt  Die Geschwindigkeit des letzten Fling-Versuchs
 *                        Standardwert: 0f (kein Versuch durchgeführt)
 * @property isGravityActive  Gibt an, ob die Schwerkraftfunktion aktiv ist
 *                            Standardwert: false (Schwerkraft deaktiviert)
 */
data class FlingState(
    val highscore: Float = 0f,
    val lastAttempt: Float = 0f,
    val isGravityActive: Boolean = false,
)
