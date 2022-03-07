package io.github.artemptushkin.demo.pizzadrones.repository.test

import io.github.artemptushkin.demo.pizzadrones.repository.EventStorageProperties
import org.springframework.stereotype.Component

@Component
class DatabaseStorageTestUtil(private val eventStorageProperties: EventStorageProperties) {
    fun resetDatabase() {
        val writer = eventStorageProperties.database.file.writer()
        writer.close()
    }
}