package com.github.noamm9

import com.github.noamm9.event.EventBus
import com.github.noamm9.event.impl.PacketEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.protocol.game.ClientboundSetTimePacket

class TestGround {
    private var lastServerTime = - 1L
    private var lastRealTime = - 1L

    companion object {
        val experimental get() = NoammAddons.debugFlags.contains("experimental-tick")
    }

    init {
        EventBus.register<WorldChangeEvent> {
            if (experimental) {
                lastServerTime = - 1
                lastRealTime = - 1
            }
        }

        EventBus.register<PacketEvent.Received> {
            if (event.packet is ClientboundSetTimePacket) {
                if (! experimental) return@register
                val newServerTime = event.packet.gameTime
                val newRealTime = System.currentTimeMillis()

                if (lastServerTime == - 1L) {
                    lastServerTime = newServerTime
                    lastRealTime = newRealTime
                    return@register
                }

                val tickDiff = (newServerTime - lastServerTime).toInt()
                if (tickDiff <= 0) return@register

                val timePassed = newRealTime - lastRealTime
                val instantTickDuration = timePassed / tickDiff

                lastServerTime = newServerTime
                lastRealTime = newRealTime

                NoammAddons.scope.launch {
                    repeat(tickDiff) {
                        EventBus.post(TickEvent.Server)
                        delay(instantTickDuration)
                    }
                }
            }
        }
    }
}