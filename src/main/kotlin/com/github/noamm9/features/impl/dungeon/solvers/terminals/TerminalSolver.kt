package com.github.noamm9.features.impl.dungeon.solvers.terminals

import com.github.noamm9.NoammAddons
import com.github.noamm9.event.impl.ContainerEvent
import com.github.noamm9.event.impl.ScreenEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.utils.Resolution
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ChatUtils.removeFormatting
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.ItemUtils.hasGlint
import com.github.noamm9.utils.ThreadUtils
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.Utils.uppercaseFirst
import com.github.noamm9.utils.render.Render2D
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor


object TerminalSolver: Feature("Terminal Solver with Prediction and Queue") {
    const val FIRST_CLICK_DELAY = 350

    val scale by SliderSetting("Custom Menu's Scale", 1f, 0.1f, 2f, 0.01f).section("Settings")
        .withDescription("How big the Terminal Menu should be.")

    val mode by DropdownSetting("Mode", 0, listOf("Normal", "Q-Terms"))
        .withDescription("The Terminal mode to use. Normal is vanilla, Q-Terms is queueing your clicks until the terminal updates so they all register correctly (recommended for high ping players).")

    val resyncTimeout by SliderSetting<Long>("Resync Timeout", 800, 400, 1000, 1)
        .withDescription("How long should the queue wait before clearing itself if the server doesn't update the terminal. Lower values are better for low ping players, higher values are better for high ping players.")
        .showIf { mode.value == 1 }

    val backgroundColor by ColorSetting("Background Color", Color(0, 0, 0, 100)).section("Colors")
    val borderColor by ColorSetting("Border Color", Color(255, 255, 255))
    val solutionColor by ColorSetting("Solution Color", Color(0, 255, 0, 255))


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


    private data class TerminalClick(val slotId: Int, val btn: Int)

    private var solution = mutableListOf<TerminalClick>()
    private val queue = mutableListOf<TerminalClick>()
    private var isClicked = false

    override fun init() {
        register<ScreenEvent.PreRender> {
            if (! TerminalListener.inTerm) return@register
            val termType = TerminalListener.currentType ?: return@register
            event.isCanceled = true

            Resolution.refresh()
            Resolution.apply(event.context)

            val scale = 3f * scale.value
            val screenWidth = Resolution.width / scale
            val screenHeight = Resolution.height / scale
            val windowSize = termType.slotCount

            val width = 9 * 18
            val height = windowSize / 9 * 18
            val offsetX = screenWidth / 2 - width / 2
            val offsetY = screenHeight / 2 - height / 2

            event.context.pose().pushMatrix()
            event.context.pose().scale(scale, scale)

            Render2D.drawCenteredString(event.context, termType.name.lowercase().uppercaseFirst(), offsetX + width / 2f, offsetY - 15f, scale = 1.2f)
            Render2D.drawRect(event.context, offsetX, offsetY, width, height, backgroundColor.value)
            Render2D.drawBorder(event.context, offsetX, offsetY, width, height, borderColor.value)
            val color = solutionColor.value

            solution.forEachIndexed { index, (slot, btn) ->
                val currentOffsetX = slot % 9 * 18 + offsetX
                val currentOffsetY = floor(slot / 9.0).toInt() * 18 + offsetY

                if (TerminalListener.currentType == TerminalType.NUMBERS) {
                    if (index > 2) return@forEachIndexed
                    Render2D.drawBorder(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color)
                    Render2D.drawRect(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color.withAlpha(40))
                    Render2D.drawCenteredString(
                        event.context,
                        (TerminalListener.currentItems[slot]?.count ?: mc.player?.containerMenu?.getSlot(slot)?.item?.count).toString(),
                        currentOffsetX + 8f, currentOffsetY + 5f
                    )
                }
                else if (TerminalListener.currentType.equalsOneOf(TerminalType.REDGREEN, TerminalType.STARTWITH, TerminalType.COLORS)) {
                    Render2D.drawBorder(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color)
                    Render2D.drawRect(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color.withAlpha(40))
                }
                else if (TerminalListener.currentType == TerminalType.RUBIX) {
                    Render2D.drawBorder(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color)
                    Render2D.drawRect(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color.withAlpha(40))
                    Render2D.drawCenteredString(event.context, "$btn", currentOffsetX + 8f, currentOffsetY + 5f)
                }
            }

            if (TerminalListener.currentType == TerminalType.MELODY) {
                val correct = TerminalType.melodyCorrect !!
                val button = TerminalType.melodyButton !!
                val current = TerminalType.melodyCurrent !!

                Render2D.drawBorder(event.context, offsetX + (correct + 1) * 18, offsetY + 18, 16f, 70f, Color(255, 0, 255))
                Render2D.drawRect(event.context, offsetX + (correct + 1) * 18, offsetY + 18, 16f, 70f, Color(255, 0, 255).withAlpha(40))

                for (i in 0 until windowSize) {
                    val currentOffsetX = i % 9 * 18 + offsetX
                    val currentOffsetY = floor((i / 9f)) * 18f + offsetY

                    val buttonSlot = button * 9 + 16
                    val currentSlot = button * 9 + 10 + current

                    when {
                        i == buttonSlot -> {
                            Render2D.drawBorder(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color)
                            Render2D.drawRect(event.context, currentOffsetX, currentOffsetY, 16f, 16f, color.withAlpha(40))
                        }

                        intArrayOf(16, 25, 34, 43).contains(i) -> {
                            Render2D.drawBorder(event.context, currentOffsetX, currentOffsetY, 16f, 16f, Color.RED)
                            Render2D.drawRect(event.context, currentOffsetX, currentOffsetY, 16f, 16f, Color.RED.withAlpha(40))
                        }

                        i == currentSlot -> {
                            Render2D.drawBorder(event.context, currentOffsetX, currentOffsetY, 16f, 16f, Color(255, 116, 0))
                            Render2D.drawRect(event.context, currentOffsetX, currentOffsetY, 16f, 16f, Color(255, 116, 0).withAlpha(40))
                        }
                    }
                }
            }

            event.context.pose().popMatrix()
            Resolution.restore(event.context)
        }

        register<ContainerEvent.MouseClick> {
            if (! TerminalListener.inTerm) return@register
            val termType = TerminalListener.currentType ?: return@register
            event.isCanceled = true
            if (System.currentTimeMillis() - TerminalListener.initialOpen < FIRST_CLICK_DELAY) return@register

            val scale = 3f * scale.value
            val mx = (mc.mouseHandler.xpos() / mc.window.screenWidth.toDouble() * Resolution.width).toInt() / scale
            val my = (mc.mouseHandler.ypos() / mc.window.screenHeight.toDouble() * Resolution.height).toInt() / scale

            val screenWidth = Resolution.width / scale
            val screenHeight = Resolution.height / scale
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

            val click = if (TerminalListener.currentType == TerminalType.NUMBERS)
                solution.firstOrNull()?.takeIf { it.slotId == slot }
            else if (TerminalListener.currentType.equalsOneOf(TerminalType.REDGREEN, TerminalType.STARTWITH, TerminalType.COLORS))
                solution.find { it.slotId == slot }
            else if (TerminalListener.currentType == TerminalType.RUBIX) {
                val btn = solution.find { it.slotId == slot }?.btn
                if (btn != null) {
                    val clickType = if (btn > 0) 0 else 1
                    TerminalClick(slot, clickType)
                }
                else null

            }
            else if (TerminalListener.currentType == TerminalType.MELODY) {
                if (slot.equalsOneOf(16, 25, 34, 43)) {
                    mc.gameMode?.handleInventoryMouseClick(TerminalListener.lastWindowId, slot, 0, ClickType.PICKUP, mc.player)
                }
                return@register
            }
            else null


            if (click == null) return@register
            predict(click)

            if (mode.value == 0) click(click)
            else if (isClicked) queue.add(click) else click(click)
        }
    }


    private fun predict(click: TerminalClick) {
        if (TerminalListener.currentType.equalsOneOf(TerminalType.NUMBERS, TerminalType.REDGREEN, TerminalType.STARTWITH, TerminalType.COLORS)) {
            solution.remove(click)
        }
        else if (TerminalListener.currentType == TerminalType.RUBIX) {
            val currentSolution = solution.find { it.slotId == click.slotId } ?: return
            val newClick = TerminalClick(click.slotId, currentSolution.btn + if (click.btn == 0) - 1 else 1)
            solution[solution.indexOf(currentSolution)] = newClick
            if (solution.find { it.slotId == click.slotId }?.btn == 0) solution.remove(newClick)
        }
    }

    private fun click(click: TerminalClick) {
        val player = mc.player ?: return
        isClicked = true

        mc.gameMode?.handleInventoryMouseClick(
            TerminalListener.lastWindowId,
            click.slotId,
            if (click.btn == 0) 2 else click.btn,
            if (click.btn == 0) ClickType.CLONE else ClickType.PICKUP,
            player
        )

        val initialWindowId = TerminalListener.lastWindowId
        ThreadUtils.setTimeout(resyncTimeout.value) {
            if (! TerminalListener.inTerm || initialWindowId != TerminalListener.lastWindowId) return@setTimeout
            if (NoammAddons.debugFlags.contains("terminal")) {
                ChatUtils.modMessage("Resync Timeout Triggered")
            }

            queue.clear()
            solve()
            isClicked = false
        }
    }


    fun solve(updatedSlot1: Int = 0, updatedItem1: ItemStack = ItemStack.EMPTY) {
        val type = TerminalListener.currentType ?: return
        val currentItems = TerminalListener.currentItems
        solution.clear()

        // (Solver Logic remains identical to original)
        when (type) {
            TerminalType.NUMBERS -> {
                currentItems.filter { it.value.item == Items.RED_STAINED_GLASS_PANE }
                    .toList()
                    .sortedBy { it.second.count }
                    .forEach { solution.add(TerminalClick(it.first, 0)) }
            }

            TerminalType.REDGREEN -> {
                currentItems.filter { it.value.item == Items.RED_STAINED_GLASS_PANE }
                    .forEach { solution.add(TerminalClick(it.key, 0)) }
            }

            TerminalType.STARTWITH -> {
                val match = TerminalType.startwithRegex.matchEntire(TerminalListener.currentTitle)
                val letter = match?.groupValues?.get(1)?.lowercase() ?: return
                currentItems.forEach { (slot, item) ->
                    val name = item.hoverName.string.removeFormatting().lowercase()
                    if (name.startsWith(letter) && ! item.hasGlint()) {
                        solution.add(TerminalClick(slot, 0))
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
                    solution.add(TerminalClick(slot, 0))
                }
            }

            TerminalType.RUBIX -> {
                val allowedSlots = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
                val panes = currentItems.filter { it.key in allowedSlots }
                val order = TerminalType.rubixOrder
                val costs = IntArray(5) { 0 }
                for (i in 0 until 5) {
                    panes.forEach { (_, stack) ->
                        val itemIdx = order.indexOf(stack.item)
                        if (itemIdx == - 1) return@forEach
                        val dist = abs(i - itemIdx)
                        val clicksNeeded = if (dist > 2) 5 - dist else dist
                        costs[i] += clicksNeeded
                    }
                }
                val bestIndex = costs.indices.minByOrNull { costs[it] } ?: 0
                if (TerminalType.lastRubixTarget == null || costs[TerminalType.lastRubixTarget !!] == 0) {
                    TerminalType.lastRubixTarget = bestIndex
                }
                val origin = TerminalType.lastRubixTarget !!
                panes.forEach { (slotId, itemstack) ->
                    val currentIdx = order.indexOf(itemstack.item)
                    if (currentIdx == - 1 || currentIdx == origin) return@forEach
                    var diff = origin - currentIdx
                    if (diff > 2) diff -= 5
                    if (diff < - 2) diff += 5
                    solution.add(TerminalClick(slotId, diff))
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
                }
            }
        }
    }

    fun onItemsUpdated(slot: Int = 0, item: ItemStack = ItemStack.EMPTY) {
        solve(slot, item)
        if (mode.value == 0) return

        if (queue.isNotEmpty()) {
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
                queue.forEach { predict(it) }
                click(nextClick)
                queue.removeAt(0)
            }
            else queue.clear()
        }
    }

    fun onTerminalOpen() = ::isClicked.set(false)
    fun onTerminalClose() = queue.clear()
}