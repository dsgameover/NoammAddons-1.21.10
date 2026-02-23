package com.github.noamm9.commands.impl

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.NoammAddons.priceData
import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.PlayerUtils
import com.github.noamm9.utils.dungeons.map.utils.ScanUtils
import com.github.noamm9.utils.items.ItemUtils.skyblockId

object TestCommand: BaseCommand("test") {
    override fun CommandNodeBuilder.build() {
        literal("relative") {
            runs {
                val room = ScanUtils.currentRoom ?: return@runs
                ChatUtils.chat(ScanUtils.getRelativeCoord(PlayerUtils.getSelectionBlock() !!, room.centerPos, room.rotation ?: return@runs))
            }
        }

        literal("gui") {
            runs {
                mc.screen?.onClose()
                //    NoammAddons.screen = KitchenSinkScreen()
            }
        }

        runs {
            ChatUtils.chat("${mc.player?.mainHandItem.skyblockId}: ${priceData[mc.player?.mainHandItem.skyblockId]}")
        }
    }
}