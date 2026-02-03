package com.github.noamm9

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.event.EventBus
import com.github.noamm9.event.impl.PacketEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.utils.ThreadUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.protocol.game.ClientboundSetTimePacket

object TestGround {
    private var lastGameTime = - 1L
    private var ticks = 0

    init {
        ThreadUtils.loop(1000) {
            ticks = 0
        }

        EventBus.register<WorldChangeEvent> {
            ticks = 0
            lastGameTime = - 1
        }

        EventBus.register<PacketEvent.Received> {
            if (event.packet is ClientboundSetTimePacket) {
                val currentGameTime = event.packet.gameTime
                if (lastGameTime == - 1L) {
                    lastGameTime = currentGameTime
                    return@register
                }

                val diff = (currentGameTime - lastGameTime).toInt()
                lastGameTime = currentGameTime
                if (diff <= 0) return@register

                val delayTime = (1000 / diff).coerceAtLeast(1).toLong()

                NoammAddons.scope.launch {
                    repeat(diff) {
                        serverTick()
                        delay(delayTime)
                    }
                }
            }
        }
    }

    private fun serverTick() {
        ticks ++
        mc.execute { EventBus.post(TickEvent.Server) }
    }
}