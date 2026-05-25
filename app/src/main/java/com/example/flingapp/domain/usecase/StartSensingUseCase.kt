package com.example.flingapp.domain.usecase

import com.example.flingapp.domain.repository.FlingRepository

/**
 * Use Case zum aktivieren der Sensor-Überwachung.
 *
 * @param repository Das zu verwendende FlingRepository
 */
class StartSensingUseCase(
    private val repository: FlingRepository,
) {
    /**
     * Aktiviert die Sensor-Überwachung für Fling-Gesten.
     *
     * Wichtig: Muss immer zusammen [StopSensingUseCase] aufgerufen werden,
     * um Ressourcen freizugeben, wenn die UI nicht sichtbar ist.
     */
    operator fun invoke() = repository.startSensing()
}
