package io.github.artemptushkin.demo.pizzadrones.controller

import io.github.artemptushkin.demo.pizzadrones.domain.DroneMessage
import io.github.artemptushkin.demo.pizzadrones.service.DroneEventsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller

@Controller
class TowerServerController(private val droneEventsService: DroneEventsService) {

    @MessageMapping("api.drones.locations.channel")
    suspend fun receive(@Payload droneMessages: Flow<DroneMessage>): Flow<Unit> {
        return droneMessages
            .onEach { message -> droneEventsService.save(message) }
            .map {}
    }

    @MessageMapping("api.drones.locations.fire")
    suspend fun receiveSingle(@Payload droneMessage: DroneMessage) {
        droneEventsService.save(droneMessage)
    }

    @MessageMapping("api.drones.locations.stream")
    suspend fun send(): Flow<DroneMessage> = droneEventsService.stream()

    @MessageMapping("api.drone.locations.stream")
    suspend fun sendDrone(droneId: Long): Flow<DroneMessage> {
        return droneEventsService.streamDrone(droneId)
    }
}