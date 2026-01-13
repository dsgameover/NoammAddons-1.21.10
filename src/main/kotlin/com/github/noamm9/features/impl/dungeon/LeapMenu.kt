package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.ContainerEvent
import com.github.noamm9.event.impl.EntityCheckRenderEvent
import com.github.noamm9.event.impl.ScreenEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.KeybindSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.TextInputSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.utils.Resolution
import com.github.noamm9.utils.ButtonType
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ChatUtils.unformattedText
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.GuiUtils
import com.github.noamm9.utils.MathUtils
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.dungeons.DungeonPlayer
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW
import java.awt.Color

object LeapMenu: Feature("Custom Leap Menu and leap message") {
    val customLeapMenu by ToggleSetting("Leap Menu", false).section("Custom Menu")
    val scale by SliderSetting("Menu Scale", 50f, 30, 100, 1).showIf { customLeapMenu.value }
    val showLastDoorOpener by ToggleSetting("Show Last Door Opener", false).showIf { customLeapMenu.value }
    val tintDeadPlayers by ToggleSetting("Tint Dead Players", true).showIf { customLeapMenu.value }

    val leapKeybinds by ToggleSetting("Leap Keybinds").showIf { customLeapMenu.value }.section("Leap Keybinds")
    val key1 by KeybindSetting("Slot 1", GLFW.GLFW_KEY_1).showIf { leapKeybinds.value }
    val key2 by KeybindSetting("Slot 2", GLFW.GLFW_KEY_2).showIf { leapKeybinds.value }
    val key3 by KeybindSetting("Slot 3", GLFW.GLFW_KEY_3).showIf { leapKeybinds.value }
    val key4 by KeybindSetting("Slot 4", GLFW.GLFW_KEY_4).showIf { leapKeybinds.value }

    private val announceSpiritLeaps by ToggleSetting("Announce Leap", true).section("Extras")
    private val leapMsg by TextInputSetting("Leap Message", "ILY ❤ {name}")
        .withDescription("replaces {name} with the player name")
        .showIf { announceSpiritLeaps.value }

    private val hideAfterLeap by ToggleSetting("Hide Players")
        .withDescription("Hides players for a certain amount of time after you leap")
    private val hideTime by SliderSetting("Hide Time", 3.5, 0.5, 5.0, 0.1).showIf { hideAfterLeap.value }


    data class LeapMenuPlayer(val slotIndex: Int, val player: DungeonPlayer)

    private val players = MutableList<LeapMenuPlayer?>(4) { null }
    private val leapRegex = Regex("^You have teleported to (.+)!$")
    private val playerPattern = Regex("(?:\\[.+?] )?(?<name>\\w+)")
    private var shouldHide: Long = 0


    override fun init() {
        register<ChatMessageEvent> {
            if (! enabled) return@register
            leapRegex.find(event.unformattedText)?.destructured?.component1()?.let { name ->
                if (announceSpiritLeaps.value) {
                    val msg = leapMsg.value.replace("{name}", name)
                    ChatUtils.sendPartyMessage(msg)
                }

                if (hideAfterLeap.value) {
                    shouldHide = System.currentTimeMillis() + ((hideTime.value * 1000L).toLong())
                }
            }
        }

        register<EntityCheckRenderEvent> {
            if (System.currentTimeMillis() > shouldHide) return@register
            if (event.entity !is Player) return@register
            if (event.entity == mc.player) return@register
            event.isCanceled = true
        }

        register<ScreenEvent.PreRender> {
            if (! customLeapMenu.value) return@register
            if (! inSpiritLeap(event.screen)) return@register
            event.isCanceled = true

            updateLeapMenu()

            Resolution.refresh()
            Resolution.apply(event.context)

            if (players.filterNotNull().isEmpty()) {
                Render2D.drawCenteredString(event.context, "§4§lNo players found", Resolution.width / 2, Resolution.height / 2)
                return@register Resolution.restore(event.context)
            }

            val userScale = (scale.value.toFloat() / 100f) * 2.0f
            val screenWidth = Resolution.width / userScale
            val screenHeight = Resolution.height / userScale

            val pose = event.context.pose()
            pose.pushMatrix()
            pose.scale(userScale)

            val boxWidth = 128f * 1.3f
            val boxHeight = 80f * 0.8f
            val padding = 40f
            val headSize = 50

            val gridW = (boxWidth * 2) + padding
            val gridH = (boxHeight * 2) + padding
            val startX = (screenWidth - gridW) / 2f
            val startY = (screenHeight - gridH) / 2f

            val offsets = listOf(
                startX to startY,
                (startX + boxWidth + padding) to startY,
                startX to (startY + boxHeight + padding),
                (startX + boxWidth + padding) to (startY + boxHeight + padding)
            )

            val cx = mc.window.guiScaledWidth / 2
            val cy = mc.window.guiScaledHeight / 2
            val mx = event.mouseX
            val my = event.mouseY

            val hoveredIndex = when {
                mx < cx && my < cy -> 0
                mx > cx && my < cy -> 1
                mx < cx && my > cy -> 2
                mx > cx && my > cy -> 3
                else -> - 1
            }

            players.forEachIndexed { i, entry ->
                if (entry == null) return@forEachIndexed

                val (x, y) = offsets[i]
                val isHovered = i == hoveredIndex

                val bgColor = when {
                    showLastDoorOpener.value && DungeonListener.lastDoorOpenner == entry.player -> {
                        if (isHovered) Color(33, 33, 33).brighter().brighter().brighter().brighter().brighter().brighter()
                        else Color(33, 33, 33).brighter().brighter().brighter().brighter()
                    }

                    tintDeadPlayers.value && entry.player.isDead -> {
                        val color = MathUtils.lerpColor(Color(33, 33, 33), Color.RED, 0.2f)
                        if (isHovered) color.brighter()
                        else color
                    }

                    else -> {
                        if (isHovered) Color(33, 33, 33).brighter().brighter()
                        else Color(33, 33, 33)
                    }
                }

                Render2D.drawRect(event.context, x, y, boxWidth, boxHeight, bgColor.withAlpha(190))
                Render2D.drawBorder(event.context, x, y, boxWidth, boxHeight, entry.player.clazz.color)

                val headX = (x + 10).toInt()
                val headY = (y + (boxHeight / 2) - headSize / 2).toInt()

                Render2D.drawPlayerHead(event.context, headX, headY, headSize, entry.player.skin)

                val textX = (x + 10 + headSize + 5).toInt()
                val textY = (y + boxHeight / 2 - mc.font.lineHeight / 2).toInt()

                Render2D.drawString(event.context, entry.player.name, textX, textY, entry.player.clazz.color)

                val status = if (entry.player.isDead) "§cDEAD" else "§7${entry.player.clazz.name}"
                event.context.drawString(mc.font, status, textX, textY + 12, 0xFFFFFF, true)
            }

            pose.popMatrix()
            Resolution.restore(event.context)
        }

        register<ContainerEvent.MouseClick> {
            if (! enabled || ! customLeapMenu.value) return@register
            if (! inSpiritLeap(event.screen)) return@register
            if (event.button != 0) return@register

            val cx = mc.window.guiScaledWidth / 2
            val cy = mc.window.guiScaledHeight / 2

            val quadrant = when {
                event.mouseX < cx && event.mouseY < cy -> 0
                event.mouseX > cx && event.mouseY < cy -> 1
                event.mouseX < cx && event.mouseY > cy -> 2
                event.mouseX > cx && event.mouseY > cy -> 3
                else -> return@register
            }

            event.isCanceled = true
            triggerLeap(quadrant)
        }

        register<ContainerEvent.Keyboard> {
            if (! enabled || ! customLeapMenu.value || ! leapKeybinds.value) return@register
            if (! inSpiritLeap(event.screen)) return@register

            val index = when (event.key) {
                key1.value -> 0
                key2.value -> 1
                key3.value -> 2
                key4.value -> 3
                else -> return@register
            }

            event.isCanceled = true
            triggerLeap(index)
        }
    }


    private fun inSpiritLeap(screen: Screen): Boolean {
        val title = screen.title.string.lowercase()
        return (title.contains("spirit leap") || title.contains("teleport to player"))
            && LocationUtils.inDungeon
            && customLeapMenu.value
            && enabled
    }

    private fun updateLeapMenu() {
        players.fill(null)
        val menu = mc.player !!.containerMenu

        for (slot in menu.slots) {
            if (slot.index >= 54) break
            val stack = slot.item
            if (! stack.`is`(Items.PLAYER_HEAD)) continue
            val headName = playerPattern.find(stack.displayName.unformattedText)?.groups?.get("name")?.value ?: continue

            DungeonListener.leapTeammates.forEachIndexed { index, teammate ->
                if (index < players.size && headName.equals(teammate.name, ignoreCase = true)) {
                    players[index] = LeapMenuPlayer(slot.index, teammate)
                }
            }
        }
    }

    private fun triggerLeap(index: Int) {
        val entry = players[index] ?: return
        if (entry.player.isDead) return ChatUtils.modMessage("§3LeapMenu >> §c${entry.player.name} is dead!")

        mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F))
        GuiUtils.clickSlot(entry.slotIndex, ButtonType.LEFT)
        mc.player !!.closeContainer()
    }
}