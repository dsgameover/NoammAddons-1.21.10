package com.github.noamm9.ui.clickgui

import com.github.noamm9.config.Config
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component


object ClickGuiScreen: Screen(Component.literal("ClickGUI")) {
    private val panels = mutableListOf<Panel>()

    init {
        CategoryType.entries.forEachIndexed { index, category ->
            panels.add(Panel(category, 20 + (index * 110), 20))
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        panels.forEach { it.render(context, mouseX, mouseY) }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val mouseX = mouseButtonEvent.x
        val mouseY = mouseButtonEvent.y
        val button = mouseButtonEvent.button()
        panels.forEach { it.mouseClicked(mouseX, mouseY, button) }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        panels.forEach { it.mouseReleased(mouseButtonEvent.x, mouseButtonEvent.y, mouseButtonEvent.button()) }
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun onClose() {
        Config.save()
        super.onClose()
    }
    override fun isPauseScreen(): Boolean = false
}

enum class CategoryType {
    COMBAT, DEV, PLAYER, VISUAL, MISC
}