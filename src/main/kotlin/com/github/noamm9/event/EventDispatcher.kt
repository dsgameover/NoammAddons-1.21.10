package com.github.noamm9.event

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.event.EventBus.register
import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.EntityDeathEvent
import com.github.noamm9.event.impl.PacketEvent
import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.event.impl.ServerEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ChatUtils.formattedText
import com.github.noamm9.utils.render.RenderContext
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.protocol.common.ClientboundPingPacket
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.entity.Entity

object EventDispatcher {
    fun init() {
        WorldRenderEvents.END_MAIN.register { context ->
            EventBus.post(RenderWorldEvent(RenderContext.fromContext(context)))
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            EventBus.post(ServerEvent.Connect())
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            EventBus.post(ServerEvent.Disconnect())
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            EventBus.post(WorldChangeEvent())
        }

        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            mc.level?.let { EventBus.post(TickEvent.Start()) }
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            mc.level?.let { EventBus.post(TickEvent.End()) }
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            EventBus.post(EntityDeathEvent(entity))
        }

        register<PacketEvent.Received> {
            if (event.packet is ClientboundPingPacket) {
                if (event.packet.id != 0) {
                    EventBus.post(TickEvent.Server())
                }
            }
            else if (event.packet is ClientboundSystemChatPacket) {
                if (event.packet.overlay) return@register
                event.isCanceled = true

                mc.execute {
                    if (!EventBus.post(ChatMessageEvent(event.packet.content))) {
                        event.packet.handle(mc.connection)
                    }
                }
            }
        }
    }
}