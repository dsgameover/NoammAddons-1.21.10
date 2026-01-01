package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import net.minecraft.network.protocol.Packet

abstract class MainThreadPacketRecivedEvent(cancellable: Boolean): Event(cancellable) {
    class Pre(val packet: Packet<*>): MainThreadPacketRecivedEvent(true)
    class Post(val packet: Packet<*>): MainThreadPacketRecivedEvent(false)
}