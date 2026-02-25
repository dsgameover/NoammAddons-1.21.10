package com.github.noamm9.features.impl.misc

import com.github.noamm9.features.Feature
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object ArrowFix: Feature("Disables Bow Pullback on Shortbows.") {
    private val bowCache = mutableSetOf<String>()

    @JvmStatic
    fun isShortbow(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        if (! item.`is`(Items.BOW)) return false
        if (item.skyblockId in bowCache) return true
        if (! item.has(DataComponents.LORE)) return false
        val lore = item.get(DataComponents.LORE) ?: return false

        for (lineComponent in lore.lines()) {
            val lineText = lineComponent.string
            if (lineText.contains("Shortbow: Instantly shoots!")) {
                bowCache.add(item.skyblockId)
                return true
            }
        }

        return false
    }
}