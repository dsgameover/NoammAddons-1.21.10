package com.github.noamm9.features.impl.dungeon.waypoints

import com.github.noamm9.NoammAddons.MOD_NAME
import com.github.noamm9.event.impl.DungeonEvent
import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.JsonUtils
import com.github.noamm9.utils.dungeons.map.utils.ScanUtils
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render3D
import com.google.gson.reflect.TypeToken
import net.minecraft.core.BlockPos
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.CopyOnWriteArrayList

object DungeonWaypoints: Feature("Add a custom waypoint with /dw add while looking at a block") {
    data class DungeonWaypoint(val pos: BlockPos, val color: Color, val filled: Boolean, val outline: Boolean, val phase: Boolean)

    private val configFile = File("config/$MOD_NAME/dungeonWaypoints.json")
    val waypoints = mutableMapOf<String, List<DungeonWaypoint>>()
    val currentRoomWaypoints: CopyOnWriteArrayList<DungeonWaypoint> = CopyOnWriteArrayList()

    val secretWaypoints by ToggleSetting("Secret Waypoints")

    override fun init() {
        register<DungeonEvent.RoomEvent.onEnter> {
            SecretsWaypoints.onRoomEnter(event.room)
            currentRoomWaypoints.clear()

            val roomName = event.room.name
            val roomRotation = event.room.rotation ?: return@register
            val roomCorner = event.room.corner ?: return@register

            waypoints[roomName]?.map {
                DungeonWaypoint(
                    ScanUtils.getRealCoord(it.pos, roomCorner, 360 - roomRotation),
                    it.color, it.filled, it.outline, it.phase
                )
            }?.let { currentRoomWaypoints.addAll(it) }
        }

        register<DungeonEvent.BossEnterEvent> {
            SecretsWaypoints.clear()
            currentRoomWaypoints.clear()
            waypoints["B" + LocationUtils.dungeonFloorNumber]?.let { currentRoomWaypoints.addAll(it) }
        }

        register<RenderWorldEvent> {
            SecretsWaypoints.onRenderWorld(event.ctx)
            if (currentRoomWaypoints.isEmpty()) return@register

            for (waypoint in currentRoomWaypoints) Render3D.renderBlock(
                event.ctx, waypoint.pos, waypoint.color,
                outline = waypoint.outline, fill = waypoint.filled,
                phase = waypoint.phase
            )
        }

        register<WorldChangeEvent> {
            SecretsWaypoints.clear()
            currentRoomWaypoints.clear()
        }

        configFile.takeIf(File::exists)?.let(::FileReader).use {
            val type = object: TypeToken<MutableMap<String, List<DungeonWaypoint>>>() {}.type
            val loadedData = runCatching { JsonUtils.gsonBuilder.fromJson<MutableMap<String, List<DungeonWaypoint>>?>(it, type) }.getOrNull()

            if (loadedData != null) {
                waypoints.clear()
                waypoints.putAll(loadedData)
            }
        }
    }

    fun saveConfig() {
        configFile.parentFile.takeUnless(File::exists)?.let(File::mkdirs)
        val writer = FileWriter(configFile)
        JsonUtils.gsonBuilder.toJson(waypoints, writer)
        writer.close()
    }

    fun saveWaypoint(absPos: BlockPos, relPos: BlockPos, roomName: String, color: Color, filled: Boolean, outline: Boolean, phase: Boolean) {
        val newWaypoint = DungeonWaypoint(relPos, color, filled, outline, phase)

        val currentData = waypoints.toMutableMap()
        val roomList = currentData.getOrDefault(roomName, emptyList()).toMutableList()
        val wasReplaced = roomList.removeIf { it.pos == relPos }

        roomList.add(newWaypoint)
        currentData[roomName] = roomList

        waypoints.clear()
        waypoints.putAll(currentData)
        saveConfig()

        currentRoomWaypoints.removeIf { it.pos == absPos }

        val absoluteWaypoint = DungeonWaypoint(absPos, color, filled, outline, phase)
        currentRoomWaypoints.add(absoluteWaypoint)

        if (wasReplaced) ChatUtils.modMessage("§e$roomName: Waypont updated at ${absPos.x}, ${absPos.y}, ${absPos.z}.")
        else ChatUtils.modMessage("§a$roomName: Waypoint added at ${absPos.x}, ${absPos.y}, ${absPos.z}.")
    }
}