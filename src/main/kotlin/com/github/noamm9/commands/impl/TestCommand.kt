package com.github.noamm9.commands.impl

import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder

object TestCommand: BaseCommand("test") {
    override fun CommandNodeBuilder.build() {
        runs {

        }
    }
}