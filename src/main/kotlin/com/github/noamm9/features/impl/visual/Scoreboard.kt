package com.github.noamm9.features.impl.visual

import com.github.noamm9.features.Feature
import com.github.noamm9.features.impl.dev.ClickGui
import com.github.noamm9.mixin.IPlayerTabOverlay
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.ui.hud.HudElement
import com.github.noamm9.utils.ChatUtils.formattedText
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
import java.awt.Color

object Scoreboard: Feature("draws a custom scoreboard instead of the vanilla one.") {
    private val hideServerId by ToggleSetting("Hide Server ID").withDescription("Hides the 'm151AM' text from the scoreboard")

    @Suppress("RemoveRedundantQualifierName")
    private val hud = object: HudElement() {
        override val name = "Scoreboard"
        override val toggle get() = Scoreboard.enabled

        override fun draw(ctx: GuiGraphics, example: Boolean): Pair<Float, Float> {
            val scoreboard = mc.level?.scoreboard ?: return 0f to 0f
            val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return 0f to 0f
            val scores = scoreboard.listPlayerScores(objective).sortedByDescending(PlayerScoreEntry::value).take(15)

            if (scores.isEmpty()) return 0f to 0f
            val titleStr = objective.displayName.formattedText

            val lines = scores.map { score ->
                val name = score.ownerName().string
                val team = scoreboard.getPlayersTeam(name)
                val line = PlayerTeam.formatNameForTeam(team, Component.literal(name)).formattedText

                val formattedLine = if (LocationUtils.inSkyblock && hideServerId.value && scores.indexOf(score) == 0)
                    "§7Date: " + line.substringBefore(" §8")
                else line

                formattedLine
            }

            var maxWidth = titleStr.width()
            lines.forEach { maxWidth = maxOf(maxWidth, it.width()) }

            val padding = 8
            val boxWidth = maxWidth + (padding * 2)
            val lineHeights = mc.font.lineHeight + 2
            val boxHeight = (lines.size * lineHeights) + mc.font.lineHeight + (padding * 2)

            val xOffset = - boxWidth.toDouble() - 5
            val yOffset = - (boxHeight.toDouble() / 2)

            Render2D.drawRect(ctx, xOffset, yOffset, boxWidth.toDouble(), boxHeight.toDouble(), Color(15, 15, 15, 190))
            Render2D.drawRect(ctx, xOffset, yOffset, boxWidth.toDouble(), 2.0, ClickGui.accsentColor.value)
            Render2D.drawRect(ctx, xOffset - 1, yOffset - 1, boxWidth + 2.0, boxHeight + 2.0, Color(255, 255, 255, 20))

            Render2D.drawCenteredString(ctx, titleStr, xOffset + (boxWidth / 2), yOffset + padding, Color.WHITE, shadow = false)

            lines.forEachIndexed { index, text ->
                val lineY = yOffset + padding + mc.font.lineHeight + 4 + (index * lineHeights)
                Render2D.drawString(ctx, text, xOffset + padding, lineY, Color.WHITE)
            }

            return boxWidth.toFloat() to boxHeight.toFloat()
        }

        override fun isHovered(mx: Int, my: Int): Boolean {
            val visualWidth = width * scale
            val visualHeight = height * scale

            val left = x - visualWidth - 5
            val right = x

            val top = y - (visualHeight / 2)
            val bottom = y + (visualHeight / 2)

            return mx >= left && mx <= right && my >= top && my <= bottom
        }

        override fun drawBackground(ctx: GuiGraphics, mx: Int, my: Int) {
            val scaledW = width * scale
            val scaledH = height * scale
            val drawX = x - scaledW - 5
            val drawY = y - (scaledH / 2)

            val hovered = mx >= drawX && mx <= drawX + scaledW && my >= drawY && my <= drawY + scaledH
            val borderColor = if (isDragging || hovered) Style.accentColor else Color(255, 255, 255, 40)

            Render2D.drawRect(ctx, drawX, drawY, scaledW.toDouble(), scaledH.toDouble(), Color(10, 10, 10, 150))
            Render2D.drawRect(ctx, drawX, drawY, scaledW.toDouble(), 1.0, borderColor)
            Render2D.drawRect(ctx, drawX, drawY + scaledH - 1, scaledW.toDouble(), 1.0, borderColor)
        }
    }

    override fun init() {
        hud.x = 200f
        hud.y = 200f
        hudElements.add(hud)
    }

    @JvmStatic
    fun drawTablist(graphics: GuiGraphics) {
        val connection = mc.player?.connection ?: return
        val players = connection.listedOnlinePlayers.sortedWith(compareBy({ it.team?.name }, { it.profile.name }))
        if (players.isEmpty()) return

        val tablist = mc.gui.tabList as IPlayerTabOverlay
        val font = mc.font

        val playersPerColumn = 20
        val maxColumns = 4
        val headSize = 8
        val entrySpacing = 1
        val columnGap = 12
        val padding = 8

        val columns = players.chunked(playersPerColumn).take(maxColumns)

        val columnWidths = columns.map { column ->
            column.maxOf { player ->
                val nameText = player.tabListDisplayName?.formattedText ?: player.profile.name
                with(Render2D) { nameText.width() }
            }
        }

        val totalGridWidth = columnWidths.sum() + (columns.size * (headSize + 3)) + ((columns.size - 1) * columnGap)

        val headerLines = tablist.header?.formattedText?.split("\n") ?: emptyList()
        val footerLines = tablist.footer?.formattedText?.split("\n") ?: emptyList()
        var maxExtraWidth = 0
        (headerLines + footerLines).forEach {
            maxExtraWidth = maxOf(maxExtraWidth, with(Render2D) { it.width() })
        }

        val boxWidth = maxOf(totalGridWidth, minOf(maxExtraWidth, 400)) + (padding * 2)
        val rowHeight = font.lineHeight + entrySpacing
        val gridHeight = (columns.maxOfOrNull { it.size } ?: 0) * rowHeight
        val headerHeight = headerLines.size * 9
        val footerHeight = footerLines.size * 9
        val boxHeight = headerHeight + gridHeight + footerHeight + (padding * 2) + 5

        val x = (graphics.guiWidth() / 2) - (boxWidth / 2)
        val y = 10

        Render2D.drawRect(graphics, x, y, boxWidth, boxHeight, Color(15, 15, 15, 200))
        Render2D.drawRect(graphics, x, y, boxWidth, 2, ClickGui.accsentColor.value)
        Render2D.drawRect(graphics, x - 1, y - 1, boxWidth + 2, boxHeight + 2, Color(255, 255, 255, 15))

        var currentY = y + padding

        headerLines.forEach { line ->
            Render2D.drawCenteredString(graphics, line, x + boxWidth / 2, currentY, Color.WHITE, shadow = true)
            currentY += 9
        }
        currentY += 4

        var currentColumnX = x + (boxWidth - totalGridWidth) / 2

        columns.forEachIndexed { colIndex, column ->
            val thisColumnWidth = columnWidths[colIndex]

            column.forEachIndexed { rowIndex, player ->
                val entryY = currentY + (rowIndex * rowHeight)

                Render2D.drawPlayerHead(graphics, currentColumnX, entryY, headSize, player.skin.body.id())

                val name = player.tabListDisplayName?.formattedText ?: player.profile.name
                Render2D.drawString(graphics, name, currentColumnX + headSize + 3, entryY, Color.WHITE)
            }

            currentColumnX += (headSize + 3 + thisColumnWidth + columnGap)
        }

        currentY += gridHeight + 6

        footerLines.forEach { line ->
            Render2D.drawCenteredString(graphics, line, x + boxWidth / 2, currentY, Color.WHITE, shadow = true)
            currentY += 9
        }
    }
}