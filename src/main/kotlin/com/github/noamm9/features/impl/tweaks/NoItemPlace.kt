package com.github.noamm9.features.impl.tweaks

import com.github.noamm9.features.Feature
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.Utils.startsWithOneOf
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import net.minecraft.world.item.context.BlockPlaceContext

object NoItemPlace: Feature("Stops you from placing skull blocks/Items") {
    @JvmStatic
    fun placeHook(context: BlockPlaceContext): Boolean {
        val sbId = context.player?.mainHandItem?.skyblockId ?: return false
        return enabled && (sbId.equalsOneOf(
            "BOUQUET_OF_LIES", "FLOWER_OF_TRUTH",
            "BAT_WAND", "STARRED_BAT_WAND",
            "INFINITE_SPIRIT_LEAP", "ROYAL_PIGION",
            "ARROW_SWAPPER"
        ) || sbId.startsWithOneOf("ABIPHONE"))
    }
}