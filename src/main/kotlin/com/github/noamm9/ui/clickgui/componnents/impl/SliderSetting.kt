package com.github.noamm9.ui.clickgui.componnents.impl

import com.github.noamm9.config.Savable
import com.github.noamm9.ui.clickgui.componnents.Setting
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.utils.Animation
import com.github.noamm9.utils.NumbersUtils.minus
import com.github.noamm9.utils.NumbersUtils.plus
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color
import kotlin.math.abs

class SliderSetting(name: String, value: Number, val min: Number, val max: Number, val step: Number): Setting<Number>(name, value), Savable {
    private var dragging = false
    private val hoverAnim = Animation(200)
    private val sliderAnim = Animation(250, ((value - min) / (max - min)).toFloat())

    override fun draw(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        val isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
        val target = ((value - min) / (max - min)).toFloat()

        if (dragging) {
            val pct = ((mouseX - (x + 8f)) / (width - 16f)).coerceIn(0f, 1f)
            value = (min + (pct * (max - min))) / step.toDouble() * step.toDouble()
            if (abs(sliderAnim.value - target) < 0.05f) sliderAnim.set(target)
        }
        hoverAnim.update(if (isHovered || dragging) 1f else 0f)
        sliderAnim.update(target)

        Style.drawBackground(ctx, x, y, width, 20f)
        Style.drawHoverBar(ctx, x, y, 20f, hoverAnim.value)
        Style.drawNudgedText(ctx, name, x + 8f, y + 2f, hoverAnim.value)

        val valStr = if (step.toDouble() >= 1) value.toInt().toString() else "%.2f".format(value.toDouble())
        Render2D.drawString(ctx, valStr, x + width - valStr.width() - 8f, y + 2f, Color(180, 180, 180))

        Style.drawSlider(ctx, x + 8f, y + 14f, width - 16f, sliderAnim.value, hoverAnim.value, Style.accentColor)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            dragging = true
            return true
        }
        return false
    }

    override fun mouseReleased(button: Int) {
        dragging = false
    }

    override fun write(): JsonElement = JsonPrimitive(value)
    override fun read(element: JsonElement?) {
        element?.asNumber?.let { value = it }
    }
}