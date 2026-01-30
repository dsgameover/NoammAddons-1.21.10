package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.height
import com.github.noamm9.utils.render.Render2D.width
import java.awt.Color

object FreezeDisplay: Feature("Shows how long the server froze after a chosen threshold") {
    private val color by ColorSetting("Color", Color(245, 73, 39), false)
    private val threshold by SliderSetting("Threshold", 500, 100, 2000, 100)
    private val dungeonsOnly by ToggleSetting("Only In Dungeons", true)

    private var lastTick = System.currentTimeMillis()

    override fun init() {
        hudElement(
            "Freeze Display",
            { (! dungeonsOnly.value || LocationUtils.inDungeon) && System.currentTimeMillis() - lastTick > threshold.value }
        ) { context, example ->
            val text = if (example) "567ms" else "${System.currentTimeMillis() - lastTick}ms"
            Render2D.drawString(context, text, 0, 0, color.value)
            return@hudElement text.width().toFloat() to text.height().toFloat()
        }

        register<TickEvent.Server> { lastTick = System.currentTimeMillis() }
    }
}