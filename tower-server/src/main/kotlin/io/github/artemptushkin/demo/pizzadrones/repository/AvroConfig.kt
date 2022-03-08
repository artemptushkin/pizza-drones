package io.github.artemptushkin.demo.pizzadrones.repository

import drones.avro.DroneEvent
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DatumWriter
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AvroConfiguration {
    @Bean
    fun droneEventsDatumWriter(): DatumWriter<DroneEvent> = SpecificDatumWriter(DroneEvent::class.java)

    @Bean
    fun droneEventsDatumReader(): DatumReader<DroneEvent> = SpecificDatumReader(DroneEvent.`SCHEMA$`)

    @Bean
    fun droneReaderProvider(eventStorageProperties: EventStorageProperties): () -> DataFileReader<DroneEvent> = {
        DataFileReader(eventStorageProperties.database.file, droneEventsDatumReader())
    }

    @Bean
    fun droneWriterProvider(eventStorageProperties: EventStorageProperties): () -> DataFileWriter<DroneEvent> = {
        DataFileWriter(droneEventsDatumWriter()).create(DroneEvent.`SCHEMA$`, eventStorageProperties.database.file)
    }
}