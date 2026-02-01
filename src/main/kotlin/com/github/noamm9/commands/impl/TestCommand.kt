package com.github.noamm9.commands.impl

import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ThreadUtils

object TestCommand: BaseCommand("test") {
    override fun CommandNodeBuilder.build() {
        runs {
            ThreadUtils.scheduledTask(25) {
                ChatUtils.modMessage("hi")
                ChatUtils.showTitle("Example", "Subtitle")
            }
        }
    }
}