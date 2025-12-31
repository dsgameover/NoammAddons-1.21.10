package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.Level

class RenderWorldEvent(
    val level: ClientLevel,
    val poseStack: PoseStack,
    val partialTick: Float
) : Event(cancelable = false)