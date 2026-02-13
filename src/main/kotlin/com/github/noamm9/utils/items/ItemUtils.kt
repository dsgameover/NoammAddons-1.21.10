package com.github.noamm9.utils.items

import com.github.noamm9.NoammAddons
import com.github.noamm9.utils.ChatUtils.formattedText
import com.github.noamm9.utils.ChatUtils.removeFormatting
import com.github.noamm9.utils.items.ItemRarity.Companion.PET_PATTERN
import com.github.noamm9.utils.items.ItemRarity.Companion.RARITY_PATTERN
import com.github.noamm9.utils.items.ItemRarity.Companion.rarityCache
import com.github.noamm9.utils.network.WebUtils
import kotlinx.coroutines.launch
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore

object ItemUtils {
    val idToNameLookup = mutableMapOf<String, String>()

    init {
        @Suppress("UNCHECKED_CAST")
        NoammAddons.scope.launch {
            WebUtils.get<Map<String, Any>>("https://api.noammaddons.workers.dev/items").onSuccess {
                idToNameLookup.putAll(it["itemIdToName"] as Map<String, String>)
            }
        }
    }

    val ItemStack.customData: CompoundTag get() = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()

    val ItemStack.skyblockId: String get() = customData.getString("id").orElse("")
    val ItemStack.itemUUID: String get() = customData.getString("uuid").orElse("")
    val ItemStack.lore: List<String> get() = getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines().map { it.formattedText }


    fun getSkullTexture(stack: ItemStack): String? {
        if (stack.isEmpty) return null
        val profile = stack.get(DataComponents.PROFILE) ?: return null
        val properties = profile.partialProfile().properties
        return properties["textures"].firstOrNull()?.value
    }

    fun ItemStack.hasGlint() = componentsPatch.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)?.isPresent == true

    fun getRarity(item: ItemStack?): ItemRarity {
        item ?: return ItemRarity.NONE
        if (item.isEmpty) return ItemRarity.NONE
        rarityCache.getIfPresent(item)?.let { return it }

        val rarity = run {
            val lore = item.lore.takeUnless { it.isEmpty() } ?: return@run ItemRarity.NONE

            for (i in lore.indices) {
                val idx = lore.lastIndex - i
                val line = lore[idx]

                val rarityName = RARITY_PATTERN.find(line)?.groups?.get("rarity")?.value?.removeFormatting()?.substringAfter("SHINY ")

                ItemRarity.entries.find { it.rarityName == rarityName }?.let { return@run it }
            }

            PET_PATTERN.find(item.hoverName.formattedText)?.groupValues?.getOrNull(1)?.let(ItemRarity::byBaseColor) ?: ItemRarity.NONE
        }

        rarityCache.put(item, rarity)
        return rarity
    }
}