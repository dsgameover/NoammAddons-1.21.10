package com.github.noamm9.utils.dungeons

import com.github.noamm9.NoammAddons.mc
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.PlayerSkin

data class DungeonPlayer(
    var name: String,
    var clazz: Classes,
    var clazzLvl: Int,
    var skin: PlayerSkin,
    var isDead: Boolean = false,
) {
    val entity: Player? get() = mc.level?.players()?.find { it.name.string == name }
    //val mapIcon = DungeonMapPlayer(this, skin)
    //val clearInfo = ClearInfo()
}