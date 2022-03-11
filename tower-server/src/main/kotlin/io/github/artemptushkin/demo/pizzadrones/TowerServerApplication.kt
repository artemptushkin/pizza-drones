package io.github.artemptushkin.demo.pizzadrones

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class TowerServerApplication

fun main(args: Array<String>) {
    runApplication<TowerServerApplication>(*args)
}
