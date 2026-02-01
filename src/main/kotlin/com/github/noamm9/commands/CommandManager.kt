package com.github.noamm9.commands

import com.github.noamm9.NoammAddons
import io.github.classgraph.ClassGraph
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object CommandManager {
    val commands = mutableSetOf<BaseCommand>()

    fun registerAll() {
        val scanResult = ClassGraph()
            .enableAllInfo()
            .acceptPackages("com.github.noamm9")
            .ignoreClassVisibility()
            .scan()

        scanResult.use { result ->
            val commandClasses = result.getSubclasses(BaseCommand::class.java.name)
            NoammAddons.logger.info("CommandManager found ${commandClasses.size} commands.")

            ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
                commandClasses.forEach { classInfo ->
                    try {
                        val instance = classInfo.loadClass().getDeclaredField("INSTANCE").get(null) as? BaseCommand

                        instance?.let { command ->
                            val root = ClientCommandManager.literal(command.name)
                            CommandNodeBuilder(root).apply { with(command) { build() } }
                            dispatcher.register(root)
                            commands.add(command)
                            NoammAddons.logger.debug("Registered command: /${command.name}")
                        }
                    } catch (e: Exception) {
                        NoammAddons.logger.error("Failed to register command: ${classInfo.name}", e)
                    }
                }
            }
        }
    }
}