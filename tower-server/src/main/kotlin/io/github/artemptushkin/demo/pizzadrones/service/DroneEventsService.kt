package io.github.artemptushkin.demo.pizzadrones.service

import io.github.artemptushkin.demo.pizzadrones.domain.DroneEvent
import io.github.artemptushkin.demo.pizzadrones.repository.DroneEventsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service

@Service
class DroneEventsService(private val droneEventsRepository: DroneEventsRepository, private val outputReadOnlyFlow: SharedFlow<DroneEvent>) {

    fun stream(): Flow<String> = outputReadOnlyFlow
        .map { it.droneId.toString() }
        .onStart { emitAll(droneEventsRepository.findAll()) }

    suspend fun save(droneEvent: DroneEvent) {
        droneEventsRepository.save(droneEvent)
    }

    suspend fun streamDrone(droneId: Long): Flow<String> {
        return droneEventsRepository
            .get(droneId)
            .shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily)
    }
}