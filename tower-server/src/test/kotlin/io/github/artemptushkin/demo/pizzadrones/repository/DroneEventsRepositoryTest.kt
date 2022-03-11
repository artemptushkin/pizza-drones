package io.github.artemptushkin.demo.pizzadrones.repository

import drones.avro.DroneEvent
import io.github.artemptushkin.demo.pizzadrones.configuration.EventStorageConfiguration
import io.github.artemptushkin.demo.pizzadrones.configuration.EventStorageProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime.now
import kotlin.io.path.deleteIfExists
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * It resets the context after each test method to reinitialize database that is removed in @AfterEach method
 */
@OptIn(ExperimentalTime::class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = [EventStorageConfiguration::class, DroneEventsRepositoryTest.Config::class], initializers = [ConfigDataApplicationContextInitializer::class])
class DroneEventsRepositoryTest {

    @Autowired
    lateinit var droneEventsRepository: DroneEventsRepository

    @Autowired
    lateinit var storageProperties: EventStorageProperties

    @AfterEach
    fun cleanup() {
        storageProperties.database.file.toPath().deleteIfExists()
    }

    @Test
    fun `it saves 10 events`() {

        runBlocking {
            for (i in 0 until 10) {
                droneEventsRepository.save(randomEvent())
            }
            assertThat(droneEventsRepository.findAll().toList()).hasSize(10)
        }
    }

    @Test
    fun `it saves and returns`() {
        val id1 = Random.nextLong()
        val id2 = Random.nextLong()
        val drone1Event1 = droneEvent(id1)
        val drone1Event2 = droneEvent(id1)
        val drone2Event1 = droneEvent(id2)
        val drone2Event2 = droneEvent(id2)

        runBlocking {
            droneEventsRepository.save(drone1Event1)
            droneEventsRepository.save(drone2Event1)
            droneEventsRepository.save(drone2Event2)
            droneEventsRepository.save(drone1Event2)

            assertThat(droneEventsRepository.get(id1).toList()).hasSize(2)
            assertThat(droneEventsRepository.get(id2).toList()).hasSize(2)
        }
    }

    @Test
    fun `it stores many events`() {
        val elementsToProcess = 100000
        val time = measureTime {
            runBlocking {
                for (i in 0 until elementsToProcess) {
                    droneEventsRepository.save(randomEvent())
                }
            }
        }
        val count: Int = runBlocking {
            droneEventsRepository
                .findAll()
                .withIndex()
                .count()
        }
        println("it took $time to process $count elements")
        assertThat(count).isEqualTo(elementsToProcess)
    }

    @Test
    fun `it stores many events and returns per drone`() {
        val elementsToProcess = 100000
        val droneId = Random.nextLong()
        val time = measureTime {
            runBlocking {
                for (i in 0 until elementsToProcess) {
                    droneEventsRepository.save(droneEvent(droneId))
                }
            }
        }
        val count: Int = runBlocking {
            droneEventsRepository
                .get(droneId)
                .withIndex()
                .count()
        }
        println("it took $time to process $count elements")
        assertThat(count).isEqualTo(elementsToProcess)
    }

    @Test
    fun `it stores in concurrent mode`() {
        val droneId1 = Random.nextLong()
        val droneId2 = Random.nextLong()
        CoroutineScope(Dispatchers.IO)
            .launch {
                for (i in 0 until 100) {
                    droneEventsRepository.save(droneEvent(droneId1))
                }
            }
        CoroutineScope(Dispatchers.IO)
            .launch {
                for (i in 0 until 100) {
                    droneEventsRepository.save(droneEvent(droneId2))
                }
            }
        runBlocking {
            delay(300)
            assertThat(droneEventsRepository
                .findAll()
                .withIndex()
                .count()).isEqualTo(200)
        }
    }

    private fun randomEvent() = DroneEvent(Random.nextLong(), now().toEpochSecond(), 10.1, 20.0)
    private fun droneEvent(id: Long) = DroneEvent(id, now().toEpochSecond(), 10.1, 20.0)

    @Configuration
    @ComponentScan("io.github.artemptushkin.demo.pizzadrones.repository")
    internal class Config
}