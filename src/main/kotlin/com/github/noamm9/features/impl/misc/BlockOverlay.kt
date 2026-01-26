package com.github.noamm9.features.impl.misc

import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.hideIf
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.Utils
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.render.Render3D
import com.github.noamm9.utils.render.RenderContext
import com.github.noamm9.utils.world.WorldUtils
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.BlockHitResult

object BlockOverlay: Feature() {
    private val mode by DropdownSetting("Mode", 2, listOf("Outline", "Fill", "Filled Outline"))
    private val fillColor by ColorSetting("Fill Color", Utils.favoriteColor.withAlpha(50)).hideIf { mode.value == 0 }
    private val outlineColor by ColorSetting("Outline Color", Utils.favoriteColor, false).hideIf { mode.value == 1 }
    private val lineWidth by SliderSetting("Line Width", 2.5, 1, 10, 0.1).hideIf { mode.value == 1 }
    private val phase by ToggleSetting("Phase")

    fun render(bufferSource: MultiBufferSource.BufferSource, poseStack: PoseStack) {
        if (mc.options.hideGui) return
        val blockHitResult = mc.hitResult as? BlockHitResult ?: return
        if (WorldUtils.getStateAt(blockHitResult.blockPos).isAir) return

        Render3D.renderBlock(
            RenderContext(poseStack, bufferSource, mc.gameRenderer.mainCamera),
            blockHitResult.blockPos,
            outlineColor.value,
            fillColor.value,
            mode.value.equalsOneOf(0, 2),
            mode.value.equalsOneOf(1, 2),
            phase = phase.value,
            lineWidth.value
        )
    }
}
