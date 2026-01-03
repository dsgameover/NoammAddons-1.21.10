package com.github.noamm9.ui.hud

import com.github.noamm9.config.Config
import com.github.noamm9.features.FeatureManager
import com.github.noamm9.ui.utils.Resolution
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import java.awt.Color

object HudEditorScreen: Screen(Component.literal("HudEditor")) {
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        Resolution.refresh()
        Resolution.apply(context)

        val mX = Resolution.getMouseX(mouseX)
        val mY = Resolution.getMouseY(mouseY)

        val midX = Resolution.width / 2

        FeatureManager.hudElements.forEach { it.drawEditor(context, mX, mY) }

        Render2D.drawCenteredString(context, "Dragging HUD Elements...", midX, 10f, Color.WHITE, 1.2f)
        Render2D.drawCenteredString(context, "ESC to Save and Exit", midX, Resolution.height - 20f, Color.GRAY, shadow = false)

        Resolution.restore(context)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontal: Double, vertical: Double): Boolean {
        FeatureManager.hudElements.asReversed().forEach { element ->
            if (element.isDragging) {
                val increment = (vertical * 0.1).toFloat()
                element.scale = (element.scale + increment).coerceIn(0.5f, 5.0f)
                return true
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val mX = Resolution.getMouseX(mouseButtonEvent.x)
        val mY = Resolution.getMouseY(mouseButtonEvent.y)

        if (mouseButtonEvent.button() == 0) {
            FeatureManager.hudElements.asReversed().forEach {
                it.startDragging(mX, mY)
                if (it.isDragging) return true
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        FeatureManager.hudElements.forEach { it.isDragging = false }
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun onClose() {
        Config.save()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false
}