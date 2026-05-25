package com.example.flingapp.domain.usecase

import com.example.flingapp.domain.repository.FlingRepository

/**
 * Use Case zum Verarbeiten einer neuen Fling-Geste.
 *
 * @param repository Das zu verwendende FlingRepository
 */
class ProcessFlingUseCase(
    private val repository: FlingRepository,
) {
    /**
     * Verarbeitet eine Fling-Bewegung mit der angegebenen Geschwindigkeit.
     *
     * Nimmt die erkannte Geschwindigkeit entgegen und delegiert die Verarbeitung
     * and das Repository.
     *
     * ### Ablauf:
     * 1. Validiert die Geschwindigkeit (wird im Repository durchgeführt)
     * 2. Aktualisiert den Highscore bei Bedarf
     * 3. Sendet Benachrichtigung bei neuem Highscore
     * 4. Speichert die Geschwindigkeit als letzten Versuch
     *
     * @param speed Erkannte Geschwindigkeit der Fling-Bewegung
     */
    operator fun invoke(speed: Float) = repository.processFling(speed)
}
