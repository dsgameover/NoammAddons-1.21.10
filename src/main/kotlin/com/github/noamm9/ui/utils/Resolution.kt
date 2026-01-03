package com.github.noamm9.ui.utils

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.NumbersUtils.div
import net.minecraft.client.gui.GuiGraphics

object Resolution {
    private const val REFERENCE_WIDTH = 960f

    var customScale = 1f
        private set
    var width = 960f
        private set
    var height = 540f
        private set

    /**
     * Updates the scale math. Call this at the very beginning of 'render'
     */
    fun refresh() {
        customScale = mc.window.guiScaledWidth.toFloat() / REFERENCE_WIDTH
        width = REFERENCE_WIDTH
        height = mc.window.guiScaledHeight.toFloat() / customScale
    }

    /**
     * Scales the renderer. Use this before drawing your UI.
     */
    fun apply(ctx: GuiGraphics) {
        ctx.pose().pushMatrix()
        ctx.pose().scale(customScale, customScale)
    }

    /**
     * Reverts the scale. Use this at the end of 'render'
     */
    fun restore(ctx: GuiGraphics) {
        ctx.pose().popMatrix()
    }

    /**
     * Converts Minecraft's scaled mouse to our logical coordinates.
     */
    fun getMouseX(vanillaX: Number): Int = (vanillaX / customScale).toInt()
    fun getMouseY(vanillaY: Number): Int = (vanillaY / customScale).toInt()
}