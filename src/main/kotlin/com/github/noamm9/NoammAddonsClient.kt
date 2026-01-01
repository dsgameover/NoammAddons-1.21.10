package com.github.noamm9

import com.github.noamm9.event.EventDispatcher
import com.github.noamm9.features.FeatureManager
import com.github.noamm9.ui.clickgui.ClickGuiScreen
import com.github.noamm9.utils.LocationUtils
import com.github.noamm9.utils.ThreadUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

object NoammAddonsClient : ClientModInitializer {
    var screen: Screen? = null

    override fun onInitializeClient() {
        EventDispatcher.init()
        LocationUtils.init()
        ThreadUtils.init()
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
                ClientCommandManager.literal("na").executes {
                    screen = ClickGuiScreen
                    1
                }
            )
        }
    }
}