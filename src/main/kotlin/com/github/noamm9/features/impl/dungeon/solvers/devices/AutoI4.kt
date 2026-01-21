package com.github.noamm9.features.impl.dungeon.solvers.devices

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.ui.utils.Animation.Companion.easeInOutCubic
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.MathUtils
import com.github.noamm9.utils.MathUtils.calcYawPitch
import com.github.noamm9.utils.MathUtils.interpolateYaw
import com.github.noamm9.utils.MathUtils.lerp
import com.github.noamm9.utils.PlayerUtils
import com.github.noamm9.utils.PlayerUtils.leapAction
import com.github.noamm9.utils.PlayerUtils.rotate
import com.github.noamm9.utils.ThreadUtils.setTimeout
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.world.WorldUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.min


object AutoI4: Feature("Fully Automated I4") {
    private val rotationTime by SliderSetting<Long>("Rotation Time", 170, 0, 250, 1).withDescription("Time (ms) to interpolate rotations when aiming at dev block targets.")
    private val predictSetting by ToggleSetting("Predictions", true).withDescription("Enables prediction logic to aim at the next target block.")

    private val rodSetting by ToggleSetting("Auto Rod", true)
    private val maskSetting by ToggleSetting("Auto Mask", true)
    private val leapSetting by ToggleSetting("Auto Leap", true)
    private val leapPriorities = listOf("Tank", "Mage", "Healer", "Archer")
    private val preferredLeapClass by DropdownSetting("Leap Priority", 0, leapPriorities)

    private const val STORM_DEATH_MESSAGE = "[BOSS] Storm: I should have known that I stood no chance."
    private val DEVICE_DONE_REGEX = Regex("^(\\w{3,16}) completed a device! \\(\\d/\\d\\)$")

    private val devBlocks = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )

    private data class PhaseState(
        val tickTimer: Int = - 1,
        val lastEmeraldTick: Int = - 1,
        val doneCoords: Set<BlockPos> = emptySet(),
        val hasLeaped: Boolean = false,
        val hasChangedMask: Boolean = false,
        val hasAlerted: Boolean = false
    )

    private data class Action(val priority: Int, val block: suspend () -> Unit)

    private var state = PhaseState()
    private val deviceActionQueue = ArrayDeque<Action>()
    private var deviceJob: Job? = null

    override fun init() {
        register<TickEvent.Server> {
            if (state.tickTimer == - 1) return@register
            state = state.copy(tickTimer = state.tickTimer + 1)
            if (! isOnDev()) return@register

            if (state.tickTimer == 174 && rodSetting.value) queue(2) { PlayerUtils.rodSwap() }
            else if (state.tickTimer == 174 && maskSetting.value && ! state.hasChangedMask) {
                state = state.copy(hasChangedMask = true)
                queue(3) { PlayerUtils.changeMaskAction() }
            }
            if (state.tickTimer == 307 && leapSetting.value && ! state.hasLeaped) queue(4) { saveLeap() }

            devBlocks.forEach { pos ->
                val block = mc.level?.getBlockState(pos)?.block ?: return@forEach
                if (block == Blocks.EMERALD_BLOCK && pos !in state.doneCoords) {
                    state = state.copy(lastEmeraldTick = state.tickTimer, doneCoords = state.doneCoords + pos)

                    queue(1) {
                        shootAtBlock(pos)

                        if (predictSetting.value) {
                            getPredictionTarget(pos)?.let { nextTarget ->
                                val preCheckEmerald = findAnyEmeraldExcluding(pos, nextTarget)
                                if (preCheckEmerald != null) return@queue
                                shootAtBlock(nextTarget)
                            }
                        }
                    }
                }
            }

            val ticksSinceLastEmerald = if (state.lastEmeraldTick < 0) Int.MAX_VALUE else (state.tickTimer - state.lastEmeraldTick)
            val shouldCheckForStall = rotationTime.value > 0 && state.tickTimer > 150 && ticksSinceLastEmerald > 30 && state.doneCoords.size > 4
            if (shouldCheckForStall) {
                val hasEmeraldBlock = devBlocks.any { mc.level?.getBlockState(it)?.block == Blocks.EMERALD_BLOCK }
                if (hasEmeraldBlock) state = state.copy(lastEmeraldTick = state.tickTimer)
                else onComplete("Device stalled")
            }
        }

        register<ChatMessageEvent> {
            val msg = event.unformattedText
            if (msg.contains(STORM_DEATH_MESSAGE)) state = PhaseState(tickTimer = 0).also {
                setTimeout(30_000L) { state = state.copy(tickTimer = - 1) }
            }
            else if (msg.contains("completed a device!")) {
                if (! (state.tickTimer >= 0 && isOnDev() && leapSetting.value)) return@register
                if (DEVICE_DONE_REGEX.find(msg)?.groupValues?.get(1) != mc.user.name) return@register
                onComplete("Completed Device")
            }
        }
    }

    private fun findAnyEmeraldExcluding(vararg exclude: BlockPos?): BlockPos? {
        return devBlocks.find { pos ->
            if (pos in exclude) return@find false
            if (pos in state.doneCoords) return@find false
            WorldUtils.getBlockAt(pos) == Blocks.EMERALD_BLOCK
        }
    }

    private suspend fun shootAtBlock(pos: BlockPos) {
        val player = mc.player ?: return
        val targetVec = getTargetVector(pos)
        val (yaw, pitch) = calcYawPitch(targetVec)
        val block = suspend {
            delay(50)
            PlayerUtils.rightClick()
        }

        findAnyEmeraldExcluding(pos)?.let { newer ->
            state = state.copy(lastEmeraldTick = state.tickTimer, doneCoords = state.doneCoords + newer)
            return shootAtBlock(newer)
        }

        val currentYaw = MathUtils.normalizeYaw(player.yRot)
        val currentPitch = MathUtils.normalizePitch(player.xRot)
        val targetYaw = MathUtils.normalizeYaw(yaw)
        val targetPitch = MathUtils.normalizePitch(pitch)
        val tolerance = 1f

        if (abs(currentYaw - targetYaw) <= tolerance && abs(currentPitch - targetPitch) <= tolerance) return block()

        val startTime = System.currentTimeMillis()
        while (true) {
            val newerDuring = findAnyEmeraldExcluding(pos)
            if (newerDuring != null) {
                state = state.copy(lastEmeraldTick = state.tickTimer, doneCoords = state.doneCoords + newerDuring)
                return shootAtBlock(newerDuring)
            }

            val elapsed = System.currentTimeMillis() - startTime
            val progress = if (rotationTime.value <= 0) 1.0 else min(elapsed.toDouble() / rotationTime.value, 1.0)

            if (progress >= 1) {
                rotate(targetYaw, targetPitch)
                block()
                break
            }

            val easedProgress = easeInOutCubic(progress).toFloat()
            val newYaw = interpolateYaw(currentYaw, targetYaw, easedProgress)
            val newPitch = lerp(currentPitch, targetPitch, easedProgress).toFloat()
            rotate(newYaw, newPitch)
        }
    }

    private fun getTargetVector(pos: BlockPos): Vec3 {
        val i = devBlocks.indexOf(pos).coerceAtLeast(0)
        val col = i % 3
        val row = i / 3

        val isBlockToTheRightDone = (col < 2) && (i + 1 < devBlocks.size) && (devBlocks[i + 1] in state.doneCoords)
        val targetX = if (col == 0 || isBlockToTheRightDone) 67.5 else 65.5

        val targetY = 131.3 - 2.0 * row
        return Vec3(targetX, targetY, 50.0)
    }

    private fun queue(priority: Int, block: suspend () -> Unit) {
        deviceActionQueue.add(Action(priority, block))
        deviceActionQueue.sortByDescending { it.priority }
        if (deviceJob?.isActive != true) {
            processDeviceQueue()
        }
    }

    private fun processDeviceQueue() {
        deviceJob = scope.launch {
            while (deviceActionQueue.isNotEmpty()) {
                runCatching {
                    deviceActionQueue.removeFirst().block()
                }
            }
        }
    }


    private suspend fun saveLeap() {
        if (! leapSetting.value) return
        if (state.hasLeaped) return
        state = state.copy(hasLeaped = true, tickTimer = - 1)
        val aliveTeammates = DungeonListener.leapTeammates.filterNot { it.isDead }

        val preferredClass = leapPriorities[preferredLeapClass.value]
        val target = aliveTeammates.find { it.clazz.name == preferredClass }
            ?: leapPriorities.firstNotNullOfOrNull { priority ->
                aliveTeammates.find { it.clazz.name == priority }
            } ?: return

        leapAction(target)
        while (isOnDev()) delay(50)
    }

    private fun onComplete(reason: String) {
        if (state.hasAlerted) return
        state = state.copy(hasAlerted = true, tickTimer = - 1)
        val remaining = devBlocks.size - state.doneCoords.size
        queue(4) {
            saveLeap()
            ChatUtils.modMessage("Predicted $remaining/9")
            ChatUtils.modMessage("Trigger: $reason")
        }
    }

    private fun getPredictionTarget(lastHitPos: BlockPos): BlockPos? {
        return devBlocks.shuffled().find { potentialTarget ->
            val isNotDone = ! state.doneCoords.contains(potentialTarget)
            val block = WorldUtils.getBlockAt(potentialTarget)
            val isCorrectBlockType = block != Blocks.EMERALD_BLOCK && block != Blocks.AIR
            val isNonAdjacentInSameColumn = potentialTarget.x == lastHitPos.x && potentialTarget.distSqr(lastHitPos) > 4.0
            isNotDone && isCorrectBlockType && ! isNonAdjacentInSameColumn
        }
    }

    private fun isOnDev(): Boolean {
        val playerPos = mc.player?.position() ?: return false
        return abs(playerPos.y - 127.0) < 0.5 && playerPos.x in 62.0 .. 65.0 && playerPos.z in 34.0 .. 37.0
    }
}