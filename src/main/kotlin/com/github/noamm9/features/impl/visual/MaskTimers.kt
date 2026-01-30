package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.section
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ChatUtils.unformattedText
import com.github.noamm9.utils.NumbersUtils.toFixed
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import net.minecraft.world.entity.EquipmentSlot

object MaskTimers : Feature("Displays a Tick accurate Display") {
    private val onlyInDungeon by ToggleSetting("Dungeons Only", true)
    private val procNotification by ToggleSetting("Proc Notification").section("Jason Derulo")
    private val comeBackNotification by ToggleSetting("Comeback Notification")

    // Data structure to hold everything related to a specific "Mask" or "Pet"
    private class MaskTracker(
        val name: String,
        val colorCode: String,
        val cooldownMs: Int,
        val regex: Regex,
        val checkActive: () -> Boolean
    ) {
        var remainingMs = 0
        var isActive = false
        var wasOnCooldown = false

        fun reset() {
            remainingMs = 0
            isActive = false
            wasOnCooldown = false
        }
    }

    private val trackers = listOf(
        MaskTracker("Bonzo", "&9", 180000, Regex("Your .+onzo's Mask saved your life!")) {
            mc.player?.getItemBySlot(EquipmentSlot.HEAD)?.skyblockId?.contains("BONZO_MASK") == true
        },
        MaskTracker("Spirit", "&5", 30000, Regex("Second Wind Activated! Your Spirit Mask saved your life!")) {
            mc.player?.getItemBySlot(EquipmentSlot.HEAD)?.skyblockId?.contains("SPIRIT_MASK") == true
        },
        MaskTracker("Phoenix", "&c", 60000, Regex("Your Phoenix Pet saved you from certain death!")) {
            cacheData.getData()["pet"]?.toString()?.contains("Phoenix", ignoreCase = true) == true
        }
    )

    private val hudElement = hudElement("Mask Timers") { context, example ->
        if (onlyInDungeon.value && !LocationUtils.inDungeon && !example) return@hudElement 0f to 0f

        val lines = trackers.map { tracker ->
            val displayCooldown = if (example) (tracker.cooldownMs / 2) else tracker.remainingMs
            val arrow = if (tracker.isActive) "&a>" else "&c>"

            if (displayCooldown > 0) {
                "${tracker.colorCode}${tracker.name} $arrow &e${(displayCooldown / 1000.0).toFixed(2)}"
            } else {
                "${tracker.colorCode}${tracker.name} $arrow &aReady"
            }
        }

        lines.forEachIndexed { i, text ->
            if(LocationUtils.inSkyblock) Render2D.drawString(context, text, 0, i * 10)
        }

        return@hudElement lines.maxOf { it.width().toFloat() } to lines.size * 10f
    }

    override fun init() {
        // Update Equipment/Pet state
        register<TickEvent.Start> {
            if (!LocationUtils.inSkyblock || (onlyInDungeon.value && !LocationUtils.inDungeon)) return@register
            trackers.forEach { it.isActive = it.checkActive() }
        }

        // Handle Cooldown Logic
        register<TickEvent.Server> {
            if (!LocationUtils.inSkyblock) return@register

            trackers.forEach { tracker ->
                if (tracker.remainingMs > 0) {
                    tracker.remainingMs -= 50
                    tracker.wasOnCooldown = true
                } else if (tracker.wasOnCooldown) {
                    tracker.wasOnCooldown = false
                    if (comeBackNotification.value) {
                        ChatUtils.showTitle("${tracker.colorCode}${tracker.name} is Ready!")
                    }
                }
            }
        }

        // Handle Proc Messages
        register<ChatMessageEvent> {
            if (!LocationUtils.inSkyblock || (onlyInDungeon.value && !LocationUtils.inDungeon)) return@register
            val msg = event.unformattedText

            trackers.forEach { tracker ->
                if (msg.matches(tracker.regex)) {
                    tracker.remainingMs = tracker.cooldownMs
                    if (procNotification.value) {
                        ChatUtils.showTitle("${tracker.colorCode}${tracker.name} Procced!")
                    }
                }
            }
        }

        register<WorldChangeEvent> {
            trackers.forEach { it.reset() }
        }
    }
}