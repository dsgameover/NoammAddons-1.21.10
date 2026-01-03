package com.github.noamm9.ui.clickgui.componnents.impl

import com.github.noamm9.config.Savable
import com.github.noamm9.ui.clickgui.componnents.Setting
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.utils.Animation
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import java.awt.Color

class KeybindSetting(name: String, value: Int): Setting<Int>(name, value), Savable {
    var listening = false
    private val hoverAnim = Animation(200)

    override fun draw(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        val isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
        hoverAnim.update(if (isHovered) 1f else 0f)

        Style.drawBackground(ctx, x, y, width, height)
        Style.drawHoverBar(ctx, x, y, height, hoverAnim.value)
        Style.drawNudgedText(ctx, name, x + 8f, y + 6f, hoverAnim.value)

        val bindText = if (listening) "§b..." else "§7" + (GLFW.glfwGetKeyName(value, 0)?.uppercase() ?: "NONE")
        Render2D.drawString(ctx, bindText, x + width - bindText.width() - 8f, y + 6f, Color.WHITE)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            listening = ! listening
            Style.playClickSound(1f)
            return true
        }
        return false
    }

    override fun keyPressed(keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                listening = false
                return true
            }

            value = if (keyCode == InputConstants.KEY_BACKSPACE) 0 else keyCode
            listening = false
            return true
        }
        return false
    }

    override fun write(): JsonElement = JsonPrimitive(value)
    override fun read(element: JsonElement?) {
        element?.asInt?.let { value = it }
    }
}