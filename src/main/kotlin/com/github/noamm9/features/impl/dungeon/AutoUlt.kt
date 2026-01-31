package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.PlayerUtils
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.dungeons.enums.Classes
import com.github.noamm9.utils.location.LocationUtils

object AutoUlt: Feature("Automatically uses your dungeon class ultimate when needed") {
    private class UltMessage(val msg: String, val classes: List<Classes>, val floor: Int)

    // todo offload to json?
    private val UltMessages = listOf(
        UltMessage(
            msg = "⚠ Maxor is enraged! ⚠",
            classes = listOf(Classes.Healer, Classes.Tank),
            floor = 7
        ),
        UltMessage(
            msg = "[BOSS] Goldor: You have done it, you destroyed the factory…",
            classes = listOf(Classes.Healer, Classes.Tank),
            floor = 7
        ),
        UltMessage(
            msg = "[BOSS] Sadan: My giants! Unleashed!",
            classes = listOf(Classes.Healer, Classes.Tank, Classes.Archer, Classes.Berserk, Classes.Mage),
            floor = 6
        ),
        UltMessage(
            msg = "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.",
            classes = listOf(Classes.Healer, Classes.Tank),
            floor = 5
        )
    )

    override fun init() {
        register<ChatMessageEvent> {
            if (! LocationUtils.inBoss) return@register
            val msg = event.unformattedText
            val matchingMessage = UltMessages.find {
                it.msg == msg && it.floor == LocationUtils.dungeonFloorNumber
            } ?: return@register

            if (DungeonListener.thePlayer?.clazz !in matchingMessage.classes) return@register
            PlayerUtils.useDungeonClassAbility(true)
            ChatUtils.modMessage("Used Ultimate!")
        }
    }
}