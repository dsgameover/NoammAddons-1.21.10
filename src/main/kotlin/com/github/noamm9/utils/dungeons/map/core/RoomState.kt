package com.github.noamm9.utils.dungeons.map.core

/**
 * [ordinal] matters here, should be in the order of what can happen to a room.
 */
enum class RoomState {
    FAILED, GREEN, CLEARED, DISCOVERED, UNOPENED, UNDISCOVERED
}