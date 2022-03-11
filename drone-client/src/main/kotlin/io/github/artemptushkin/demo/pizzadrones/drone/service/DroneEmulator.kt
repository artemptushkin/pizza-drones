package io.github.artemptushkin.demo.pizzadrones.drone.service

import io.github.artemptushkin.demo.pizzadrones.drone.domain.DroneMessage
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.ZonedDateTime.now
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Service
class DroneEmulator(private val rsocketRequester: RSocketRequester): CommandLineRunner {
    private val dronePool: List<Long> = listOf(1L, 2L, 5L, 110L, 200L)

    override fun run(vararg args: String?) {
        runBlocking {
            val subscribe = rsocketRequester
                .route("api.drones.locations.channel")
                .dataWithType(generateMessage())
                .retrieveFlux(Void.TYPE)
                .subscribe { println("subscribed") }

            while (!subscribe.isDisposed) {}
        }
    }

    private fun generateMessage(): Flux<DroneMessage> {
        return Flux
            .interval(Duration.ofMillis((500..2000).random().toLong()))
            .doOnEach { println("sending an event to tower server") }
            .map { DroneMessage(dronePool.random(), now().toEpochSecond(), 44.8, 10.25) }
    }
}