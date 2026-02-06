package com.github.noamm9.features.impl.general.teleport

import com.github.noamm9.event.impl.MouseClickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.showIf
import com.github.noamm9.utils.PlayerUtils
import com.github.noamm9.utils.items.ItemUtils.customData
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

object LCEtherwarp: Feature(name = "LC Etherwarp", description = "Allows you to use the etherwarp ability with left-click") {
    private val swingHandToggle by ToggleSetting("Swing Hand", true)
    private val autoSneak by ToggleSetting("Auto Sneak", false)
    private val autoSneakDelay by SliderSetting("Auto Sneak Delay", 50, 50, 150, 1).showIf { autoSneak.value }

    override fun init() {
        register<MouseClickEvent> {
            if (event.button != 0) return@register
            if (event.action != GLFW.GLFW_PRESS) return@register
            if (mc.screen != null) return@register
            val player = mc.player ?: return@register
            if (! player.isCrouching && ! autoSneak.value) return@register
            if (getEtherwarpDistance(player.mainHandItem) == null) return@register

            event.isCanceled = true

            if (! player.isCrouching && autoSneak.value) {
                scope.launch {
                    val wait = autoSneakDelay.value.toLong() / 2
                    PlayerUtils.toggleSneak(true)
                    delay(wait)

                    PlayerUtils.rightClick()
                    if (swingHandToggle.value) PlayerUtils.swingArm()

                    delay(wait)
                    PlayerUtils.toggleSneak(false)
                }
            }
            else {
                PlayerUtils.rightClick()
                if (swingHandToggle.value) PlayerUtils.swingArm()
            }
        }
    }

    private fun getEtherwarpDistance(stack: ItemStack): Double? {
        if (stack.isEmpty) return null
        val id = stack.skyblockId
        if (id != "ASPECT_OF_THE_VOID" && id != "ASPECT_OF_THE_END") return null
        val nbt = stack.customData
        if (nbt.getByte("ethermerge").orElse(0).toInt() != 1) return null
        return 57.0 + nbt.getByte("tuned_transmission").orElse(0).toInt()
    }
}

