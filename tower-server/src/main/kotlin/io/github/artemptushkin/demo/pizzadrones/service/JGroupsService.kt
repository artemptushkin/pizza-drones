package io.github.artemptushkin.demo.pizzadrones.service

import drones.avro.DroneEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jgroups.BytesMessage
import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.Receiver
import org.jgroups.blocks.cs.ReceiverAdapter
import org.springframework.stereotype.Service

@Service
class JGroupsService(private val jChannel: JChannel, private val outputFlow: MutableSharedFlow<DroneEvent>) : ReceiverAdapter(), Receiver {

    init {
        jChannel.receiver = this
    }

    suspend fun emit(droneEvent: DroneEvent) {
        jChannel.send(BytesMessage(null, droneEvent.toByteBuffer().array()))
    }

    override fun receive(msg: Message) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                outputFlow.emit(DroneEvent.getDecoder().decode(msg.getObject<ByteArray>()))
            }
    }
}