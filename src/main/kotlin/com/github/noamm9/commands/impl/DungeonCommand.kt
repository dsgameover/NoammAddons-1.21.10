package com.github.noamm9.commands.impl

import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder
import com.github.noamm9.utils.ChatUtils

object DungeonCommand: BaseCommand("d") {
    override fun CommandNodeBuilder.build() {
        runs {
            ChatUtils.sendCommand("warp dungeon_hub")
        }
    }
}