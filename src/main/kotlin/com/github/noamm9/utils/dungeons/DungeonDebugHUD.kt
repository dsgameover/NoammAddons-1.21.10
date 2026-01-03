package com.github.noamm9.utils.dungeons

import com.github.noamm9.utils.dungeons.map.core.RoomState
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

object DungeonDebugHUD {

    @JvmStatic
    fun render(graphics: GuiGraphics) {
        var y = 20
        val x = 10

        // Helper to draw lines using your custom Render2D
        fun draw(text: String, color: Int = 0xFFFFFF) {
            Render2D.drawString(graphics, text, x, y, color = Color(color))
            y += 10
        }

        // 1. HEADER & GLOBAL STATE
        draw("§6§lDUNGEON DEBUGGER", 0xFFAA00)
        draw("Dungeon Started: ${if (DungeonListener.dungeonStarted) "§aYES" else "§cNO"}")
        draw("Dungeon Ended: ${if (DungeonListener.dungeonEnded) "§aYES" else "§cNO"}")
        draw("Current Tick: §7${DungeonListener.currentTime} §8(${DungeonListener.currentTime / 20}s)")
        draw("Door Keys: §e${DungeonListener.doorKeys}")
        draw("Last Opener: §d${DungeonListener.lastDoorOpenner?.name ?: "None"}")

        y += 5 // Spacer

        // 2. TIMESTAMPS (Calculated relative to start)
        draw("§c§lMILESTONES (Ticks)", 0xFF5555)
        fun formatTS(name: String, time: Long?) {
            val display = time?.let { "§f${it}t §8(${it / 20}s)" } ?: "§7N/A"
            draw("$name: $display")
        }
        formatTS("Start", DungeonListener.dungeonStartTime)
        formatTS("Blood Open", DungeonListener.bloodOpenTime)
        formatTS("Watcher Spawn", DungeonListener.watcherFinishSpawnTime)
        formatTS("Watcher Clear", DungeonListener.watcherClearTime)
        formatTS("Boss Entry", DungeonListener.bossEntryTime)
        formatTS("Run End", DungeonListener.dungeonEndTime)

        y += 5 // Spacer

        // 3. TEAMMATES
        draw("§b§lTEAMMATES (${DungeonListener.dungeonTeammates.size})", 0x55FFFF)
        if (DungeonListener.dungeonTeammates.isEmpty()) {
            draw(" §7No teammates detected...")
        }
        else {
            DungeonListener.dungeonTeammates.forEach { player ->
                val status = if (player.isDead) "§c[DEAD]" else "§a[ALIVE]"
                val isSelf = if (player == DungeonListener.thePlayer) " §d(YOU)" else ""
                draw("§f${player.name} §7- ${player.clazz.name} ${player.clazzLvl} $status$isSelf")
            }
        }

        y += 5 // Spacer

        // 4. PUZZLES
        val activePuzzles = DungeonListener.puzzles.count { it != Puzzle.UNKNOWN }
        draw("§d§lPUZZLES ($activePuzzles/${DungeonListener.maxPuzzleCount})", 0xFF55FF)
        if (DungeonListener.puzzles.isEmpty()) {
            draw(" §7No puzzles found yet...")
        }
        else {
            DungeonListener.puzzles.forEachIndexed { index, puzzle ->
                val pColor = when (puzzle.state) {
                    RoomState.GREEN, RoomState.CLEARED -> "§a"
                    RoomState.FAILED -> "§c"
                    RoomState.DISCOVERED -> "§e"
                    else -> "§7"
                }
                // Showing Puzzle name or UNKNOWN index
                val name = if (puzzle == Puzzle.UNKNOWN) "Slot $index" else puzzle.name
                draw(" §7- $pColor$name §8[§7${puzzle.state}§8]")
            }
        }

        y += 5 // Spacer

        // 5. BLESSINGS (Using the enum/reset logic in your listener)
        draw("§a§lBLESSINGS", 0x55FF55)
        var foundBlessing = false
        Blessing.entries.forEach { blessing ->
            if (blessing.current > 0) {
                draw(" §f${blessing.name}: §a${blessing.current}")
                foundBlessing = true
            }
        }
        if (!foundBlessing) draw(" §7No blessings active")
    }
}