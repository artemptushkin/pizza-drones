package io.github.artemptushkin.demo.pizzadrones.controller

import app.cash.turbine.test
import io.github.artemptushkin.demo.pizzadrones.domain.DroneMessage
import io.rsocket.transport.netty.client.TcpClientTransport
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.messaging.rsocket.sendAndAwait
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
class TowerServerControllerTest {
    @Autowired
    lateinit var rsocketBuilder: RSocketRequester.Builder

    @Value("\${spring.rsocket.server.port}")
    private var serverPort = 0

    @Test
    fun `it saves and streams messages`() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.transport(TcpClientTransport.create("localhost", serverPort))

            rSocketRequester
                .route("api.drones.locations.fire")
                .data(randomEvent())
                .sendAndAwait()

            rSocketRequester
                .route("api.drones.locations.stream")
                .retrieveFlow<DroneMessage>()
                .test {
                    assertThat(this.awaitItem()).isNotNull
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun `it saves and streams messages by drone id`() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.transport(TcpClientTransport.create("localhost", serverPort))
            val droneId = Random.nextLong()
            rSocketRequester
                .route("api.drones.locations.fire")
                .data(droneEvent(droneId))
                .sendAndAwait()

            rSocketRequester
                .route("api.drone.locations.stream")
                .data(droneId)
                .retrieveFlow<DroneMessage>()
                .test {
                    assertThat(this.awaitItem()).isNotNull
                }
        }
    }

    private fun randomEvent() = DroneMessage(Random.nextLong(), ZonedDateTime.now().toEpochSecond(), 10.1, 20.0)
    private fun droneEvent(id: Long) = DroneMessage(id, ZonedDateTime.now().toEpochSecond(), 10.1, 20.0)
}