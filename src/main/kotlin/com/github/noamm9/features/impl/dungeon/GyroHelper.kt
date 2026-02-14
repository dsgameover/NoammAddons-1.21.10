package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.section
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.showIf
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.Utils.favoriteColor
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import com.github.noamm9.utils.render.Render3D
import com.github.noamm9.utils.render.RenderContext
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object GyroHelper : Feature("Renders a circle where your gyro will be located", "Gyro Helper") {
    private val drawRing by ToggleSetting(
        "Draw Ring", true
    ).withDescription("Draws an outline where your Gyro will be placed").section("Render")
    private val drawBox by ToggleSetting(
        "Draw Box", false
    ).withDescription("Draws a Box in the middle of the Gyro outline")
    private val lineWidth by SliderSetting(
        "Outline Width", 2, 1, 10, 1
    ).withDescription("Choose outline ring width").showIf { drawRing.value }
    private val useServerPos by ToggleSetting(
        "Use Server Position", false
    ).withDescription("Uses server position to show you where gyro will be placed").section("Server")

    private val boxColor by ColorSetting("Box Color", favoriteColor.withAlpha(0.3f)).section("Color")
    private val ringColor by ColorSetting("Ring Color", favoriteColor)

    override fun init() {
        register<RenderWorldEvent> {
            val player = mc.player ?: return@register
            if (!drawRing.value && !drawBox.value) return@register
            if (boxColor.value.alpha + ringColor.value.alpha == 0) return@register
            if (mc.player?.mainHandItem?.skyblockId != "GYROKINETIC_WAND") return@register
            val finalPos: Vec3
            val finalYaw: Float
            val finalPitch: Float
            if (useServerPos.value) {
                finalPos = Vec3(player.xOld, player.yOld + player.eyeHeight, player.zOld)
                finalYaw = player.yHeadRot
                finalPitch = player.xRot
            } else {
                finalPos = player.getEyePosition(1.0f)
                finalYaw = player.yRot
                finalPitch = player.xRot
            }
            val gyroPos = getBlockFromLook(player, 25.0, finalPos, finalYaw, finalPitch) ?: return@register
            val level = player.level()
            val stateAtPos = level.getBlockState(gyroPos)
            val stateAbove = level.getBlockState(gyroPos.above())

            if (stateAtPos.isAir || (!stateAbove.isAir && !stateAbove.`is`(BlockTags.WOOL_CARPETS))) return@register
            val ctx = RenderContext(
                event.ctx.matrixStack, mc.renderBuffers().bufferSource(), mc.gameRenderer.mainCamera
            )
            if (drawBox.value) Render3D.renderBlock(ctx, gyroPos, boxColor.value, fill = true, outline = true)
            if (drawRing.value) {
                val circleCenter = Vec3.atLowerCornerOf(gyroPos).add(0.5, 2.05, 0.5)
                Render3D.renderCircle(ctx, circleCenter, 10.0, ringColor.value, lineWidth.value)
            }
        }
    }

    fun getBlockFromLook(
        player: LocalPlayer,
        range: Double,
        customPos: Vec3? = null,
        customYaw: Float? = null,
        customPitch: Float? = null
    ): BlockPos? {
        val startVec = customPos ?: player.getEyePosition(1.0f)

        val lookVec = if (customYaw != null && customPitch != null) {
            player.calculateViewVector(customPitch, customYaw)
        } else {
            player.getViewVector(1.0f)
        }

        val endVec = startVec.add(lookVec.scale(range))

        val context = ClipContext(
            startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
        )

        val result = player.level().clip(context)
        return if (result.type == HitResult.Type.BLOCK) result.blockPos else null
    }
}