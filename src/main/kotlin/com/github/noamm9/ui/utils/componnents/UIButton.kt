package com.github.noamm9.ui.utils.componnents

import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

class UIButton(
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
    var text: String,
    private val colorProvider: (() -> Color)? = null,
    private val action: (UIButton) -> Unit
) {
    var overrideColor: Color? = null

    fun render(context: GuiGraphics, mouseX: Int, mouseY: Int) {
        val isHovered = isMouseOver(mouseX.toDouble(), mouseY.toDouble())

        val bgColor = if (isHovered) Color(45, 45, 45, 220) else Color(30, 30, 30, 200)
        Render2D.drawRect(context, x, y, width, height, bgColor)

        val stateColor = colorProvider?.invoke() ?: overrideColor
        val borderColor = if (isHovered) Style.accentColor else stateColor ?: Color(60, 60, 60)
        val textColor = if (isHovered) Style.accentColor else stateColor ?: Color.WHITE

        Render2D.drawRect(context, x, y, width, 1, borderColor)
        Render2D.drawRect(context, x, y + height - 1, width, 1, borderColor)
        Render2D.drawRect(context, x, y, 1, height, borderColor)
        Render2D.drawRect(context, x + width - 1, y, 1, height, borderColor)

        Render2D.drawCenteredString(
            context,
            text,
            x + width / 2,
            y + (height - 8) / 2,
            textColor
        )
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            Style.playClickSound(1f)
            action(this)
            return true
        }
        return false
    }

    private fun isMouseOver(mx: Double, my: Double): Boolean {
        return mx >= x && mx <= x + width && my >= y && my <= y + height
    }
}