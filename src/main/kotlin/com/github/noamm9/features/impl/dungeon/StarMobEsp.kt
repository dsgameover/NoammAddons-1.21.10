package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.CheckEntityGlowEvent
import com.github.noamm9.event.impl.EntityDeathEvent
import com.github.noamm9.event.impl.MainThreadPacketRecivedEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.ColorSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.utils.ChatUtils.formattedText
import com.github.noamm9.utils.ChatUtils.removeFormatting
import com.github.noamm9.utils.ChatUtils.unformattedText
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.location.LocationUtils.dungeonFloorNumber
import com.github.noamm9.utils.location.LocationUtils.inBoss
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ambient.Bat
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.player.Player
import java.awt.Color

object StarMobEsp: Feature() {
    private val dungeonMobRegex = Regex("^.+❤$")

    @JvmStatic
    val starMobs = HashSet<Int>()
    val checked = HashSet<Int>()

    private val espBats by ToggleSetting("Highlight Bats", true).withDescription("Highlights Bats in Dungeons.")
    private val espFels by ToggleSetting("Highlight Fels", true).withDescription("Highlights Fels, even when they are invisible.")
    private val customMinibossesColors by ToggleSetting("Custom Minibosses Colors", false).withDescription("Enable this to override individual mini-boss colors below.")

    private val starMobColor by ColorSetting("Star Mob Color", Color.YELLOW, false).section("General Colors").withDescription("Default color for all Starred mobs.")
    private val batColor by ColorSetting("Bat Color", Color.GREEN, false).withDescription("The color used for highlighted bats.").showIf { espBats.value }
    private val felColor by ColorSetting("Fel Color", Color.PINK, false).withDescription("The color used for fels.").showIf { espFels.value }

    private val shadowAssassinColor by ColorSetting("Shadow Assassin", Color.BLACK, false).section("Mini-Boss Colors").showIf { customMinibossesColors.value }
    private val angryArchaeologistColor by ColorSetting("Angry Archaeologist", Color.RED, false).showIf { customMinibossesColors.value }
    private val frozenAdventurerColor by ColorSetting("Frozen Adventurer", Color.CYAN, false).showIf { customMinibossesColors.value }
    private val superiorDragonColor by ColorSetting("Superior Dragon", Color.YELLOW, false).showIf { customMinibossesColors.value }
    private val unstableDragonColor by ColorSetting("Unstable Dragon", Color(100, 0, 100), false).showIf { customMinibossesColors.value }
    private val youngDragonColor by ColorSetting("Young Dragon", Color.WHITE, false).showIf { customMinibossesColors.value }
    private val holyDragonColor by ColorSetting("Holy Dragon", Color.GREEN, false).showIf { customMinibossesColors.value }


    override fun init() {
        register<MainThreadPacketRecivedEvent.Post> {
            if (! LocationUtils.inDungeon || inBoss) return@register
            if (event.packet !is ClientboundSetEntityDataPacket) return@register
            val entity = mc.level?.getEntity(event.packet.id) ?: return@register
            if (entity is ArmorStand) {
                val name = entity.customName?.formattedText ?: return@register
                if (name.matches(dungeonMobRegex) && name.contains("✯")) {
                    checkStarMob(entity, name)
                }
            }
            else if (entity is Player) {
                val name = mc.connection?.getPlayerInfo(entity.uuid)?.profile?.name ?: return@register
                if (name.equalsOneOf("Shadow Assassin", "Lost Adventurer", "Diamond Guy", "King Midas")) {
                    starMobs.add(entity.id)
                }
            }
        }

        register<EntityDeathEvent> {
            if (! LocationUtils.inDungeon || inBoss) return@register
            starMobs.removeIf { it == event.entity.id }
            checked.removeIf { it == event.entity.id }
        }

        register<WorldChangeEvent> {
            starMobs.clear()
            checked.clear()
        }

        register<CheckEntityGlowEvent> {
            getColor(event.entity)?.let {
                event.color = it
                return@register
            }

            if (event.entity.id in starMobs) {
                event.color = starMobColor.value
            }
        }
    }

    @JvmStatic
    fun getColor(entity: Entity): Color? {
        if (entity is Bat) return if (espBats.value && ! entity.isInvisible && ! entity.isPassenger) batColor.value else null
        if (entity is EnderMan) return if (espFels.value && entity.name.unformattedText == "Dinnerbone") felColor.value else null
        if (entity is Player) {
            val name = entity.name.unformattedText.takeUnless { it.isBlank() } ?: return null
            if (name.contains("Shadow Assassin")) return if (customMinibossesColors.value) {
                shadowAssassinColor.value
            }
            else starMobColor.value

            if (dungeonFloorNumber != 4 && ! inBoss) {
                val bootsName = entity.getItemBySlot(EquipmentSlot.FEET).takeUnless { it.isEmpty }?.hoverName?.unformattedText ?: return null

                return when (name) {
                    "Lost Adventurer" -> {
                        if (! customMinibossesColors.value) return starMobColor.value
                        when {
                            "Unstable" in bootsName -> unstableDragonColor
                            "Young" in bootsName -> youngDragonColor
                            "Superior" in bootsName -> superiorDragonColor
                            "Holy" in bootsName -> holyDragonColor
                            "Frozen Blaze" in bootsName -> frozenAdventurerColor
                            else -> starMobColor
                        }.value
                    }

                    "Diamond Guy" -> if (customMinibossesColors.value && "Perfect Boots" in bootsName) {
                        angryArchaeologistColor.value
                    }
                    else starMobColor.value

                    else -> null
                }
            }
        }

        return null
    }

    private fun checkStarMob(armorStand: Entity, name: String) {
        if (! checked.add(armorStand.id)) return
        val name = name.removeFormatting().uppercase()
        // withermancers are always -3 to real entity the -1 and -2 are the wither skulls that they shoot
        val id = if (name.contains("WITHERMANCER")) 3 else 1
        val realEntityId = armorStand.id - id

        val mob = armorStand.level().getEntity(realEntityId)
        if (mob !is ArmorStand && realEntityId !in starMobs && mob != null) {
            starMobs.add(realEntityId)
            return
        }

        val possibleEntities = armorStand.level().getEntities(
            armorStand, armorStand.boundingBox.move(0.0, - 1.0, 0.0)
        ) { it !is ArmorStand }

        possibleEntities.find {
            ! starMobs.contains(it.id) && when (it) {
                is Player -> ! it.isInvisible && it.uuid.version() == 2 && it != mc.player
                is WitherBoss -> false
                else -> true
            }
        }?.let {
            if (getColor(it) == null) starMobs.add(it.id)
        }
    }
}