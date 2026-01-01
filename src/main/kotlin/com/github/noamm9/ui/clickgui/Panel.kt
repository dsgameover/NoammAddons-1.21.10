package com.github.noamm9.ui.clickgui

import com.github.noamm9.features.FeatureManager
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

class Panel(val category: CategoryType, var x: Int, var y: Int) {
    private val features = FeatureManager.getFeaturesByCategory(category)

    private val width = 100
    private val headerHeight = 18
    private var dragging = false
    private var dragX = 0
    private var dragY = 0

    fun render(context: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - dragX
            y = mouseY - dragY
        }

        Render2D.drawRect(context, x, y, width, headerHeight, Color(40, 40, 45, 255))
        Render2D.drawString(context, category.name, x + width / 2 - category.name.width() / 2, y + 5)

        var currentY = y + headerHeight

        features.forEach { feature ->
            val isHovered = mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 14

            val bgColor = when {
                feature.enabled -> if (isHovered) Color(0, 150, 255) else Color(0, 120, 255)
                isHovered -> Color(60, 60, 65)
                else -> Color(40, 40, 45, 255)
            }

            Render2D.drawRect(context, x, currentY, width, 14, bgColor)
            Render2D.drawString(context, feature.name, x + width / 2 - feature.name.width() / 2, currentY + 3)

            currentY += 14
        }

        Render2D.drawRect(context, x, currentY, width, 2, Color(40, 40, 45))
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight) {
            if (button == 0) {
                dragging = true
                dragX = (mouseX - x).toInt()
                dragY = (mouseY - y).toInt()
            }
        }

        var currentY = y + headerHeight
        FeatureManager.getFeaturesByCategory(category).forEach { feature ->
            if (mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 14) {
                if (button == 0) feature.toggle() // Left click to toggle
                // if (button == 1) feature.expanded = !feature.expanded // Right click for settings
            }
            currentY += 14
        }
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (button == 0) dragging = false
    }
}