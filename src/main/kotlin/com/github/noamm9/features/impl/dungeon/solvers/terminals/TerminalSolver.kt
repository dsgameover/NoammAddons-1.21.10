package com.github.noamm9.features.impl.dungeon.solvers.terminals

import com.github.noamm9.NoammAddons
import com.github.noamm9.event.impl.ContainerEvent
import com.github.noamm9.event.impl.ScreenEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.utils.Resolution
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ChatUtils.removeFormatting
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.ItemUtils.hasGlint
import com.github.noamm9.utils.ThreadUtils
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.Utils.uppercaseFirst
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random


object TerminalSolver: Feature("Terminal Solver for floor 7 terminals") {
    const val FIRST_CLICK_DELAY = 7 // 350ms

    val scale by SliderSetting("Custom Menu's Scale", 1f, 0.1f, 2f, 0.01f).section("General")
        .withDescription("How big the Terminal Menu should be.")

    val slotStyle by DropdownSetting("Slot Style", 0, listOf("Rect", "Bordered-Rect", "Button"))
        .withDescription("How the highlighted slots should be drawn.")

    val mode by DropdownSetting("Mode", 0, listOf("Normal", "Q-Terms", "Auto"))
        .withDescription("The Terminal mode to use.")

    val resyncTimeout by SliderSetting<Long>("Resync Timeout", 800, 600, 1000, 1)
        .withDescription("How long should the queue wait before clearing itself if the server doesn't update the terminal.")
        .showIf { mode.value != 0 }

    val randomDelay by ToggleSetting("Random Delay").section("Auto Terminal")
        .withDescription("Should the delay between clicks be random?")
        .showIf { mode.value == 2 }

    val autoDelay by SliderSetting("Click Delay", 150.0, 0.0, 500.0, 10.0)
        .withDescription("Fixed delay between clicks in milliseconds.")
        .showIf { mode.value == 2 && ! randomDelay.value }

    val minRandomDelay by SliderSetting("Min Random Delay", 50.0, 0.0, 500.0, 10.0)
        .withDescription("The minimum possible delay")
        .showIf { mode.value == 2 && randomDelay.value }

    val maxRandomDelay by SliderSetting("Max Random Delay", 150.0, 0.0, 500.0, 10.0)
        .withDescription("The maximum possible delay")
        .showIf { mode.value == 2 && randomDelay.value }

    val clickOrder by DropdownSetting("Click Order", 0, listOf("None", "Random", "Human", "Skizo"))
        .withDescription("Human: Logic pathing. Random: RNG. Skizo: Chaotic/Furthest.")
        .showIf { mode.value == 2 }

    val backgroundColor by ColorSetting("Background Color", Color(0, 0, 0, 100)).section("Settings - UI")
    val borderColor by ColorSetting("Border Color", Color(255, 255, 255))
    val titleColor by ColorSetting("Title Text Color", Color.WHITE)
    val queueColor by ColorSetting("Queue Text Color", Color.CYAN)
    val overlayTextColor by ColorSetting("Overlay Text Color", Color.WHITE)

    val solutionColor by ColorSetting("Generic Solution", Color(0, 255, 0, 130)).section("Colors - Terminals").showIf {
        melody.value || numbers.value || rubix.value || colors.value || startwith.value || redgreen.value
    }
    val numbersNumbers by ToggleSetting("Numbers: Show Numbers").showIf { numbers.value }.withDescription("Should the numbers be shown on the slots?")
    val numbersFirstColor by ColorSetting("Numbers: 1st Click", Color(0, 255, 0, 130)).showIf { numbers.value }
    val numbersSecondColor by ColorSetting("Numbers: 2nd Click", Color(0, 200, 0, 130)).showIf { numbers.value }
    val numbersThirdColor by ColorSetting("Numbers: 3rd Click", Color(0, 150, 0, 130)).showIf { numbers.value }
    val rubixPositiveColor by ColorSetting("Rubix: Positive (+)", Color(0, 114, 255, 130)).showIf { rubix.value }
    val rubixNegativeColor by ColorSetting("Rubix: Negative (-)", Color(205, 0, 0, 130)).showIf { rubix.value }
    val melodyColumnColor by ColorSetting("Melody: Column", Color(255, 0, 255, 130)).showIf { melody.value }
    val melodyIndicatorColor by ColorSetting("Melody: Indicator", Color(255, 116, 0, 130)).showIf { melody.value }
    val melodyWrongColor by ColorSetting("Melody: Wrong", Color(255, 0, 0, 130)).showIf { melody.value }

    val melody by ToggleSetting("Melody", true).section("Toggles")
    val numbers by ToggleSetting("Numbers", true)
    val rubix by ToggleSetting("Rubix", true)
    val colors by ToggleSetting("Colors", true)
    val startwith by ToggleSetting("Start-With", true)
    val redgreen by ToggleSetting("Red-Green", true)

    override fun onEnable() {
        super.onEnable()
        TerminalListener.packetRecivedListener.register()
        TerminalListener.packetSentListener.register()
        TerminalListener.tickListener.register()
    }

    override fun onDisable() {
        super.onDisable()
        TerminalListener.packetRecivedListener.unregister()
        TerminalListener.packetSentListener.unregister()
        TerminalListener.tickListener.unregister()
    }

    private var solution = mutableListOf<TerminalClick>()
    private val queue = mutableListOf<TerminalClick>()
    private var isClicked = false
    private var lastClickTime = 0L
    private var lastClickedSlot: Int? = null

    override fun init() {
        register<TickEvent.Server> {
            if (mode.value != 2) return@register
            if (! TerminalListener.inTerm) return@register
            if (DungeonListener.currentTime - TerminalListener.initialOpen < FIRST_CLICK_DELAY) return@register
            if (TerminalListener.currentType != TerminalType.MELODY) return@register
            if (DungeonListener.currentTime - lastClickTime < 5) return@register

            val current = TerminalType.melodyCurrent ?: return@register
            val correct = TerminalType.melodyCorrect ?: return@register
            val buttonRow = TerminalType.melodyButton ?: return@register
            if (current != correct) return@register
            val actualSlot = buttonRow * 9 + 16
            if (lastClickedSlot == actualSlot) return@register

            sendClickPacket(actualSlot, 0)
            lastClickTime = DungeonListener.currentTime
            lastClickedSlot = actualSlot
        }

        register<ScreenEvent.PreRender> {
            if (! TerminalListener.inTerm) return@register
            val termType = TerminalListener.currentType ?: return@register
            event.isCanceled = true

            Resolution.refresh()
            Resolution.push(event.context)

            val uiScale = 3f * scale.value
            val screenWidth = Resolution.width / uiScale
            val screenHeight = Resolution.height / uiScale
            val windowSize = termType.slotCount

            val width = 9 * 18
            val height = windowSize / 9 * 18
            val offsetX = screenWidth / 2 - width / 2
            val offsetY = screenHeight / 2 - height / 2

            event.context.pose().pushMatrix()
            event.context.pose().scale(uiScale, uiScale)

            Render2D.drawCenteredString(
                event.context,
                termType.name.lowercase().uppercaseFirst(),
                offsetX + width / 2f,
                offsetY - 15f,
                color = titleColor.value,
                scale = 1.2f
            )
            Render2D.drawRect(event.context, offsetX, offsetY, width, height, backgroundColor.value)
            Render2D.drawBorder(event.context, offsetX, offsetY, width, height, borderColor.value)

            val baseColor = solutionColor.value

            solution.forEachIndexed { index, (slot, btn) ->
                val slotX = slot % 9 * 18 + offsetX
                val slotY = floor(slot / 9.0).toInt() * 18 + offsetY

                when (TerminalListener.currentType) {
                    TerminalType.NUMBERS -> {
                        if (index <= 2) {
                            val color = when (index) {
                                0 -> numbersFirstColor.value
                                1 -> numbersSecondColor.value
                                else -> numbersThirdColor.value
                            }
                            drawSlot(event.context, slotX, slotY, color)

                            if (numbersNumbers.value) {
                                val count = TerminalType.numbersSlotCounts[slot] ?: 0
                                drawCenteredText(event.context, count.toString(), slotX, slotY)
                            }
                        }
                    }

                    TerminalType.RUBIX -> {
                        val color = if (btn > 0) rubixPositiveColor.value else rubixNegativeColor.value
                        drawSlot(event.context, slotX, slotY, color)
                        drawCenteredText(event.context, "$btn", slotX, slotY)
                    }

                    else -> drawSlot(event.context, slotX, slotY, baseColor)
                }
            }

            if (TerminalListener.currentType == TerminalType.MELODY) {
                val correct = TerminalType.melodyCorrect
                val button = TerminalType.melodyButton
                val current = TerminalType.melodyCurrent

                if (correct != null && button != null && current != null) {
                    drawSlot(event.context, offsetX + (correct + 1) * 18, offsetY + 18, melodyColumnColor.value, 16, 70)

                    for (i in 0 until windowSize) {
                        val x = i % 9 * 18 + offsetX
                        val y = floor((i / 9f)) * 18f + offsetY

                        val buttonSlot = button * 9 + 16
                        val currentSlot = button * 9 + 10 + current

                        when {
                            i == buttonSlot -> drawSlot(event.context, x, y, baseColor)
                            i.equalsOneOf(16, 25, 34, 43) -> drawSlot(event.context, x, y, melodyWrongColor.value)
                            i == currentSlot -> drawSlot(event.context, x, y, melodyIndicatorColor.value)
                        }
                    }
                }
            }

            if (mode.value == 1) Render2D.drawCenteredString(
                event.context,
                "Queue: ${queue.size}",
                offsetX + width / 2,
                offsetY + height + 5,
                color = queueColor.value,
                scale = 1.2f
            )

            event.context.pose().popMatrix()
            Resolution.pop(event.context)
        }

        register<ContainerEvent.MouseClick> {
            if (! TerminalListener.inTerm) return@register
            val termType = TerminalListener.currentType ?: return@register
            event.isCanceled = true
            if (mode.value == 2) return@register
            if (DungeonListener.currentTime - TerminalListener.initialOpen < FIRST_CLICK_DELAY) return@register

            val uiScale = 3f * scale.value
            val mx = Resolution.getMouseX() / uiScale
            val my = Resolution.getMouseY() / uiScale

            val screenWidth = Resolution.width / uiScale
            val screenHeight = Resolution.height / uiScale
            val windowSize = termType.slotCount

            val width = 9 * 18
            val height = windowSize / 9 * 18
            val offsetX = screenWidth / 2 - width / 2
            val offsetY = screenHeight / 2 - height / 2

            val slotX = floor((mx - offsetX) / 18).toInt()
            val slotY = floor((my - offsetY) / 18).toInt()

            if (slotX !in 0 .. 8 || slotY < 0) return@register

            val slot = slotX + slotY * 9
            if (slot >= windowSize) return@register

            val click = when {
                TerminalListener.currentType == TerminalType.NUMBERS -> solution.firstOrNull()?.takeIf { it.slotId == slot }

                TerminalListener.currentType.equalsOneOf(TerminalType.REDGREEN, TerminalType.STARTWITH, TerminalType.COLORS) -> {
                    solution.find { it.slotId == slot }
                }

                TerminalListener.currentType == TerminalType.RUBIX -> {
                    solution.find { it.slotId == slot }?.btn?.let {
                        TerminalClick(slot, if (it > 0) 0 else 1)
                    }
                }

                TerminalListener.currentType == TerminalType.MELODY -> {
                    if (slot.equalsOneOf(16, 25, 34, 43)) sendClickPacket(slot, 0)
                    return@register
                }

                else -> null
            }

            if (click == null) return@register

            if (mode.value != 0) predict(click)
            if (mode.value == 0) click(click) else if (isClicked) queue.add(click) else click(click)
        }
    }

    private fun drawSlot(ctx: GuiGraphics, x: Number, y: Number, color: Color, w: Number = 16, h: Number = 16) {
        when (slotStyle.value) {
            0 -> Render2D.drawRect(ctx, x, y, w, h, color)
            1 -> {
                Render2D.drawBorder(ctx, x, y, w, h, color)
                Render2D.drawRect(ctx, x, y, w, h, color.withAlpha(40))
            }

            2 -> Render2D.drawFloatingRect(ctx, x, y, w, h, color.darker())
        }
    }

    private fun drawCenteredText(ctx: GuiGraphics, text: String, slotX: Number, slotY: Number) {
        val centerX = slotX.toFloat() + 8f
        val centerY = slotY.toFloat() + 8f - mc.font.lineHeight / 2
        Render2D.drawCenteredString(ctx, text, centerX, centerY, color = overlayTextColor.value)
    }

    private fun predict(click: TerminalClick) {
        if (TerminalListener.currentType.equalsOneOf(TerminalType.NUMBERS, TerminalType.REDGREEN, TerminalType.STARTWITH, TerminalType.COLORS)) {
            solution.remove(click)
        }
        else if (TerminalListener.currentType == TerminalType.RUBIX) {
            val currentSolution = solution.find { it.slotId == click.slotId } ?: return
            val change = if (click.btn == 0) - 1 else 1
            val newDiff = currentSolution.btn + change

            if (newDiff == 0) solution.remove(currentSolution)
            else solution[solution.indexOf(currentSolution)] = TerminalClick(click.slotId, newDiff)
        }
    }

    private fun sendClickPacket(slot: Int, btn: Int) {
        mc.gameMode?.handleInventoryMouseClick(
            TerminalListener.lastWindowId,
            slot,
            if (btn == 0) 2 else btn,
            if (btn == 0) ClickType.CLONE else ClickType.PICKUP,
            mc.player
        )

        if (NoammAddons.debugFlags.contains("terminal")) {
            ChatUtils.modMessage("Clicked $slot on ${TerminalListener.currentType?.name}")
        }
    }

    private fun click(click: TerminalClick) {
        isClicked = true
        lastClickedSlot = click.slotId
        sendClickPacket(click.slotId, click.btn)
        val initialWindowId = TerminalListener.lastWindowId
        ThreadUtils.setTimeout(resyncTimeout.value) {
            if (! TerminalListener.inTerm || initialWindowId != TerminalListener.lastWindowId) return@setTimeout
            if (NoammAddons.debugFlags.contains("terminal")) {
                ChatUtils.modMessage("Resync Timeout Triggered")
            }

            if (mode.value == 1) {
                queue.clear()
                solve()
                isClicked = false
            }
            else if (mode.value == 2) {
                ThreadUtils.scheduledTaskServer((resyncTimeout.value / 50).toInt()) {
                    if (! TerminalListener.inTerm || initialWindowId != TerminalListener.lastWindowId) return@scheduledTaskServer
                    lastClickedSlot = null
                    autoClick()
                }
            }
        }
    }

    fun solve(updatedSlot1: Int = 0, updatedItem1: ItemStack = ItemStack.EMPTY) {
        val type = TerminalListener.currentType ?: return
        val currentItems = TerminalListener.currentItems
        solution.clear()

        when (type) {
            TerminalType.NUMBERS -> {
                currentItems.filter { it.value.item == Items.RED_STAINED_GLASS_PANE }
                    .toList()
                    .sortedBy { it.second.count }
                    .forEach {
                        TerminalType.numbersSlotCounts[it.first] = it.second.count
                        solution.add(TerminalClick(it.first))
                    }
            }

            TerminalType.REDGREEN -> {
                currentItems.filter { it.value.item == Items.RED_STAINED_GLASS_PANE }
                    .forEach { solution.add(TerminalClick(it.key)) }
            }

            TerminalType.STARTWITH -> {
                val match = TerminalType.startwithRegex.matchEntire(TerminalListener.currentTitle)
                val letter = match?.groupValues?.get(1)?.lowercase() ?: return
                currentItems.forEach { (slot, item) ->
                    val name = item.hoverName.string.removeFormatting().lowercase()
                    if (name.startsWith(letter) && ! item.hasGlint()) {
                        solution.add(TerminalClick(slot))
                    }
                }
            }

            TerminalType.COLORS -> {
                val match = TerminalType.colorsRegex.matchEntire(TerminalListener.currentTitle)
                val targetColor = match?.groupValues?.get(1) ?: return
                val dyeColor = DyeColor.entries.find { it.name.replace("_", " ").equals(targetColor.replace("SILVER", "LIGHT GRAY"), true) } ?: return
                currentItems.forEach { (slot, item) ->
                    if (item.hasGlint()) return@forEach
                    if (item.item == Items.BLACK_STAINED_GLASS_PANE) return@forEach
                    val isNameMatch = item.item.name.string.startsWith(dyeColor.name.replace("_", " "), true)
                    val isDyeMatch = when (dyeColor) {
                        DyeColor.BLACK -> item.item == Items.INK_SAC
                        DyeColor.BLUE -> item.item == Items.LAPIS_LAZULI
                        DyeColor.BROWN -> item.item == Items.COCOA_BEANS
                        DyeColor.WHITE -> item.item == Items.BONE_MEAL
                        else -> false
                    }
                    if (! isNameMatch && ! isDyeMatch) return@forEach
                    solution.add(TerminalClick(slot))
                }
            }

            TerminalType.RUBIX -> {
                val allowedSlots = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
                val panes = currentItems.filter { it.key in allowedSlots && TerminalType.rubixOrder.contains(it.value.item) }
                val costs = IntArray(5) { 0 }

                for (i in 0 until 5) {
                    panes.forEach { (_, itemStack) ->
                        val itemIdx = TerminalType.rubixOrder.indexOf(itemStack.item)
                        if (itemIdx != - 1) {
                            val dist = abs(i - itemIdx)
                            val clicksNeeded = if (dist > 2) 5 - dist else dist
                            costs[i] += clicksNeeded
                        }
                    }
                }

                val origin = costs.indices.minByOrNull { costs[it] } ?: 0

                panes.forEach { (slotId, itemStack) ->
                    val currentIdx = TerminalType.rubixOrder.indexOf(itemStack.item)
                    if (currentIdx != - 1 && currentIdx != origin) {
                        var diff = origin - currentIdx
                        if (diff > 2) diff -= 5
                        if (diff < - 2) diff += 5
                        solution.add(TerminalClick(slotId, diff))
                    }
                }
            }

            TerminalType.MELODY -> {
                if (updatedItem1.item == Items.LIME_STAINED_GLASS_PANE) {
                    val correct = currentItems.entries.find { it.value.item == Items.MAGENTA_STAINED_GLASS_PANE }?.key?.minus(1)
                    val button = floor((updatedSlot1 / 9).toDouble()) - 1
                    val current = updatedSlot1 % 9 - 1
                    if (correct != null) TerminalType.melodyCorrect = correct
                    TerminalType.melodyButton = button.toInt()
                    TerminalType.melodyCurrent = current
                    lastClickedSlot = null
                }
            }
        }
    }

    fun onItemsUpdated(slot: Int = 0, item: ItemStack = ItemStack.EMPTY) {
        solve(slot, item)

        if (mode.value == 1 && queue.isNotEmpty()) {
            val nextClick = queue[0]

            val isValid = when (TerminalListener.currentType) {
                TerminalType.NUMBERS -> {
                    val firstSol = solution.firstOrNull()
                    firstSol != null && firstSol.slotId == nextClick.slotId
                }

                TerminalType.RUBIX -> {
                    val sol = solution.find { it.slotId == nextClick.slotId }
                    sol != null && ((sol.btn > 0 && nextClick.btn == 0) || (sol.btn < 0 && nextClick.btn == 1))
                }

                else -> solution.any { it.slotId == nextClick.slotId }
            }

            if (isValid) {
                queue.forEach(::predict)
                click(queue.removeAt(0))
            }
            else queue.clear()
        }
        else autoClick()

    }

    private fun autoClick() {
        if (mode.value != 2 || solution.isEmpty()) return

        val rawClick = if (TerminalListener.currentType == TerminalType.NUMBERS) solution.first()
        else when (clickOrder.value) {
            1 -> solution.random()
            2 -> HumanClickOrder.getBestClick(solution, TerminalListener.currentType !!)
            3 -> HumanClickOrder.getWorstClick(solution, TerminalListener.currentType !!)
            else -> solution.first()
        }

        if (lastClickedSlot == rawClick.slotId && TerminalListener.currentType != TerminalType.RUBIX) return

        val finalClick = if (TerminalListener.currentType == TerminalType.RUBIX)
            TerminalClick(rawClick.slotId, if (rawClick.btn > 0) 0 else 1)
        else rawClick

        val delayTicks = when {
            DungeonListener.currentTime - TerminalListener.initialOpen <= FIRST_CLICK_DELAY -> FIRST_CLICK_DELAY

            randomDelay.value -> {
                val min = minRandomDelay.value.toLong().coerceAtLeast(0)
                val max = maxRandomDelay.value.toLong().coerceAtLeast(0)
                val delayMs = if (min == max) min else Random.nextLong(minOf(min, max), maxOf(min, max))
                (delayMs / 50).toInt()
            }

            else -> (autoDelay.value.toLong() / 50).toInt()

        }.coerceAtLeast(0)


        if (delayTicks == 0) click(finalClick)
        else ThreadUtils.scheduledTaskServer(delayTicks) {
            if (TerminalListener.inTerm) click(finalClick)
        }
    }

    fun onTerminalOpen() = ::isClicked.set(false)

    fun onTerminalClose() {
        queue.clear()
        solution.clear()
        lastClickTime = 0
        lastClickedSlot = null
    }
}