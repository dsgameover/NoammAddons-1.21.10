package com.github.noamm9.utils.render

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.render.RenderHelper.renderX
import com.github.noamm9.utils.render.RenderHelper.renderY
import com.github.noamm9.utils.render.RenderHelper.renderZ
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import org.joml.Matrix4f
import java.awt.Color

object Render3D {
    fun renderBlock(
        ctx: RenderContext,
        pos: BlockPos,
        color: Color,
        outline: Boolean = true,
        fill: Boolean = true,
        phase: Boolean = false
    ) {
        val state = mc.level?.getBlockState(pos) ?: return
        val mstack = ctx.matrixStack ?: return
        val consumers = ctx.consumers ?: return
        val camPos = ctx.camera.position
        val shape = state.getShape(mc.level !!, pos, CollisionContext.of(ctx.camera.entity))

        if (shape.isEmpty) return renderBox(ctx, pos.x + 0.5, pos.y, pos.z + 0.5, 1.0, 1.0, color, outline, fill, phase)

        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val a = color.alpha / 255f

        shape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            val x1 = pos.x + minX
            val y1 = pos.y + minY
            val z1 = pos.z + minZ
            val x2 = pos.x + maxX
            val y2 = pos.y + maxY
            val z2 = pos.z + maxZ

            if (fill) {
                val layer = if (phase) NoammRenderLayers.FILLED_THROUGH_WALLS else NoammRenderLayers.FILLED
                ShapeRenderer.addChainedFilledBoxVertices(
                    mstack,
                    consumers.getBuffer(layer),
                    x1 - camPos.x, y1 - camPos.y, z1 - camPos.z,
                    x2 - camPos.x, y2 - camPos.y, z2 - camPos.z,
                    r, g, b, a
                )
            }

            if (outline) {
                val layer = if (phase) NoammRenderLayers.getLinesThroughWalls(2.5) else NoammRenderLayers.getLines(2.5)
                ShapeRenderer.renderLineBox(
                    mstack.last(),
                    consumers.getBuffer(layer),
                    x1 - camPos.x, y1 - camPos.y, z1 - camPos.z,
                    x2 - camPos.x, y2 - camPos.y, z2 - camPos.z,
                    r, g, b, 1f
                )
            }
        }
    }

    fun renderBox(
        ctx: RenderContext,
        x: Number, y: Number, z: Number,
        width: Number, height: Number,
        color: Color = Color.CYAN,
        outline: Boolean = true,
        fill: Boolean = true,
        phase: Boolean = false
    ) {
        if (! outline && ! fill) return
        val consumers = ctx.consumers ?: return
        val matrices = ctx.matrixStack ?: return
        val cam = ctx.camera.position.reverse()

        val xd = x.toDouble();
        val yd = y.toDouble();
        val zd = z.toDouble()
        val hw = width.toDouble() / 2.0
        val hd = height.toDouble()

        val r = color.red / 255f;
        val g = color.green / 255f;
        val b = color.blue / 255f;
        val a = color.alpha / 255f

        matrices.pushPose()
        matrices.translate(cam.x, cam.y, cam.z)

        if (fill) {
            val layer = if (phase) NoammRenderLayers.FILLED_THROUGH_WALLS else NoammRenderLayers.FILLED
            ShapeRenderer.addChainedFilledBoxVertices(matrices, consumers.getBuffer(layer), xd - hw, yd, zd - hw, xd + hw, yd + hd, zd + hw, r, g, b, a)
        }

        if (outline) {
            val layer = if (phase) NoammRenderLayers.getLinesThroughWalls(2.5) else NoammRenderLayers.getLines(2.5)
            ShapeRenderer.renderLineBox(matrices.last(), consumers.getBuffer(layer), xd - hw, yd, zd - hw, xd + hw, yd + hd, zd + hw, r, g, b, 1f)
        }
        matrices.popPose()
    }

    fun renderString(
        text: String,
        x: Number, y: Number, z: Number,
        scale: Float = 1f,
        bgBox: Boolean = false,
        phase: Boolean = false
    ) {
        val toScale = scale * 0.025f
        val matrices = Matrix4f()
        val textRenderer = mc.font
        val camera = mc.gameRenderer.mainCamera
        val dx = (x.toDouble() - camera.position.x).toFloat()
        val dy = (y.toDouble() - camera.position.y).toFloat()
        val dz = (z.toDouble() - camera.position.z).toFloat()

        matrices.translate(dx, dy, dz).rotate(camera.rotation()).scale(toScale, - toScale, toScale)

        val consumer = mc.renderBuffers().bufferSource()
        val textLayer = if (phase) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
        val lines = text.split("\n")
        val maxWidth = lines.maxOf { textRenderer.width(it) }
        val offset = - maxWidth / 2f

        if (bgBox) {
            val widestLine = lines.maxByOrNull { textRenderer.width(it) } ?: ""
            for ((i, _) in lines.withIndex()) {
                textRenderer.drawInBatch(widestLine, offset, i * 9f, 0x20FFFFFF, true, matrices, consumer, textLayer, (mc.options.getBackgroundOpacity(0.25f) * 255).toInt() shl 24, LightTexture.FULL_BLOCK)
            }
        }

        for ((i, line) in lines.withIndex()) {
            textRenderer.drawInBatch(line, - textRenderer.width(line) / 2f, i * 9f, 0xFFFFFFFF.toInt(), true, matrices, consumer, textLayer, 0, LightTexture.FULL_BLOCK)
        }
        consumer.endBatch()
    }

    fun renderString(text: String, entity: Entity, scale: Float = 1f, phase: Boolean = false) {
        renderString(text, entity.renderX, entity.renderY + entity.bbHeight + 0.5, entity.renderZ, scale, bgBox = true, phase = phase)
    }

    fun renderLine(ctx: RenderContext, start: Vec3, finish: Vec3, thickness: Number, color: Color) {
        val matrices = ctx.matrixStack ?: return
        val cameraPos = mc.gameRenderer.mainCamera.position
        matrices.pushPose()
        matrices.translate(- cameraPos.x, - cameraPos.y, - cameraPos.z)

        val buffer = (ctx.consumers as MultiBufferSource.BufferSource).getBuffer(RenderType.lines())
        RenderSystem.lineWidth(thickness.toFloat())

        val r = color.red / 255f;
        val g = color.green / 255f;
        val b = color.blue / 255f;
        val a = color.alpha / 255f
        val direction = finish.subtract(start).normalize().toVector3f()
        val entry = matrices.last()

        buffer.addVertex(entry, start.x.toFloat(), start.y.toFloat(), start.z.toFloat()).setColor(r, g, b, a).setNormal(entry, direction)
        buffer.addVertex(entry, finish.x.toFloat(), finish.y.toFloat(), finish.z.toFloat()).setColor(r, g, b, a).setNormal(entry, direction)

        ctx.consumers.endBatch(RenderType.lines())
        matrices.popPose()
    }

    fun renderLine(ctx: RenderContext, start: BlockPos, end: BlockPos, thickness: Number, color: Color) {
        renderLine(ctx, Vec3.atCenterOf(start), Vec3.atCenterOf(end), thickness, color)
    }

    fun renderTracer(ctx: RenderContext, point: Vec3, color: Color, thickness: Number) {
        val camera = ctx.camera
        val matrixStack = ctx.matrixStack ?: return
        val consumers = ctx.consumers
        val cameraPos = camera.position

        matrixStack.pushPose()
        matrixStack.translate(- cameraPos.x, - cameraPos.y, - cameraPos.z)

        val buffer = (consumers as MultiBufferSource.BufferSource).getBuffer(NoammRenderLayers.getLinesThroughWalls(2.5))
        val cameraPoint = cameraPos.add(Vec3.directionFromRotation(camera.xRot, camera.yRot))
        val normal = point.toVector3f().sub(cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat()).normalize()
        val entry = matrixStack.last() ?: return

        RenderSystem.lineWidth(thickness.toFloat())

        buffer.addVertex(entry, cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat()).setColor(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f).setNormal(entry, normal)
        buffer.addVertex(entry, point.x.toFloat(), point.y.toFloat(), point.z.toFloat()).setColor(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f).setNormal(entry, normal)

        consumers.endBatch(RenderType.lines())
        matrixStack.popPose()
    }

    fun renderTracer(ctx: RenderContext, point: BlockPos, color: Color, thickness: Number) {
        renderTracer(ctx, Vec3.atCenterOf(point), color, thickness)
    }
}