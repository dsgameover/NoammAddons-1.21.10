package com.github.noamm9.commands.impl

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.NoammAddons.priceData
import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.items.ItemUtils.skyblockId

object TestCommand: BaseCommand("test") {
    override fun CommandNodeBuilder.build() {
        runs {
            /*
            val room = ScanUtils.currentRoom ?: return@runs
            ChatUtils.chat(ScanUtils.getRelativeCoord(PlayerUtils.getSelectionBlock() !!, room.corner ?: return@runs, room.rotation ?: return@runs))*/

            ChatUtils.chat("${mc.player?.mainHandItem.skyblockId}: ${priceData[mc.player?.mainHandItem.skyblockId]}")
        }
    }
}