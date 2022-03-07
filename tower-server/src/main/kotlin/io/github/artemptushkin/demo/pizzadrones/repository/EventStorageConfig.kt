package io.github.artemptushkin.demo.pizzadrones.repository

import io.github.artemptushkin.demo.pizzadrones.domain.DroneEvent
import kotlinx.coroutines.channels.Channel
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
    var append: Boolean = false

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
    fun droneEventChannel(): Channel<DroneEvent> = Channel(capacity = 3)
}