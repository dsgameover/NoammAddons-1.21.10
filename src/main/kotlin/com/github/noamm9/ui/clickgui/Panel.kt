package com.github.noamm9.ui.clickgui

import com.github.noamm9.features.FeatureManager
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.utils.Animation
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

class Panel(val category: CategoryType, var x: Int, var y: Int) {
    private val features = FeatureManager.getFeaturesByCategory(category)

    private val openAnim = Animation(150)
    var collapsed = false

    private val width = 110
    private val headerHeight = 22
    private val buttonHeight = 16
    private var dragging = false
    private var dragX = 0
    private var dragY = 0

    private val headerBg = Color(20, 20, 20, 230)
    private val bodyBg = Color(15, 15, 15, 180)
    private val hoverColor = Color(255, 255, 255, 30)

    fun render(context: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - dragX
            y = mouseY - dragY
        }

        val filteredFeatures = features.filter {
            it.name.contains(ClickGuiScreen.searchQuery, ignoreCase = true)
        }

        if (filteredFeatures.isEmpty() && ClickGuiScreen.searchQuery.isNotEmpty()) return

        openAnim.update(if (collapsed) 0f else 1f)

        Render2D.drawRect(context, x, y, width, headerHeight, headerBg)
        Render2D.drawRect(context, x, y, width, 2, Style.accentColor)

        val icon = if (collapsed) "+" else "-"
        Render2D.drawString(context, icon, x + width - 12, y + 7, Color.GRAY)

        Render2D.drawCenteredString(context, "§l${category.name}", x + width / 2, y + 7)

        if (openAnim.value > 0.01f) {
            var currentY = y + headerHeight

            val scissorHeight = (filteredFeatures.size * buttonHeight * openAnim.value).toInt()
            context.enableScissor(x, y + headerHeight, x + width, y + headerHeight + scissorHeight)

            filteredFeatures.forEach { feature ->
                val isHovered = mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + buttonHeight

                Render2D.drawRect(context, x, currentY, width, buttonHeight, bodyBg)

                if (feature.enabled) {
                    Render2D.drawRect(context, x, currentY, width, buttonHeight, Style.accentColor.withAlpha(100))
                    Render2D.drawRect(context, x, currentY, 2, buttonHeight, Style.accentColor)
                }

                if (isHovered) {
                    Render2D.drawRect(context, x, currentY, width, buttonHeight, hoverColor)
                }

                Render2D.drawCenteredString(context, feature.name, x + width / 2, currentY + 4)

                if (isHovered && ClickGuiScreen.selectedFeature == null) {
                    TooltipManager.hover(feature.description, mouseX, mouseY)
                }

                currentY += buttonHeight
            }
            context.disableScissor()
        }
    }

    fun isMouseOverHeader(mx: Double, my: Double): Boolean {
        return mx >= x && mx <= x + width && my >= y && my <= y + headerHeight
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (ClickGuiScreen.selectedFeature != null) return

        if (isMouseOverHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true
                dragX = (mouseX - x).toInt()
                dragY = (mouseY - y).toInt()
            }
            else if (button == 1) {
                collapsed = ! collapsed
                Style.playClickSound(if (collapsed) 0.8f else 1.1f)
            }
            return
        }

        if (collapsed) return

        var currentY = y + headerHeight
        val filteredFeatures = features.filter { it.name.contains(ClickGuiScreen.searchQuery, ignoreCase = true) }

        filteredFeatures.forEach { feature ->
            if (mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + buttonHeight) {
                if (button == 0) {
                    feature.toggle()
                }
                else if (button == 1) {
                    ClickGuiScreen.selectFeature(feature)
                }
            }
            currentY += buttonHeight
        }
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (button == 0) dragging = false
    }
}