package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

abstract class ScreenEvent(val screen: Screen): Event(cancelable = true) {
    class PreRender(screen: Screen, val context: GuiGraphics, val mouseX: Int, val mouseY: Int): ScreenEvent(screen)
}