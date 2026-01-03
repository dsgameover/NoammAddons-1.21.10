package com.github.noamm9.utils

import com.github.noamm9.utils.ChatUtils.formattedText
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore
import kotlin.collections.map


object ItemUtils {
    val ItemStack.customData: CompoundTag get() = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()

    val ItemStack.skyblockId: String get() = customData.getString("id").orElse("")
    val CompoundTag.skyblockId: String get() = getString("id").orElse("")
    val ItemStack.itemUUID: String get() = customData.getString("uuid").orElse("")
    val ItemStack.lore: List<String> get() = getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines().map { it.formattedText}

}