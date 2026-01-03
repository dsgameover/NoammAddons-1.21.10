package com.github.noamm9.ui.hud

import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color
import kotlin.reflect.jvm.jvmName

abstract class HudElement {
    open val name = this::class.simpleName ?: this::class.jvmName

    abstract val enabled: Boolean
    var width = 0f
    var height = 0f

    var x = 0f
    var y = 0f

    var scale = 1f

    var isDragging = false
    private var dragX = 0f
    private var dragY = 0f

    fun renderElement(ctx: GuiGraphics, example: Boolean) {
        if (!enabled) return

        ctx.pose().pushMatrix()
        ctx.pose().translate(x, y)
        ctx.pose().scale(scale, scale)

        draw(ctx, example).run {
            width = first
            height = second
        }

        ctx.pose().popMatrix()
    }

    abstract fun draw(ctx: GuiGraphics, example: Boolean): Pair<Float, Float>

    fun drawEditor(ctx: GuiGraphics, mx: Int, my: Int) {
        if (!enabled) return

        if (isDragging) {
            x = mx - dragX
            y = my - dragY
        }

        val scaledW = width * scale
        val scaledH = height * scale
        val hovered = mx >= x && mx <= x + scaledW && my >= y && my <= y + scaledH

        val borderColor = if (isDragging || hovered) Style.accentColor else Color(255, 255, 255, 40)

        Render2D.drawRect(ctx, x, y, scaledW, scaledH, Color(10, 10, 10, 150))
        Render2D.drawRect(ctx, x, y, scaledW, 1f, borderColor)
        Render2D.drawRect(ctx, x, y + scaledH - 1f, scaledW, 1f, borderColor)

        renderElement(ctx, true)
    }

    fun isHovered(mx: Int, my: Int): Boolean {
        return mx >= x && mx <= x + (width * scale) && my >= y && my <= y + (height * scale)
    }

    fun startDragging(mx: Int, my: Int) {
        if (isHovered(mx, my)) {
            isDragging = true
            dragX = mx - x
            dragY = my - y
        }
    }
}