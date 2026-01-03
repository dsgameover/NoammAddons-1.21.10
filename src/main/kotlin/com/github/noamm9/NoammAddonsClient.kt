package com.github.noamm9

import com.github.noamm9.event.EventDispatcher
import com.github.noamm9.features.FeatureManager
import com.github.noamm9.features.impl.dungeon.StarMobEsp
import com.github.noamm9.ui.clickgui.ClickGuiScreen
import com.github.noamm9.ui.hud.HudEditorScreen
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ThreadUtils
import com.github.noamm9.utils.dungeons.DungeonListener
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screens.Screen

object NoammAddonsClient: ClientModInitializer {
    var screen: Screen? = null

    override fun onInitializeClient() {
        EventDispatcher.init()
        ThreadUtils.init()
        DungeonListener.init()

        FeatureManager.registerFeatures()

        ClientTickEvents.START_CLIENT_TICK.register {
            it.execute {
                if (screen != null) {
                    it.setScreen(screen)
                    screen = null
                }
            }
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("na")
                    .executes {
                        screen = ClickGuiScreen
                        1
                    }
                    .then(
                        ClientCommandManager.literal("hud").executes {
                            screen = HudEditorScreen
                            1
                        }
                    )
                    .then(
                        ClientCommandManager.literal("help").executes {
                            ChatUtils.chat("§6§lNoammAddons§r\n§e/na §7- Open GUI\n§e/na hud §7- Open HUD Editor")
                            1
                        }
                    )
            )

            dispatcher.register(
                ClientCommandManager.literal("test").executes {
                    ThreadUtils.scheduledTask(25) {
                        ChatUtils.modMessage("hi")
                    }

                    StarMobEsp.checked.clear()
                    StarMobEsp.starMobs.clear()
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
}