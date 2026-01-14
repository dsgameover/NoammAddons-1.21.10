package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import com.github.noamm9.utils.dungeons.DungeonPlayer
import com.github.noamm9.utils.dungeons.Puzzle
import com.github.noamm9.utils.dungeons.map.core.RoomState
import com.github.noamm9.utils.dungeons.map.core.UniqueRoom
import net.minecraft.core.BlockPos

abstract class DungeonEvent: Event(false) {
    class PuzzleEvent(val pazzle: Puzzle, val type: Type): DungeonEvent() {
        enum class Type { RESET, DISCOVERE, COMPLETE, FAIL }
    }

    abstract class RoomEvent(val room: UniqueRoom): DungeonEvent() {
        class onEnter(room: UniqueRoom): RoomEvent(room)
        class onExit(room: UniqueRoom): RoomEvent(room)

        class onStateChange(room: UniqueRoom, val oldState: RoomState, val newState: RoomState, val roomPlayers: List<DungeonPlayer>): RoomEvent(room)
    }

    class SecretEvent(val type: SecretType, val pos: BlockPos): DungeonEvent() {
        enum class SecretType { CHEST, SKULL, ITEM, BAT, LEVER }
    }


    class PlayerDeathEvent(val name: String, val reason: String): DungeonEvent()

    class Score(val score: Int): DungeonEvent()
    object BossEnterEvent: DungeonEvent()
    object RunStatedEvent: DungeonEvent()
    object RunEndedEvent: DungeonEvent()
}