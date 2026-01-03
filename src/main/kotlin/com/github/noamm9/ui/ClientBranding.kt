package com.github.noamm9.ui

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.features.impl.dev.ClickGui
import com.github.noamm9.interfaces.ITabList
import com.github.noamm9.utils.ChatUtils.formattedText
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
import java.awt.Color

object ClientBranding {
    @JvmStatic
    fun drawScoreboard(graphics: GuiGraphics, objective: Objective) {
        val scoreboard = objective.scoreboard
        val scores = scoreboard.listPlayerScores(objective).sortedByDescending(PlayerScoreEntry::value).take(15)

        if (scores.isEmpty()) return
        val titleStr = objective.displayName.formattedText

        val lines = scores.map { score ->
            val name = score.ownerName().string
            val team = scoreboard.getPlayersTeam(name)
            PlayerTeam.formatNameForTeam(team, Component.literal(name)).formattedText
        }

        var maxWidth = with(Render2D) { titleStr.width() }
        lines.forEach { line ->
            val lineWidth = with(Render2D) { line.width() }
            maxWidth = maxOf(maxWidth, lineWidth)
        }

        val padding = 8
        val boxWidth = maxWidth + (padding * 2)
        val lineSpacing = 2
        val lineHeights = mc.font.lineHeight + lineSpacing
        val boxHeight = (lines.size * lineHeights) + mc.font.lineHeight + (padding * 2)
        val x = graphics.guiWidth() - boxWidth - 5
        val y = (graphics.guiHeight() / 2) - (boxHeight / 2)

        Render2D.drawRect(graphics, x, y, boxWidth, boxHeight, Color(15, 15, 15, 190))
        Render2D.drawRect(graphics, x, y, boxWidth, 2, ClickGui.accsentColor.value)
        Render2D.drawRect(graphics, x - 1, y - 1, boxWidth + 2, boxHeight + 2, Color(255, 255, 255, 20))

        Render2D.drawCenteredString(graphics, titleStr, x + (boxWidth / 2), y + padding, Color.WHITE, shadow = false)

        lines.forEachIndexed { index, text ->
            val lineY = y + padding + mc.font.lineHeight + 4 + (index * lineHeights)
            Render2D.drawString(graphics, text, x + padding, lineY, Color.WHITE)
        }
    }

    @JvmStatic
    fun drawTablist(graphics: GuiGraphics) {
        val connection = mc.player?.connection ?: return
        val players = connection.listedOnlinePlayers.sortedWith(compareBy({ it.team?.name }, { it.profile.name }))
        if (players.isEmpty()) return

        val tablist = mc.gui.tabList as ITabList
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

                Render2D.drawPlayerHead(graphics, currentColumnX, entryY, headSize, player.profile.id)

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