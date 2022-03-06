package io.github.artemptushkin.demo.pizzadrones.repository

import io.github.artemptushkin.demo.pizzadrones.domain.DroneEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Repository
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@Repository
class DroneEventsRepository(private val eventStorageProperties: EventStorageProperties, private val eventsChannel: Channel<DroneEvent>) {
    private val index: MutableMap<Long, MutableList<Int>> = ConcurrentHashMap()
    private val atomicCounter: AtomicInteger = AtomicInteger()

    init {
        val databaseFile = eventStorageProperties.database.file
        val writer = FileOutputStream(databaseFile, eventStorageProperties.append).bufferedWriter()
        CoroutineScope(Dispatchers.IO)
            .launch {
                while (!eventsChannel.isClosedForReceive) {
                    println("consuming all of them")
                    eventsChannel.consumeEach {
                        writer.write(StringBuilder(it.droneId.toString()).append('\n').toString())
                        println("persisting")
                        writer.flush()
                    }
                }
            }
    }

    suspend fun save(droneEvent: DroneEvent) {
        println("saving an event with timestamp ${droneEvent.droneId}")
        val newLine = atomicCounter.incrementAndGet()
        index.compute(droneEvent.droneId) { _, u ->
            val result: MutableList<Int> = u ?: mutableListOf()
            result.add(newLine)
            u
        }
        eventsChannel.send(droneEvent)
    }

    fun get(droneId: Long): Flow<String> {
        val currentDroneIndices: MutableList<Int>? = index[droneId]
        if (currentDroneIndices != null) {
            return flow {
                var position = 0
                eventStorageProperties.database.file
                    .reader()
                    .readLines()
                    .forEachIndexed { index, s ->
                        if (index == currentDroneIndices[position]) {
                            emit(s)
                            ++position
                        }
                    }
            }
        }
        return emptyFlow()
    }

    fun findAll(): Flow<String> {
        return eventStorageProperties.database.file
            .reader()
            .readLines()
            .asFlow()
    }
}