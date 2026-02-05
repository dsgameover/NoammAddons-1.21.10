package com.github.noamm9.features.impl.dungeon.waypoints

import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import java.awt.Color

class WaypointEditorGui(
    private val roomName: String,
    private val absolutePos: BlockPos,
    private val relativePos: BlockPos,
    private val initialState: DungeonWaypoints.DungeonWaypoint? = null
): Screen(Component.literal("Waypoint Editor")) {

    private var filled = true
    private var outline = true
    private var phase = true
    private var colorIndex = 0

    private val colors = listOf(
        Color.GREEN, Color.RED, Color.BLUE, Color.CYAN,
        Color.MAGENTA, Color.YELLOW, Color.WHITE, Color.BLACK, Color.ORANGE
    )

    private val colorNames = listOf(
        "Green", "Red", "Blue", "Cyan",
        "Magenta", "Yellow", "White", "Black", "Orange"
    )

    private val bodyBg = Color(15, 15, 15, 200)
    private val headerBg = Color(20, 20, 20, 255)

    override fun init() {
        if (initialState != null) {
            filled = initialState.filled
            outline = initialState.outline
            phase = initialState.phase

            val initialRgb = initialState.color.rgb and 0xFFFFFF
            colorIndex = colors.indexOfFirst { (it.rgb and 0xFFFFFF) == initialRgb }
            if (colorIndex == - 1) colorIndex = 0
        }

        val centerX = this.width / 2
        val centerY = this.height / 2
        val btnWidth = 200
        val btnHeight = 20
        val startY = centerY - 40
        val spacing = 24

        addStyledButton(centerX - 100, startY, btnWidth, btnHeight, "Filled: ${colorBoolean(filled)}") { btn ->
            filled = ! filled
            btn.message = Component.literal("Filled: ${colorBoolean(filled)}")
        }

        addStyledButton(centerX - 100, startY + spacing, btnWidth, btnHeight, "Outline: ${colorBoolean(outline)}") { btn ->
            outline = ! outline
            btn.message = Component.literal("Outline: ${colorBoolean(outline)}")
        }

        addStyledButton(centerX - 100, startY + spacing * 2, btnWidth, btnHeight, "Phase (See-Thru): ${colorBoolean(phase)}") { btn ->
            phase = ! phase
            btn.message = Component.literal("Phase (See-Thru): ${colorBoolean(phase)}")
        }

        addStyledButton(
            centerX - 100, startY + 75, 200, 20, "&fColor: ${colorNames[colorIndex]}", colorProvider = { colors[colorIndex] }) { btn ->
            colorIndex ++
            if (colorIndex >= colors.size) colorIndex = 0
            btn.message = Component.literal("Color: ${colorNames[colorIndex]}")
        }

        addStyledButton(centerX - 100, startY + spacing * 5, 98, btnHeight, "§aSave") {
            if (! filled && ! outline) {
                ChatUtils.modMessage("§cBoth Filled and Outline cannot be false")
                return@addStyledButton
            }

            val baseColor = colors[colorIndex]
            val finalColor = if (filled) Color(baseColor.red, baseColor.green, baseColor.blue, 60) else baseColor

            DungeonWaypoints.saveWaypoint(
                absolutePos, relativePos,
                roomName, finalColor,
                filled, outline, phase
            )
            onClose()
        }

        addStyledButton(centerX + 2, startY + spacing * 5, 98, btnHeight, "§cCancel") {
            onClose()
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val centerX = this.width / 2
        val centerY = this.height / 2

        val panelWidth = 220
        val panelHeight = 210
        val panelX = centerX - panelWidth / 2
        val panelY = centerY - 100

        Render2D.drawRect(context, panelX, panelY, panelWidth, panelHeight, bodyBg)
        Render2D.drawRect(context, panelX, panelY, panelWidth, 25, headerBg)

        Render2D.drawRect(context, panelX, panelY, panelWidth, 2, Style.accentColor)
        Render2D.drawRect(context, panelX, panelY + panelHeight - 1, panelWidth, 1, Style.accentColor)

        Render2D.drawCenteredString(context, "§l${if (initialState == null) "New" else "Edit"} Waypoint", centerX, panelY + 8)

        Render2D.drawCenteredString(context, "§b$roomName", centerX, panelY + 30)

        Render2D.drawCenteredString(context, "§7[${absolutePos.x}, ${absolutePos.y}, ${absolutePos.z}]", centerX, panelY + 43)

        super.render(context, mouseX, mouseY, partialTicks)
    }

    private fun addStyledButton(
        x: Int, y: Int, w: Int, h: Int,
        text: String,
        colorProvider: (() -> Color)? = null,
        action: (Button) -> Unit
    ): Button {
        val customBtn = object: Button(x, y, w, h, Component.literal(text), {
            action(it)
            it.playDownSound(minecraft !!.soundManager)
        }, DEFAULT_NARRATION) {
            override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
                val bgColor = if (isHovered) Color(40, 40, 40, 200) else Color(30, 30, 30, 200)
                Render2D.drawRect(context, x, y, width, height, bgColor)

                val specificColor = colorProvider?.invoke()

                val borderColor = if (isHovered) Style.accentColor else specificColor ?: Color(60, 60, 60)
                val textColor = if (isHovered) Style.accentColor else specificColor ?: Color.WHITE

                Render2D.drawRect(context, x, y, width, 1, borderColor)
                Render2D.drawRect(context, x, y + height - 1, width, 1, borderColor)
                Render2D.drawRect(context, x, y, 1, height, borderColor)
                Render2D.drawRect(context, x + width - 1, y, 1, height, borderColor)

                Render2D.drawCenteredString(context, message.string, x + width / 2, y + (height - 8) / 2, textColor)
            }
        }

        addRenderableWidget(customBtn)
        return customBtn
    }

    private fun colorBoolean(bl: Boolean) = if (bl) "&atrue" else "&cfalse"
}