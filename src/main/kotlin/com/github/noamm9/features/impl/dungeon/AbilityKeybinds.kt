package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.KeyboardEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.KeybindSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.section
import com.github.noamm9.ui.clickgui.componnents.showIf
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.network.PacketUtils.send
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ITEM
import org.lwjgl.glfw.GLFW

object AbilityKeybinds: Feature("Allows you do use your dungeon class ult/ability with a keybind") {
    private val classUltimate by ToggleSetting("Class Ultimate", true)
    private val classAbility by ToggleSetting("Class Ability", true)
    private val ultKeybind by KeybindSetting("Ultimate Keybind").showIf { classUltimate.value }.section("keybinds")
    private val abilityKeybind by KeybindSetting("Ability Keybind").showIf { classAbility.value }

    override fun init() {
        register<KeyboardEvent> {
            if (! LocationUtils.inDungeon || ! DungeonListener.dungeonStarted) return@register
            if (event.scanCode != GLFW.GLFW_PRESS) return@register
            if (mc.screen != null) return@register

            if (classUltimate.value && ultKeybind.value == event.key) {
                event.isCanceled = true
                return@register useDungeonClassAbility(true)
            }

            if (classAbility.value && abilityKeybind.value == event.key) {
                event.isCanceled = true
                return@register useDungeonClassAbility(false)
            }
        }
    }

    private fun useDungeonClassAbility(dropOne: Boolean) {
        val action = if (dropOne) DROP_ITEM else DROP_ALL_ITEMS
        ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN).send()
    }
}