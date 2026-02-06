package com.github.noamm9.utils.dungeons.enums

import java.awt.Color

enum class DungeonClass(
    val color: Color,
    val code: String,
    val quadIndex: Int,
    val priority: Int
) {
    Archer(Color(125, 0, 0), "§4", 0, 2),
    Berserk(Color(205, 106, 0), "§6", 1, 0),
    Healer(Color(123, 0, 123), "§5", 2, 2),
    Mage(Color(0, 185, 185), "§3", 3, 2),
    Tank(Color(0, 125, 0), "§2", 4, 1),
    Empty(Color(0, 0, 0), "§7", 0, 0);

    companion object {
        fun fromName(name: String): DungeonClass {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: Empty
        }
    }
}