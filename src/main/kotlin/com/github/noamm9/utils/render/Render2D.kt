package com.github.noamm9.utils.render

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.ChatUtils.addColor
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.sqrt

object Render2D {
    fun drawImage(ctx: GuiGraphics, image: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        ctx.blitSprite(RenderPipelines.GUI_TEXTURED, image, x, y, width, height)
    }

    fun drawTexture(ctx: GuiGraphics, texture: ResourceLocation, x: Number, y: Number, width: Number, height: Number) {
        ctx.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }

    fun drawRect(ctx: GuiGraphics, x: Number, y: Number, width: Number, height: Number, color: Color = Color.WHITE) {
        val pose = ctx.pose()
        pose.translate(x.toFloat(), y.toFloat())
        ctx.fill(0, 0, width.toInt(), height.toInt(), color.rgb)
        pose.translate(- x.toFloat(), - y.toFloat())
    }

    fun drawBorder(
        ctx: GuiGraphics,
        x: Number,
        y: Number,
        width: Number,
        height: Number,
        color: Color = Color.WHITE,
        thickness: Number = 1,
    ) {
        val X = x.toDouble()
        val Y = y.toDouble()
        val W = width.toDouble()
        val H = height.toDouble()
        val T = thickness.toDouble()

        drawRect(ctx, X, Y, W, T, color)
        drawRect(ctx, X, Y + H - T, W, T, color)
        drawRect(ctx, X, Y + T, T, H - (T * 2), color)
        drawRect(ctx, X + W - T, Y + T, T, H - (T * 2), color)
    }

    fun drawLine(ctx: GuiGraphics, x1: Number, y1: Number, x2: Number, y2: Number, color: Color, thickness: Number = 1) {
        val pose = ctx.pose()
        val fx1 = x1.toFloat()
        val fy1 = y1.toFloat()
        val fx2 = x2.toFloat()
        val fy2 = y2.toFloat()
        val iThick = thickness.toInt()

        val dx = fx2 - fx1
        val dy = fy2 - fy1
        val distance = sqrt(dx * dx + dy * dy).toInt()
        val angle = atan2(dy, dx)

        pose.translate(fx1, fy1)
        pose.rotate(angle)

        ctx.fill(0, 0, distance, iThick, color.rgb)

        pose.rotate(- angle)
        pose.translate(- fx1, - fy1)
    }

    @JvmOverloads
    fun drawString(ctx: GuiGraphics, str: String, x: Number, y: Number, color: Color = Color.WHITE, scale: Number = 1, shadow: Boolean = true) {
        val pose = ctx.pose()
        val fx = x.toFloat()
        val fy = y.toFloat()
        val fScale = scale.toFloat()

        pose.translate(fx, fy)
        if (fScale != 1f) pose.scale(fScale, fScale)
        ctx.drawString(mc.font, str.addColor(), 0, 0, color.rgb, shadow)
        if (fScale != 1f) pose.scale(1f / fScale, 1f / fScale)
        pose.translate(- fx, - fy)
    }

    fun drawCenteredString(ctx: GuiGraphics, str: String, x: Number, y: Number, color: Color = Color.WHITE, scale: Number = 1, shadow: Boolean = true) {
        val fScale = scale.toFloat()
        val totalScaledWidth = with(Render2D) { str.width() } * fScale
        val centerX = x.toFloat() - (totalScaledWidth / 2f)
        drawString(ctx, str, centerX, y, color, scale, shadow)
    }

    fun renderItem(context: GuiGraphics, item: ItemStack, x: Float, y: Float, scale: Float) {
        context.pose().pushMatrix()
        context.pose().translate(x, y)
        context.pose().scale(scale, scale)

        context.renderItem(item, 0, 0)

        context.pose().popMatrix()
    }


    fun drawPlayerHead(context: GuiGraphics, x: Int, y: Int, size: Int, skin: ResourceLocation) {
        context.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8f, 8f, size, size, 8, 8, 64, 64, - 1)
        context.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 40f, 8f, size, size, 8, 8, 64, 64, - 1)
    }

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { mc.font.width(it.addColor()) }
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

    fun drawCircle(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int, color: Color = Color.WHITE) {
        var x = 0
        var y = radius
        var d = 3 - 2 * radius

        while (x <= y) {
            ctx.hLine(cx - x, cx + x, cy + y, color.rgb)
            ctx.hLine(cx - x, cx + x, cy - y, color.rgb)
            ctx.hLine(cx - y, cx + y, cy + x, color.rgb)
            ctx.hLine(cx - y, cx + y, cy - x, color.rgb)

            if (d < 0) d += 4 * x + 6
            else {
                d += 4 * (x - y) + 10
                y --
            }
            x ++
        }
    }

    fun drawRoundedRect(ctx: GuiGraphics, x: Number, y: Number, width: Number, height: Number, radius: Number, color: Color) {
        val xInt = x.toInt() * 4
        val yInt = y.toInt() * 4
        val wInt = width.toInt() * 4
        val hInt = height.toInt() * 4
        val rInt = radius.toInt() * 4

        ctx.pose().pushMatrix()
        ctx.pose().scale(1 / 4f)

        ctx.fill(xInt + rInt, yInt, xInt + wInt - rInt, yInt + hInt, color.rgb)
        ctx.fill(xInt, yInt + rInt, xInt + rInt, yInt + hInt - rInt, color.rgb)
        ctx.fill(xInt + wInt - rInt, yInt + rInt, xInt + wInt, yInt + hInt - rInt, color.rgb)

        var dx = 0
        var dy = rInt
        var p = 3 - 2 * rInt

        val xLeft = xInt + rInt
        val xRight = xInt + wInt - rInt - 1
        val yTop = yInt + rInt
        val yBottom = yInt + hInt - rInt - 1

        while (dx <= dy) {
            ctx.hLine(xLeft - dx, xLeft, yTop - dy, color.rgb)
            ctx.hLine(xLeft - dy, xLeft, yTop - dx, color.rgb)

            ctx.hLine(xRight, xRight + dx, yTop - dy, color.rgb)
            ctx.hLine(xRight, xRight + dy, yTop - dx, color.rgb)

            ctx.hLine(xLeft - dx, xLeft, yBottom + dy, color.rgb)
            ctx.hLine(xLeft - dy, xLeft, yBottom + dx, color.rgb)

            ctx.hLine(xRight, xRight + dx, yBottom + dy, color.rgb)
            ctx.hLine(xRight, xRight + dy, yBottom + dx, color.rgb)

            if (p < 0) p += 4 * dx + 6
            else {
                p += 4 * (dx - dy) + 10
                dy --
            }
            dx ++
        }

        ctx.pose().popMatrix()
    }
}