package com.github.noamm9.ui.clickgui.componnents.impl

import com.github.noamm9.config.Savable
import com.github.noamm9.ui.clickgui.componnents.Setting
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.utils.Animation
import com.github.noamm9.utils.render.Render2D
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import java.awt.Color

class TextInputSetting(name: String, value: String): Setting<String>(name, value), Savable {
    var focused = false
    private val hoverAnim = Animation(200L)

    override val height get() = 24

    override fun draw(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        val isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
        hoverAnim.update(if (isHovered || focused) 1f else 0f)

        Style.drawBackground(ctx, x, y, width, height)
        Style.drawHoverBar(ctx, x, y, height, hoverAnim.value)

        val labelColor = if (focused) Color.WHITE else Color.GRAY
        Style.drawNudgedText(ctx, name, x + 8f, y + 2f, hoverAnim.value, labelColor)

        val bx = x + 8f;
        val by = y + 13f;
        val bw = width - 16f;
        val bh = 9f
        Render2D.drawRect(ctx, bx, by, bw, bh, Color(10, 10, 10, 150))
        Render2D.drawRect(ctx, bx, by + bh - 0.5f, bw * hoverAnim.value, 0.5f, Style.accentColor)

        val cursor = if (focused && (System.currentTimeMillis() / 500) % 2 == 0L) "_" else ""
        val display = if (value.isEmpty() && ! focused) "§8Type..." else value + cursor
        Render2D.drawString(ctx, display, bx + 2, by + 1, Color.WHITE)
    }

    override fun charTyped(codePoint: Char): Boolean {
        if (focused) {
            value += codePoint
            return true
        }
        return false
    }

    override fun keyPressed(keyCode: Int): Boolean {
        if (focused) {
            if (keyCode == InputConstants.KEY_BACKSPACE && value.isNotEmpty()) {
                value = value.dropLast(1)
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == InputConstants.KEY_ESCAPE) {
                focused = false
                return true
            }
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        focused = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
        return focused
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }
}