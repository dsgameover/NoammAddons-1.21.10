package com.github.noamm9.features.impl.dev

import com.github.noamm9.NoammAddonsClient
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.ClickGuiScreen
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ButtonSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.ui.hud.HudEditorScreen
import java.awt.Color

object ClickGui: Feature("A feature used to change the ClickGui configiration.", toggled = true) {
    val playClickSound by ToggleSetting("Click Sound", true)
        .withDescription("Toggle for the sound that plays when u click on a setting element.")

    val accsentColor by ColorSetting("Accent Color", Color.CYAN, false)
        .withDescription("The Accent Color used by the whole ClickGui.")

    val editGuiButton by ButtonSetting("Open GUD Editor") {
        NoammAddonsClient.screen = HudEditorScreen
        ClickGuiScreen.onClose()
    }.withDescription("Opens the HUD Editor Screen where u can change you HUD elements size and position")

    val resetButton by ButtonSetting("Reset All Settings") {
        playClickSound.value = true
        accsentColor.value = Color.CYAN
    }.withDescription("Reverts all settings back to their original values.")

    override fun toggle() {}
}