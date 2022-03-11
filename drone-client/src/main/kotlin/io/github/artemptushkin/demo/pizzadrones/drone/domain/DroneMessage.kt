package io.github.artemptushkin.demo.pizzadrones.drone.domain

data class DroneMessage(val id: Long, val timestamp: Long, val latitude: Double, val longitude: Double)