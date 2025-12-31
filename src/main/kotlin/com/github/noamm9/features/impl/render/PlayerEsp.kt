package com.github.noamm9.features.impl.render

import com.github.noamm9.NoammAddons
import com.github.noamm9.event.EventBus.register
import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.RenderUtils
import net.minecraft.client.Minecraft
import java.awt.Color


object PlayerEsp: Feature() {
    private val listener = register<RenderWorldEvent> {
        event.level.entitiesForRendering().forEach { entity ->
            if (entity == mc.player) return@forEach

        }
    }
}