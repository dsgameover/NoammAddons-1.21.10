package com.github.noamm9

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.event.EventBus
import com.github.noamm9.event.impl.PacketEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.protocol.game.ClientboundSetTimePacket

object TestGround {
    private var lastServerTime = - 1L
    private var lastRealTime = - 1L

    init {
        EventBus.register<WorldChangeEvent> {
            lastServerTime = - 1
            lastRealTime = - 1
        }

        EventBus.register<PacketEvent.Received> {
            if (event.packet !is ClientboundSetTimePacket) return@register

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
                    mc.execute {
                        EventBus.post(TickEvent.Server)
                    }

                    delay(instantTickDuration)
                }
            }
        }
    }
}