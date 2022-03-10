package io.github.artemptushkin.demo.pizzadrones.repository

import drones.avro.DroneEvent
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@Repository
class DroneEventsRepository(
    private val inputChannel: Channel<DroneEvent>,
    private val outputFlow: MutableSharedFlow<DroneEvent>,
    private val droneWriterProvider: () -> DataFileWriter<DroneEvent>,
    private val droneReaderProvider: () -> DataFileReader<DroneEvent>
) {
    private val index: MutableMap<Long, MutableList<Int>> = ConcurrentHashMap()
    private val atomicCounter: AtomicInteger = AtomicInteger()

    init {
        CoroutineScope(Dispatchers.IO)
            .launch {
                while (!inputChannel.isClosedForReceive) {
                    val droneEventsWriter = droneWriterProvider()
                    inputChannel.consumeEach {
                        droneEventsWriter.append(it)
                        println("persisting")
                        droneEventsWriter.flush()
                        outputFlow.emit(it)
                    }
                }
            }
    }

    suspend fun save(droneEvent: DroneEvent) {
        println("saving an event with drone id ${droneEvent.id}")
        val newLine = atomicCounter.getAndIncrement()
        index.compute(droneEvent.id) { _, u ->
            val result: MutableList<Int> = u ?: mutableListOf()
            result.add(newLine)
            result
        }
        inputChannel.send(droneEvent)
    }

    //todo it should loop through lines and do not deserialize records OR google avro indexing
    fun get(droneId: Long): Flow<DroneEvent> {
        return flow {
            val droneEventsReader: Iterable<DroneEvent> = droneReaderProvider()
            val currentDroneIndices: MutableList<Int>? = index[droneId]
            if (currentDroneIndices != null) {
                var indexPosition = 0
                val linesIterator = droneEventsReader.withIndex()
                for (indexedValue in linesIterator) {
                    if (indexedValue.index == currentDroneIndices[indexPosition]) {
                        emit(indexedValue.value)
                        ++indexPosition
                    }
                    if (currentDroneIndices.size == indexPosition) break
                }
            }
        }
    }

    fun findAll(): Flow<DroneEvent> {
        val droneEventsReader = droneReaderProvider()
        return flow {
            while (droneEventsReader.hasNext()) {
                emit(droneEventsReader.next())
            }
        }
    }
}