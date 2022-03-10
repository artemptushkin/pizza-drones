package io.github.artemptushkin.demo.pizzadrones.repository

import drones.avro.DroneEvent
import io.github.artemptushkin.demo.pizzadrones.domain.DroneMessage
import io.github.artemptushkin.demo.pizzadrones.domain.toMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.io.File

@ConstructorBinding
@ConfigurationProperties("storage")
class EventStorageProperties : InitializingBean {
    lateinit var locationPath: String
    lateinit var database: Resource

    override fun afterPropertiesSet() {
        val locationDir = File(locationPath)
        if (locationDir.exists() || locationDir.mkdirs()) {
            val databaseFile = File(locationDir, "database.avro")
            if (databaseFile.exists() || databaseFile.createNewFile()) {
                database = FileSystemResource(databaseFile)
            } else {
                throw IllegalStateException("Failed to create database file")
            }
        } else {
            throw IllegalStateException("Failed to create database directory")
        }
    }
}

@Configuration
@ComponentScan
@EnableConfigurationProperties(EventStorageProperties::class)
class EventStorageConfiguration {

    @Bean
    fun inputChannel(): Channel<DroneEvent> = Channel(capacity = 3)

    @Bean
    fun outputFlow(): MutableSharedFlow<DroneEvent> = MutableSharedFlow()

    @Bean
    fun outputReadOnlyFlow(): SharedFlow<DroneMessage> = outputFlow()
        .map { it.toMessage() }
        .shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily)
}