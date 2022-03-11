package io.github.artemptushkin.demo.pizzadrones.drone

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DroneClientApplication

fun main(args: Array<String>) {
    runApplication<DroneClientApplication>(*args)
}
