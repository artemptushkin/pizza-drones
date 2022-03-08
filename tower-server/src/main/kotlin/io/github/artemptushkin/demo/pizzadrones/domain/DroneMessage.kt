package io.github.artemptushkin.demo.pizzadrones.domain

import drones.avro.DroneEvent

data class DroneMessage(val droneId: Long, val timestamp: Long, )

fun DroneMessage.toEvent() = DroneEvent(this.droneId, this.timestamp)

fun DroneEvent.toMessage() = DroneMessage(this.id, this.timestamp)