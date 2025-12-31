package com.github.noamm9.features

import com.github.noamm9.NoammAddons
import com.github.noamm9.event.EventBus
import com.github.noamm9.features.annotations.AlwaysActive
import com.github.noamm9.features.annotations.Dev
import com.github.noamm9.utils.Utils.spaceCaps
import net.minecraft.world.flag.FeatureElement
import kotlin.collections.find
import kotlin.jvm.java

open class Feature(
    val desc: String = "",
    name: String? = null,
    toggled: Boolean = false,
) {
    val name = name ?: this::class.simpleName.toString().spaceCaps()

    @JvmField
    var enabled = toggled


    private val isDev = this::class.java.isAnnotationPresent(Dev::class.java)
    val alwaysActive = this::class.java.isAnnotationPresent(AlwaysActive::class.java)

   // open val category = if (isDev) CategoryType.DEV else _getCategory()

    protected inline val mc get() = NoammAddons.mc
    protected inline val scope get() = NoammAddons.scope

    fun _init() {
     //   if (alwaysActive || enabled) onEnable()

        init()
    }

    open fun init() {}

    /*
    private fun _getCategory(): CategoryType {
        val parts = this::class.java.`package` !!.name.split(".")
        val categoryName = parts[parts.indexOf("impl") + 1].uppercase()
        if (CategoryType.entries.none { it.name == categoryName }) throw Error("Category does not exist: $categoryName")
        return CategoryType.valueOf(categoryName)
    }*/
}