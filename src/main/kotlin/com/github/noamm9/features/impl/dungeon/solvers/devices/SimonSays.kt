package com.github.noamm9.features.impl.dungeon.solvers.devices

import com.github.noamm9.event.impl.*
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.utils.ChatUtils.unformattedText
import com.github.noamm9.utils.PlayerUtils
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.NoammRenderLayers
import com.github.noamm9.utils.render.RenderContext
import com.github.noamm9.utils.world.WorldUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.block.Blocks
import java.awt.Color

object SimonSays: Feature("Simon Says Solver") {
    private val ssSkip by ToggleSetting("SS skip Compatibility").withDescription("Always assume at the start that u perfectly ss skip").section("Options")
    private val blockWrongClicks by ToggleSetting("Block Wrong Clicks").withDescription("Blocks clicks if you aren't looking at the correct button. &eSneak to override.")

    private val color1 by ColorSetting("First Color", Color.GREEN).withDescription("Color of the first button.").section("Colors")
    private val color2 by ColorSetting("Second Color", Color.YELLOW).withDescription("Color of the second button.")
    private val color3 by ColorSetting("Other Color", Color.RED).withDescription("Color of the rest of the buttons.")


    private val isSimonSaysActive get() = enabled && LocationUtils.F7Phase == 3
    private val solution = ArrayList<BlockPos>()
    private var lastExisted = false
    private var skipOver = false
    private var allObi = true
    private var lastClick = 0L

    private val startButton = BlockPos(110, 121, 91)
    private val buttonCheckPos = BlockPos(110, 120, 92)
    private val startPos = BlockPos(111, 120, 92)

    private var job: Job? = null


    override fun init() {
        register<WorldChangeEvent> { reset() }


        register<TickEvent.Start> {
            if (! isSimonSaysActive) return@register
            val buttonsExist = WorldUtils.getBlockAt(buttonCheckPos) == Blocks.STONE_BUTTON

            if (buttonsExist && ! lastExisted) {
                allObi = true

                for (dy in 0 .. 3) {
                    for (dz in 0 .. 3) {
                        val pos = startPos.offset(0, dy, dz)
                        if (WorldUtils.getBlockAt(pos) != Blocks.OBSIDIAN) {
                            allObi = false
                        }
                    }
                }

                if (allObi) {
                    lastExisted = true
                    skipOver = true

                }
            }

            if (! buttonsExist && lastExisted) {
                lastExisted = false
                solution.clear()
            }

            for (dy in 0 .. 3) {
                for (dz in 0 .. 3) {
                    val pos = startPos.offset(0, dy, dz)
                    val block = WorldUtils.getBlockAt(pos)
                    if (block != Blocks.SEA_LANTERN || solution.contains(pos)) continue

                    solution.add(pos)

                    if (! skipOver && ssSkip.value && solution.size == 3) {
                        solution.removeAt(0)
                    }
                }
            }
        }

        register<RenderWorldEvent> {
            if (! isSimonSaysActive) return@register
            if (solution.isEmpty()) return@register

            for (i in solution.indices) {
                val color = when (i) {
                    0 -> color1
                    1 -> color2
                    else -> color3
                }.value

                renderSSBox(event.ctx, solution[i].west(), color)
            }
        }

        register<PlayerInteractEvent.RIGHT_CLICK.BLOCK> { handleClick(event, event.pos) }
        register<PlayerInteractEvent.LEFT_CLICK.BLOCK> { handleClick(event, event.pos) }
    }

    private fun handleClick(event: PlayerInteractEvent, clickedPos: BlockPos) {
        if (! isSimonSaysActive) return

        if (clickedPos.x == 110 && clickedPos.y == 121 && clickedPos.z == 91) {
            solution.clear()
            skipOver = false
            return
        }

        if (solution.isEmpty()) return
        if (WorldUtils.getBlockAt(clickedPos) != Blocks.STONE_BUTTON) return
        if (lastClick == DungeonListener.currentTime) return event.cancel()
        lastClick = DungeonListener.currentTime

        val checkPos = clickedPos.east()
        val expected = solution.firstOrNull() ?: return

        if (checkPos != expected) {
            if (blockWrongClicks.value && ! mc.player !!.isCrouching) return event.cancel()

            if (solution.size == 3 && checkPos == solution[1]) {
                for (i in 1 downTo 0) solution.removeAt(i)
            }
        }
        else solution.remove(expected)
    }

    private fun reset() {
        lastExisted = false
        skipOver = false
        solution.clear()
        allObi = true
    }

    private fun renderSSBox(ctx: RenderContext, pos: BlockPos, color: Color) {
        val consumers = ctx.consumers ?: return
        val matrices = ctx.matrixStack ?: return
        val cam = ctx.camera.position.reverse()

        val w = 0.4 / 2.0
        val h = 0.26 / 2.0

        val cx = pos.x + 1.0
        val cy = pos.y + 0.5
        val cz = pos.z + 0.5

        val minX = cx - 0.2
        val minY = cy - h
        val maxY = cy + h
        val minZ = cz - w
        val maxZ = cz + w

        matrices.pushPose()
        matrices.translate(cam.x, cam.y, cam.z)

        ShapeRenderer.addChainedFilledBoxVertices(
            matrices,
            consumers.getBuffer(NoammRenderLayers.FILLED_THROUGH_WALLS),
            minX, minY, minZ,
            cx, maxY, maxZ,
            color.red / 255f, color.green / 255f, color.blue / 255f, 0.7f
        )

        matrices.popPose()
    }
}