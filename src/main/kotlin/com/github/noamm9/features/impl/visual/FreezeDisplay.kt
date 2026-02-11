package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.hud.HudElement
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.height
import com.github.noamm9.utils.render.Render2D.width
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

object FreezeDisplay: Feature("Shows how long the server froze after a chosen threshold") {
    private val color by ColorSetting("Color", Color(245, 73, 39), false)
    private val threshold by SliderSetting("Threshold", 500, 100, 2000, 100)
    private val dungeonsOnly by ToggleSetting("Only In Dungeons", true)

    private var lastPacketTime = System.currentTimeMillis()

    @Suppress("RemoveRedundantQualifierName")
    private val hud = object: HudElement() {
        override val toggle get() = FreezeDisplay.enabled

        override val shouldDraw: Boolean
            get() {
                if (dungeonsOnly.value && ! LocationUtils.inDungeon) return false
                if (mc.isLocalServer) return false

                return (System.currentTimeMillis() - lastPacketTime) > threshold.value
            }

        override val centered = true

        override fun draw(ctx: GuiGraphics, example: Boolean): Pair<Float, Float> {
            val diff = System.currentTimeMillis() - lastPacketTime
            val text = if (example) "567ms" else "${diff}ms"

            Render2D.drawCenteredString(ctx, text, 0, 0, color.value)
            return text.width().toFloat() to text.height().toFloat()
        }
    }

    override fun init() {
        hudElements.add(hud)

        register<WorldChangeEvent> { lastPacketTime = System.currentTimeMillis() }
        register<TickEvent.Server> { lastPacketTime = System.currentTimeMillis() }
    }
}
