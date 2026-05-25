package com.example.flingapp.domain.usecase

import com.example.flingapp.domain.repository.FlingRepository

/**
 * Use Case zum Deaktivieren der Sensor-Überwachung.
 *
 * @param repository Das zu verwendende FlingRepository
 */
class StopSensingUseCase(
    private val repository: FlingRepository,
) {
    /**
     * Deaktiviert die Sensor-Überwachung für Fling-Gesten.
     *
     * Wichtig: Sollte immer nach [StartSensingUseCase] aufgerufen werden, um eine
     * korrekte Ressourcenverwaltung zu gewährleisten.
     */
    operator fun invoke() = repository.stopSensing()
}
