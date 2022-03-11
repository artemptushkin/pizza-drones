package io.github.artemptushkin.demo.pizzadrones.service

import io.github.artemptushkin.demo.pizzadrones.configuration.EventStorageProperties
import org.springframework.stereotype.Service
import java.io.File
import java.io.OutputStreamWriter
import java.util.concurrent.ConcurrentHashMap

@Service
class IndexService(private val eventStorageProperties: EventStorageProperties, private val index: MutableMap<Long, Long>) :
    MutableMap<Long, Long> by index {
    private val writers: MutableMap<Long, OutputStreamWriter> = ConcurrentHashMap()

    override fun put(key: Long, value: Long): Long? {
        val writer = if (writers.containsKey(key)) writers[key] else createWriter(key)
        writer!!.write(value.toString())
        writer.flush()
        return index.put(key, value)
    }

    override fun putAll(from: Map<out Long, Long>) {
        return index.putAll(from)
    }

    override fun putIfAbsent(key: Long, value: Long): Long? {
        return index.putIfAbsent(key, value)
    }

    override fun get(id: Long): Long? {
        return index[id]
    }

    private fun createWriter(key: Long): OutputStreamWriter {
        val storageDir = File(eventStorageProperties.locationPath)
        val indexFile = File(storageDir, key.toString())
        if (!indexFile.exists()) indexFile.createNewFile()
        val writer = indexFile.writer()
        writers[key] = writer
        return writer
    }
}