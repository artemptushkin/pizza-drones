package io.github.artemptushkin.demo.pizzadrones

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PizzaDronesApplication

fun main(args: Array<String>) {
    runApplication<PizzaDronesApplication>(*args)
}
