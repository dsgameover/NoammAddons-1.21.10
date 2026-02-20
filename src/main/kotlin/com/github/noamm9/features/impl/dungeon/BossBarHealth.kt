package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.ChatUtils.unformattedText
import com.github.noamm9.utils.location.LocationUtils
import net.minecraft.network.chat.Component
import java.util.*

object BossBarHealth: Feature(name = "BossBar Health", description = "Shows the health number of the bossbar boss") {
    private val theWatcher by ToggleSetting("The Watcher")
    private val f4Thorn by ToggleSetting("Thorn")
    private val f7Withers by ToggleSetting("F7 Withers")

    @JvmStatic
    fun getMaxHealth(nameComponent: Component): Float {
        val name = nameComponent.unformattedText
        val isMaster = LocationUtils.isMasterMode

        return when (name) {
            "The Watcher" if theWatcher.value -> 12F + (LocationUtils.dungeonFloorNumber?.toFloat() ?: 0F)
            "Thorn" if f4Thorn.value -> if (isMaster) 6F else 4F
            "Maxor" if f7Withers.value -> if (isMaster) 800_000_000F else 100_000_000F
            "Storm" if f7Withers.value -> if (isMaster) 1_000_000_000F else 400_000_000F
            "Goldor" if f7Withers.value -> if (isMaster) 1_200_000_000F else 750_000_000F
            "Necron" if f7Withers.value -> if (isMaster) 1_400_000_000F else 1_000_000_000F
            else -> - 1f
        }
    }

    @JvmStatic
    fun formatHealth(health: Float): String {
        if (health >= 1_000_000_000) {
            val h = health / 1_000_000_000F
            if (h % 1.0f == 0f) return h.toInt().toString() + "B"
            return String.format(Locale.US, "%.1fB", h)
        }
        else if (health >= 1_000_000) return (health / 1_000_000F).toInt().toString() + "M"
        else if (health >= 1000) return (health / 1000f).toInt().toString() + "k"
        else return health.toInt().toString()
    }
}