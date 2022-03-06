package io.github.artemptushkin.demo.pizzadrones.repository

import io.github.artemptushkin.demo.pizzadrones.domain.DroneEvent
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime.now
import kotlin.random.Random

@ActiveProfiles("test")
//@SpringBootTest
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [EventStorageConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
class DroneEventsRepositoryTest {

    @Autowired
    lateinit var droneEventsRepository: DroneEventsRepository

    @Test
    fun `it saves 10 events`() {

        runBlocking {
            for (i in 0 until 10) {
                droneEventsRepository.save(DroneEvent(now().toEpochSecond(), Random.nextLong()))
            }
            println("fetching all")
            assertThat(droneEventsRepository.findAll().toList()).hasSize(10)
        }
    }
}