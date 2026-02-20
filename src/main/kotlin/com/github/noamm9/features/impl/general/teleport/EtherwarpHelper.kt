package com.github.noamm9.features.impl.general.teleport

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.MathUtils.add
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.items.ItemUtils.customData
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.piston.PistonHeadBlock
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sign

object EtherwarpHelper {
    private const val EYE_HEIGHT = 1.62
    private const val SNEAK_OFFSET = 0.08

    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?) {
        companion object {
            val NONE = EtherPos(false, null)
        }
    }

    fun getEtherwarpDistance(stack: ItemStack): Double? {
        if (stack.skyblockId.equalsOneOf("ASPECT_OF_THE_VOID", "ASPECT_OF_THE_END")) {
            val nbt = stack.customData
            if (nbt.getByte("ethermerge").orElse(0) != 1.toByte()) return null
            val tuners = nbt.getByte("tuned_transmission").orElse(0).toInt()
            return 57.0 + tuners
        }
        return null
    }

    fun getEtherPos(pos: Vec3, distance: Double = 60.0): EtherPos {
        val player = mc.player ?: return EtherPos.NONE
        val startPos = pos.add(y = EYE_HEIGHT - if (player.isCrouching) SNEAK_OFFSET else 0.0)
        val endPos = startPos.add(player.lookAngle.scale(distance))
        return traverseVoxels(startPos, endPos)
    }

    private fun traverseVoxels(start: Vec3, end: Vec3): EtherPos {
        var x = floor(start.x).toInt()
        var y = floor(start.y).toInt()
        var z = floor(start.z).toInt()

        val endX = floor(end.x).toInt()
        val endY = floor(end.y).toInt()
        val endZ = floor(end.z).toInt()

        val dirX = end.x - start.x
        val dirY = end.y - start.y
        val dirZ = end.z - start.z

        val stepX = sign(dirX).toInt()
        val stepY = sign(dirY).toInt()
        val stepZ = sign(dirZ).toInt()

        val tDeltaX = if (dirX == 0.0) Double.POSITIVE_INFINITY else abs(1.0 / dirX)
        val tDeltaY = if (dirY == 0.0) Double.POSITIVE_INFINITY else abs(1.0 / dirY)
        val tDeltaZ = if (dirZ == 0.0) Double.POSITIVE_INFINITY else abs(1.0 / dirZ)

        var tMaxX = if (dirX == 0.0) Double.POSITIVE_INFINITY else abs((floor(start.x) + max(0.0, stepX.toDouble()) - start.x) / dirX)
        var tMaxY = if (dirY == 0.0) Double.POSITIVE_INFINITY else abs((floor(start.y) + max(0.0, stepY.toDouble()) - start.y) / dirY)
        var tMaxZ = if (dirZ == 0.0) Double.POSITIVE_INFINITY else abs((floor(start.z) + max(0.0, stepZ.toDouble()) - start.z) / dirZ)

        val currentPos = BlockPos.MutableBlockPos()

        repeat(1000) {
            currentPos.set(x, y, z)

            val chunk = mc.level?.getChunk(
                SectionPos.blockToSectionCoord(x),
                SectionPos.blockToSectionCoord(z)
            ) ?: return EtherPos.NONE

            val state = chunk.getBlockState(currentPos)
            val id = Block.getId(state)

            if (isValidEtherwarpBlock(currentPos, id, chunk)) return EtherPos(true, currentPos)
            if (! validEtherwarpFeetIds[id]) return if (state.isAir) EtherPos.NONE else EtherPos(false, currentPos)
            if (x == endX && y == endY && z == endZ) return if (state.isAir) EtherPos.NONE else EtherPos(false, currentPos)

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    tMaxX += tDeltaX
                    x += stepX
                }
                else {
                    tMaxZ += tDeltaZ
                    z += stepZ
                }
            }
            else {
                if (tMaxY < tMaxZ) {
                    tMaxY += tDeltaY
                    y += stepY
                }
                else {
                    tMaxZ += tDeltaZ
                    z += stepZ
                }
            }
        }

        return EtherPos.NONE
    }

    private fun isValidEtherwarpBlock(currentPos: BlockPos, currendId: Int, chunk: LevelChunk): Boolean {
        if (currendId == 0 || validEtherwarpFeetIds[currendId]) return false
        if (! validEtherwarpFeetIds[Block.getId(chunk.getBlockState(currentPos.above()))]) return false
        return validEtherwarpFeetIds[Block.getId(chunk.getBlockState(currentPos.above(2)))]
    }

    private val validTypes = setOf(
        ButtonBlock::class, CarpetBlock::class, SkullBlock::class,
        WallSkullBlock::class, LadderBlock::class, SaplingBlock::class,
        FlowerBlock::class, StemBlock::class, CropBlock::class,
        RailBlock::class, SnowLayerBlock::class, BubbleColumnBlock::class,
        TripWireBlock::class, TripWireHookBlock::class, FireBlock::class,
        AirBlock::class, TorchBlock::class, FlowerPotBlock::class,
        TallFlowerBlock::class, TallGrassBlock::class, BushBlock::class,
        SeagrassBlock::class, TallSeagrassBlock::class, SugarCaneBlock::class,
        LiquidBlock::class, VineBlock::class, MushroomBlock::class,
        PistonHeadBlock::class, WebBlock::class,
        NetherWartBlock::class, NetherPortalBlock::class, RedStoneWireBlock::class,
        ComparatorBlock::class, RedstoneTorchBlock::class, RepeaterBlock::class
    )

    private val validEtherwarpFeetIds = BitSet().apply {
        BuiltInRegistries.BLOCK.forEach { block ->
            if (validTypes.any { it.isInstance(block) }) {
                set(Block.getId(block.defaultBlockState()))
            }
        }
    }
}