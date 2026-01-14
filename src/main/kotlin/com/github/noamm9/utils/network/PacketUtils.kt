package com.github.noamm9.utils.network

import com.github.noamm9.NoammAddons.mc
import net.minecraft.network.protocol.Packet

object PacketUtils {
    fun Packet<*>.send() = mc.connection?.send(this)
}