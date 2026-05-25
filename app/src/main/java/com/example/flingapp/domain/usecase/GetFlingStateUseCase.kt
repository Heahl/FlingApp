package com.example.flingapp.domain.usecase

import com.example.flingapp.domain.model.FlingState
import com.example.flingapp.domain.repository.FlingRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Use Case zum Abrufen des aktuellen Fling-Spielzustands.
 *
 * Stellt eine einheitliche Schnittstelle für den Zugriff auf den reaktiven Spielzustand bereit.
 * Kapselt den Zugriff auf das FlingRepository für die Präsentationsschicht.
 *
 * @param repository Das zu verwendende FlingRepository
 */
class GetFlingStateUseCase(
    private val repository: FlingRepository,
) {
    /**
     * Gibt den aktuellen Spielzustand als reaktiven Stream zurück.
     *
     * @return StateFlow mit dem aktuellen [FlingState]
     */
    operator fun invoke(): StateFlow<FlingState> = repository.flingState
}
