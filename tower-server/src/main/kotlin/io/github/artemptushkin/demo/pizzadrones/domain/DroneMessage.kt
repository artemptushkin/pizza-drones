package io.github.artemptushkin.demo.pizzadrones.domain

import drones.avro.DroneEvent

data class DroneMessage(val id: Long, val timestamp: Long, val latitude: Double, val longitude: Double)

fun DroneMessage.toEvent() = DroneEvent(this.id, this.timestamp, this.latitude, this.longitude)

fun DroneEvent.toMessage() = DroneMessage(this.id, this.timestamp, this.latitude, this.longitude)