package com.github.noamm9.commands

abstract class BaseCommand(val name: String) {
    abstract fun CommandNodeBuilder.build()
}