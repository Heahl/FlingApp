package com.example.flingapp.domain.usecase

import com.example.flingapp.domain.repository.FlingRepository

/**
 * Use Case zum Zurücksetzen des Highscores.
 *
 * Stellt eine einheitliche Schnittstelle für das Zurücksetzen des Spielstands bereit.
 *
 * @param repository Das zu verwendende FlingRepository
 */
class ResetHighscoreUseCase(
    private val repository: FlingRepository,
) {
    /**
     * Setzt den Highscore zurück und deaktiviert die Schwerkraft.param
     *
     * Stellt den Anfangszustand des Spiels wieder her:
     * - Highscore wird auf 0 gesetzt
     * - Schwerkraft wird deaktiviert
     * - Interner Zustand wird bereinigt
     */
    operator fun invoke() = repository.resetHighscore()
}
