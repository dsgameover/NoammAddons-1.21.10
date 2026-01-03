package com.github.noamm9.utils.render

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.ChatUtils.addColor
import com.github.noamm9.utils.NumbersUtils.minus
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.item.ItemStack
import java.awt.Color
import java.util.*

object Render2D {
    fun drawImage(ctx: GuiGraphics, image: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        ctx.blitSprite(RenderPipelines.GUI_TEXTURED, image, x, y, width, height)
    }

    fun drawTexture(
        ctx: GuiGraphics, image: ResourceLocation,
        x: Int, y: Int, u: Float, v: Float,
        width: Int, height: Int,
        regionWidth: Int, regionHeight: Int,
        textureWidth: Int = 256, textureHeight: Int = 256
    ) {
        ctx.blit(
            RenderPipelines.GUI_TEXTURED,
            image,
            x,
            y,
            u,
            v,
            width,
            height,
            regionWidth,
            regionHeight,
            textureWidth,
            textureHeight
        )
    }

    @JvmOverloads
    fun drawRect(ctx: GuiGraphics, x: Number, y: Number, width: Number, height: Number, color: Color = Color.WHITE) {
        val pose = ctx.pose()
        pose.translate(x.toFloat(), y.toFloat())
        ctx.fill(0, 0, width.toInt(), height.toInt(), color.rgb)
        pose.translate(- x.toFloat(), - y.toFloat())
    }

    @JvmOverloads
    fun drawString(ctx: GuiGraphics, str: String, x: Number, y: Number, color: Color = Color.WHITE, scale: Number = 1, shadow: Boolean = true) {
        val pose = ctx.pose()

        pose.translate(x.toFloat(), y.toFloat())
        if (scale != 1f) pose.scale(scale.toFloat(), scale.toFloat())

        ctx.drawString(mc.font, str.addColor(), 0, 0, color.rgb, shadow)

        if (scale != 1f) pose.scale(1f / scale.toFloat(), 1f / scale.toFloat())
        pose.translate(- x.toFloat(), - y.toFloat())
    }

    fun drawCenteredString(ctx: GuiGraphics, str: String, x: Number, y: Number, color: Color = Color.WHITE, scale: Number = 1, shadow: Boolean = true) {
        drawString(ctx, str, x - str.width() / 2, y, color, scale, shadow)
    }

    fun renderItem(context: GuiGraphics, item: ItemStack, x: Float, y: Float, scale: Float) {
        context.pose().pushMatrix()
        context.pose().translate(x, y)
        context.pose().scale(scale, scale)

        context.renderItem(item, 0, 0)

        context.pose().popMatrix()
    }

    private val textureCache = mutableMapOf<UUID, PlayerSkin>()
    private var lastCacheClear = System.currentTimeMillis()

    fun drawPlayerHead(context: GuiGraphics, x: Int, y: Int, size: Int, uuid: UUID) {
        val now = System.currentTimeMillis()
        if (now - lastCacheClear > 300000L) {
            textureCache.clear()
            lastCacheClear = now
        }

        val textures = textureCache.getOrElse(uuid) {
            val profile = mc.connection?.getPlayerInfo(uuid)?.profile
            val skin = if (profile != null) {
                mc.skinManager.get(profile).getNow(Optional.empty()).orElseGet { DefaultPlayerSkin.get(uuid) }
            }
            else DefaultPlayerSkin.get(uuid)

            val defaultSkin = DefaultPlayerSkin.get(uuid)
            if (skin.body() != defaultSkin.body()) textureCache[uuid] = skin
            skin
        }

        PlayerFaceRenderer.draw(context, textures, x, y, size)
    }

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { mc.font.width(it) }
    }

    fun String.height(): Int {
        val lineCount = count { it == '\n' } + 1
        return mc.font.lineHeight * lineCount
    }

    /**
     * Draws a gradient from Color1 (Left) to Color2 (Right)
     */
    fun drawHorizontalGradient(ctx: GuiGraphics, x: Number, y: Number, width: Number, height: Number, color1: Color, color2: Color) {
        val pose = ctx.pose()
        val fx = x.toFloat()
        val fy = y.toFloat()
        val fw = width.toFloat()
        val fh = height.toFloat()
        val angle = (- Math.PI / 2).toFloat() // -90 degrees

        pose.translate(fx, fy + fh)
        pose.rotate(angle)

        ctx.fillGradient(0, 0, fh.toInt(), fw.toInt(), color1.rgb, color2.rgb)

        pose.rotate(- angle)
        pose.translate(- fx, - (fy + fh))
    }

    /**
     * Draws a gradient from Color1 (Top) to Color2 (Bottom)
     */
    fun drawVerticalGradient(ctx: GuiGraphics, x: Number, y: Number, width: Number, height: Number, color1: Color, color2: Color) {
        val fx = x.toFloat()
        val fy = y.toFloat()
        val iw = width.toInt()
        val ih = height.toInt()

        ctx.pose().translate(fx, fy)
        ctx.fillGradient(0, 0, iw, ih, color1.rgb, color2.rgb)
        ctx.pose().translate(- fx, - fy)
    }
}