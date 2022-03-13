package io.github.artemptushkin.demo.pizzadrones.repository

import drones.avro.DroneEvent
import io.github.artemptushkin.demo.pizzadrones.service.JGroupsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.springframework.stereotype.Repository

@OptIn(ExperimentalCoroutinesApi::class)
@Repository
class DroneEventsRepository(
    private val inputChannel: Channel<DroneEvent>,
    private val jGroupsService: JGroupsService,
    private val droneWriterProvider: () -> DataFileWriter<DroneEvent>,
    private val droneReaderProvider: () -> DataFileReader<DroneEvent>
) {

    init {
        CoroutineScope(Dispatchers.IO)
            .launch {
                while (!inputChannel.isClosedForReceive) {
                    val droneEventsWriter = droneWriterProvider()
                    inputChannel.consumeEach {
                        droneEventsWriter.append(it)
                        droneEventsWriter.flush()
                        jGroupsService.emit(it)
                    }
                }
            }
    }

    suspend fun save(droneEvent: DroneEvent) {
        inputChannel.send(droneEvent)
    }

    fun get(droneId: Long): Flow<DroneEvent> {
        return flow {
            droneReaderProvider()
                .filter { it.id == droneId }
                .forEach { emit(it) }
        }
    }

    fun findAll(): Flow<DroneEvent> {
        return flow {
            val droneEventsReader = droneReaderProvider()
            while (droneEventsReader.hasNext()) {
                emit(droneEventsReader.next())
            }
        }
    }
}