package com.github.noamm9

import com.github.noamm9.config.PogObject
import com.github.noamm9.event.EventBus
import com.github.noamm9.event.EventDispatcher
import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.features.FeatureManager
import com.github.noamm9.ui.clickgui.ClickGuiScreen
import com.github.noamm9.ui.hud.HudEditorScreen
import com.github.noamm9.utils.*
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.network.WebUtils
import com.github.noamm9.utils.network.data.ElectionData
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object NoammAddonsClient: ClientModInitializer {
    val cacheData = PogObject("cacheData", mutableMapOf<String, Any>())
    val debugFlags = mutableSetOf<String>()
    var screen: Screen? = null

    var electionData = ElectionData.empty

    override fun onInitializeClient() {
        DataDownloader.downloadData()

        EventDispatcher.init()
        ThreadUtils.init()
        DungeonListener.init()
        ServerUtils.init()
        ActionBarParser.init()
        PartyUtils.init()
        ChatUtils.init()

        this.initNetworkLoop()

        FeatureManager.registerFeatures()

        ClientTickEvents.START_CLIENT_TICK.register {
            it.execute {
                if (screen != null) {
                    it.setScreen(screen)
                    screen = null
                }
            }
        }

        // temp until i make somehting better chz this is cancer
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("na")
                    .executes {
                        screen = ClickGuiScreen
                        1
                    }
                    .then(
                        ClientCommandManager.literal("help").executes {
                            ChatUtils.chat("§6§lNoammAddons§r\n§e/na §7- Open GUI\n§e/na hud §7- Open HUD Editor")
                            1
                        }
                    )
                    .then(
                        ClientCommandManager.literal("hud").executes {
                            screen = HudEditorScreen
                            1
                        }
                    )
                    .then(
                        ClientCommandManager.literal("debug").then(ClientCommandManager.argument("flag", StringArgumentType.word())
                            .executes { context ->
                                val flag = StringArgumentType.getString(context, "flag")

                                if (flag.isNotBlank()) {
                                    if (flag in debugFlags) {
                                        debugFlags.remove(flag)
                                        ChatUtils.modMessage("§cRemoved §fdebug flag: §b$flag")
                                    }
                                    else {
                                        debugFlags.add(flag)
                                        ChatUtils.modMessage("§aAdded §fdebug flag: §b$flag")
                                    }
                                }

                                1
                            }
                        ).executes {
                            ChatUtils.modMessage("§7Current flags: §f${debugFlags.joinToString(", ")}")
                            1
                        }
                    )
                    .then(ClientCommandManager.literal("sim").then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes { context ->
                            val msg = StringArgumentType.getString(context, "message")
                            if (msg.isBlank()) return@executes 0

                            ChatUtils.modMessage(msg)
                            EventBus.post(ChatMessageEvent(Component.literal(msg)))

                            1 // Success
                        }
                    ))
            )

            dispatcher.register(
                ClientCommandManager.literal("test").executes {
                    ThreadUtils.scheduledTask(25) {
                        ChatUtils.modMessage("hi")
                    }
                    1
                }
            )

            dispatcher.register(
                ClientCommandManager.literal("d").executes {
                    ChatUtils.sendCommand("warp dungeon_hub")
                    1
                }
            )

        }
    }

    private fun initNetworkLoop() = ThreadUtils.loop(600_000) {
        WebUtils.get<ElectionData>("https://api.noammaddons.workers.dev/mayor").onSuccess {
            electionData = it
        }
    }
}