package com.example.flingapp.domain.usecase

import com.example.flingapp.domain.repository.FlingRepository

/**
 * Use Case zum Aktivieren / Deaktivieren der Schwerkraft-Mechanik
 *
 * ### Was ist die Schwerkraft-Mechanik?
 * Wenn aktiviert, verringert sich der Highscore kontinuierlich über die Zeit bis zu seinem Ende.
 * Dies simuliert einen "Countdown"-Effekt, der den Spieler dazu anregt, schneller einen neuen
 * Highscore zu erreichen, bevor der aktuelle verfällt. Die Schwerkraft kann nur aktiviert werden,
 * wenn ein positiver Highscore vorhanden ist.
 *
 * @param repository Das zu verwendende FlingRepository
 */
class ToggleGravityUseCase(
    private val repository: FlingRepository,
) {
    /**
     * Aktiviert oder deaktiviert die Schwerkraft-Mechanik.
     *
     * Steuert den Zustand der Schwerkraft-Mechanik im Spiel:
     * - Wenn aktiviert: Die Highscore verringert sich kontinuierlich über Zeit
     * - Wenn deaktiviert: Der Highscore-Verfall wird gestoppt
     *
     * ### Edge Cases:
     * - Wenn active = true und highscore <= 0: Setzt isGravityActive auf false und bricht ab
     * - Wenn active = true und bereits aktiv: Keine Aktion (idempotent)
     * - Wenn active = false und bereits inaktiv: Keine Aktion (idempotent)
     *
     * @param active true, um Schwerkraft zu aktivieren, false für Deaktivierung
     */
    operator fun invoke(active: Boolean) = repository.toggleGravity(active)
}
