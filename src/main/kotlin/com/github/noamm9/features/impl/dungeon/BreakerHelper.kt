package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.features.Feature
import com.github.noamm9.utils.ItemUtils.skyblockId
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.dungeons.map.core.RoomType
import com.github.noamm9.utils.dungeons.map.utils.ScanUtils
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.world.WorldUtils
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.Blocks

object BreakerHelper: Feature("Zero Ping Dungeon Breaker") {
    private val blacklist = setOf(
        Blocks.BARRIER, Blocks.BEDROCK, Blocks.COMMAND_BLOCK, Blocks.TNT, Blocks.CHEST, Blocks.PLAYER_HEAD,
        Blocks.PLAYER_WALL_HEAD, Blocks.TRAPPED_CHEST, Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL, Blocks.STICKY_PISTON,
        Blocks.PISTON_HEAD, Blocks.PISTON, Blocks.MOVING_PISTON, Blocks.LEVER, Blocks.STONE_BUTTON,
        Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL
    )

    @JvmStatic
    fun onHitBlock(pos: BlockPos) {
        if (! enabled) return
        if (! LocationUtils.inDungeon) return
        if (LocationUtils.inBoss) return
        if (mc.player?.mainHandItem?.skyblockId != "DUNGEONBREAKER") return
        if (ScanUtils.currentRoom?.data?.type.equalsOneOf(RoomType.PUZZLE, RoomType.FAIRY)) return
        val state = WorldUtils.getStateAt(pos).takeUnless { it.block in blacklist } ?: return

        mc.level?.removeBlock(pos, false)
        mc.level?.playLocalSound(
            pos.x + 0.5,
            pos.y + 0.5,
            pos.z + 0.5,
            state.soundType.breakSound,
            SoundSource.BLOCKS,
            (state.soundType.volume + 1.0f) / 2.0f,
            state.soundType.pitch * 0.8f,
            false
        )
    }
}