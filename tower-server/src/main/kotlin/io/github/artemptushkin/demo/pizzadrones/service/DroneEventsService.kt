package io.github.artemptushkin.demo.pizzadrones.service

import io.github.artemptushkin.demo.pizzadrones.domain.DroneMessage
import io.github.artemptushkin.demo.pizzadrones.domain.toEvent
import io.github.artemptushkin.demo.pizzadrones.domain.toMessage
import io.github.artemptushkin.demo.pizzadrones.repository.DroneEventsRepository
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service

@Service
class DroneEventsService(private val droneEventsRepository: DroneEventsRepository, private val outputReadOnlyFlow: SharedFlow<DroneMessage>) {

    fun stream(): Flow<DroneMessage> = outputReadOnlyFlow
        .onStart { emitAll(droneEventsRepository.findAll().map { it.toMessage() }) }

    suspend fun save(droneMessage: DroneMessage) {
        droneEventsRepository.save(droneMessage.toEvent())
    }

    suspend fun streamDrone(droneId: Long): Flow<DroneMessage> {
        return outputReadOnlyFlow
            .filter { it.id == droneId }
            .onStart { emitAll(droneEventsRepository.findAll()
                .filter { it.id == droneId }
                .map { it.toMessage() })
            }
    }
}