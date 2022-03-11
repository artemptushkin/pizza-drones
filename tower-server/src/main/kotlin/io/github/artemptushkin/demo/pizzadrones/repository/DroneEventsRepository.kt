package io.github.artemptushkin.demo.pizzadrones.repository

import drones.avro.DroneEvent
import io.github.artemptushkin.demo.pizzadrones.service.IndexService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalCoroutinesApi::class)
@Repository
class DroneEventsRepository(
    private val inputChannel: Channel<DroneEvent>,
    private val outputFlow: MutableSharedFlow<DroneEvent>,
    private val droneWriterProvider: () -> DataFileWriter<DroneEvent>,
    private val droneReaderProvider: () -> DataFileReader<DroneEvent>,
    private val indexService: IndexService
) {
    private val atomicCounter: AtomicLong = AtomicLong()

    init {
        CoroutineScope(Dispatchers.IO)
            .launch {
                while (!inputChannel.isClosedForReceive) {
                    val droneEventsWriter = droneWriterProvider()
                    inputChannel.consumeEach {
                        droneEventsWriter.append(it)
                //        println("persisting")
                        droneEventsWriter.flush()
                        outputFlow.emit(it)
                        val newLineIndex = atomicCounter.getAndIncrement()
                        indexService[it.id] = newLineIndex
                    }
                }
            }
    }

    suspend fun save(droneEvent: DroneEvent) {
       // println("saving an event with drone id ${droneEvent.id}")
        inputChannel.send(droneEvent)
    }

    fun get(droneId: Long): Flow<DroneEvent> {
        return flow {
            val droneEventsReader: Iterable<DroneEvent> = droneReaderProvider()
            var indexPosition = 0
            val linesIterator = droneEventsReader.withIndex()
            for (indexedValue in linesIterator) {
                if (indexedValue.value.id == droneId) {
                    emit(indexedValue.value)
                    ++indexPosition
                }
            }
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