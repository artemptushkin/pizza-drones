package io.github.artemptushkin.demo.pizzadrones.service

import io.github.artemptushkin.demo.pizzadrones.domain.DroneMessage
import io.github.artemptushkin.demo.pizzadrones.repository.EventStorageConfiguration
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [EventStorageConfiguration::class, DroneEventsService::class], initializers = [ConfigDataApplicationContextInitializer::class])
class DroneEventsServiceTest {

    @Autowired
    lateinit var droneEventsService: DroneEventsService

    @Test
    fun `it streams all events per drone`() {
        val droneId = Random.nextLong()
        val elementsCount = 10
        runBlocking {
            val droneStream = droneEventsService.streamDrone(droneId)

            for (i in 0..elementsCount) {
                droneEventsService.save(droneEvent(droneId))
            }

            assertThat(
                droneStream
                    .withIndex()
                    .takeWhile { it.index != elementsCount }
                    .toList()
            ).hasSize(elementsCount)
        }
    }

    @Test
    fun `it streams all events`() {
        val droneId1 = Random.nextLong()
        val droneId2 = Random.nextLong()
        val elementsPerDrone = 10
        runBlocking {
            val droneStream = droneEventsService.stream()

            for (i in 0..elementsPerDrone) {
                droneEventsService.save(droneEvent(droneId1))
            }

            for (i in 0..elementsPerDrone) {
                droneEventsService.save(droneEvent(droneId2))
            }

            assertThat(
                droneStream
                    .withIndex()
                    .takeWhile { it.index != elementsPerDrone * 2 }
                    .toList()
            ).hasSize(elementsPerDrone * 2)
        }
    }

    @Test
    fun `it streams all events to multiple consumers`() {
        val droneId1 = Random.nextLong()
        val droneId2 = Random.nextLong()
        val elementsPerDrone = 10
        runBlocking {
            val droneConsumerStream1 = droneEventsService.stream()
            val droneConsumerStream2 = droneEventsService.stream()

            for (i in 0..elementsPerDrone) {
                droneEventsService.save(droneEvent(droneId1))
            }

            for (i in 0..elementsPerDrone) {
                droneEventsService.save(droneEvent(droneId2))
            }

            assertThat(
                droneConsumerStream1
                    .withIndex()
                    .takeWhile { it.index != elementsPerDrone * 2 }
                    .toList()
            ).hasSize(elementsPerDrone * 2)

            assertThat(
                droneConsumerStream2
                    .withIndex()
                    .takeWhile { it.index != elementsPerDrone * 2 }
                    .toList()
            ).hasSize(elementsPerDrone * 2)
        }
    }

    @Test
    fun `it returns instance of shared flow on drone stream`() {
        runBlocking {
            val droneId = Random.nextLong()
            droneEventsService.save(droneEvent(droneId))
            assertThat(droneEventsService.streamDrone(droneId)).isInstanceOf(SharedFlow::class.java)
        }
    }

    private fun droneEvent(id: Long) = DroneMessage(id, ZonedDateTime.now().toEpochSecond()) //todo move to commons
}