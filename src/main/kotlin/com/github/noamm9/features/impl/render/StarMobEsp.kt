package com.github.noamm9.features.impl.render

import com.github.noamm9.event.impl.EntityDeathEvent
import com.github.noamm9.event.impl.MainThreadPacketRecivedEvent
import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.ChatUtils.removeFormatting
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.LocationUtils
import com.github.noamm9.utils.ThreadUtils
import com.github.noamm9.utils.render.Render3D
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import java.awt.Color

object StarMobEsp : Feature() {
    val dungeonMobRegex = Regex("^*(.+).+❤$")
    @JvmStatic
    val starMobs = HashSet<Entity>()
    val checked = HashSet<Entity>()

    override fun init() {
        register<MainThreadPacketRecivedEvent.Post> {
            if (!LocationUtils.inDungeon || LocationUtils.inBoss) return@register
            if (event.packet !is ClientboundSetEntityDataPacket) return@register
            val entity = mc.level?.getEntity(event.packet.id)
            val name = entity?.customName?.string ?: return@register
            if (! name.matches(dungeonMobRegex) || ! name.contains("✯")) return@register
            checkStarMob(entity, name)
        }

        register<EntityDeathEvent> {
            if (!LocationUtils.inDungeon) return@register
            if (starMobs.isEmpty()) return@register
            starMobs.removeIf { it.id == event.entity.id }
            checked.removeIf { it.id == event.entity.id }
        }

        register<RenderWorldEvent> {
            if (!LocationUtils.inDungeon) return@register
            return@register
            starMobs.forEach { entity ->
                Render3D.renderBox(
                    event.ctx, entity,
                    Color.CYAN.withAlpha(80),
                    outline = true, fill = true, phase = true
                )
            }
        }
    }


    private fun checkStarMob(armorStand: Entity, name: String) {
        if (!checked.add(armorStand)) return
        val name = name.removeFormatting().uppercase()
        // withermancers are always -3 to real entity the -1 and -2 are the wither skulls that they shoot
        val id = if (name.contains("WITHERMANCER")) 3 else 1

        val mob = armorStand.level().getEntity(armorStand.id - id)
        if (mob !is ArmorStand && mob !in starMobs && mob != null) {
            starMobs.add(mob)
            return
        }

        val possibleEntities = armorStand.level().getEntities(
            armorStand, armorStand.boundingBox.move(0.0, - 1.0, 0.0)
        ) { it !is ArmorStand }

        possibleEntities.find {
            ! starMobs.contains(it) && when (it) {
                is Player -> ! it.isInvisible && it.uuid.version() == 2 && it != mc.player
                is WitherBoss -> false
                else -> true
            }
        }?.let {
            starMobs.add(it)
        }//?.let(starMobs::add)
    }
}