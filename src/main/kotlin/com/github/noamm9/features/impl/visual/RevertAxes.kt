package com.github.noamm9.features.impl.visual

import com.github.noamm9.features.Feature
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object RevertAxes: Feature("Turns certain swords back into an axe") {
    private val replaceableItems: Map<String, Item> = hashMapOf(
        makePair("RAGNAROCK_AXE", Items.GOLDEN_AXE),
        makePair("DAEDALUS_AXE", Items.GOLDEN_AXE),
        makePair("AXE_OF_THE_SHREDDED", Items.DIAMOND_AXE)
    )

    fun shouldReplace(itemStack: ItemStack): ItemStack? {
        if (!enabled) return null
        val skyblockID = itemStack.skyblockId

        if (skyblockID == "")
            return null

        val replace = replaceableItems[skyblockID] ?: return null
        return itemStack.transmuteCopy(replace, itemStack.count)
    }

    private fun makePair(string: String, item: Item): Pair<String, Item> = Pair(string, item)
}