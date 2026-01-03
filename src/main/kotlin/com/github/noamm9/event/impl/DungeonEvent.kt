package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import com.github.noamm9.utils.dungeons.Puzzle
import com.github.noamm9.utils.dungeons.map.core.RoomState
import net.minecraft.core.BlockPos

abstract class DungeonEvent: Event(false) {
    abstract class PuzzleEvent(val pazzle: Puzzle): DungeonEvent() {
        class Reset(pazzle: Puzzle): PuzzleEvent(pazzle)
        class Discovered(pazzle: Puzzle): PuzzleEvent(pazzle)
        class Completed(pazzle: Puzzle): PuzzleEvent(pazzle)
        class Failed(pazzle: Puzzle): PuzzleEvent(pazzle)
    }
    /*
    abstract class RoomEvent(val room: UniqueRoom): DungeonEvent() {
        class onEnter(room: UniqueRoom): RoomEvent(room)
        class onExit(room: UniqueRoom): RoomEvent(room)

        class onStateChange(room: UniqueRoom, val oldState: RoomState, val newState: RoomState, val roomPlayers: List<DungeonUtils.DungeonPlayer>): RoomEvent(room)
    }*/

    class SecretEvent(val type: SecretType, val pos: BlockPos): DungeonEvent() {
        enum class SecretType { CHEST, SKULL, ITEM, BAT, LAVER }
    }

    class PlayerDeathEvent(val name: String, val reason: String): DungeonEvent()

    class BossEnterEvent: DungeonEvent()

    class RunStatedEvent: DungeonEvent()
    class RunEndedEvent: DungeonEvent()
}