package com.github.noamm9.features

import com.github.noamm9.NoammAddons
import com.github.noamm9.config.Config
import com.github.noamm9.event.Event
import com.github.noamm9.event.EventBus
import com.github.noamm9.event.EventBus.EventContext
import com.github.noamm9.event.EventListener
import com.github.noamm9.features.annotations.AlwaysActive
import com.github.noamm9.features.annotations.Dev
import com.github.noamm9.ui.clickgui.CategoryType
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.Utils.spaceCaps
import kotlin.jvm.java

open class Feature(
    val desc: String = "",
    name: String? = null,
    toggled: Boolean = false,
) {
    val name = name ?: this::class.simpleName.toString().spaceCaps()
    val listeners = mutableSetOf<EventListener>()

    @JvmField
    var enabled = toggled

    private val isDev = this::class.java.isAnnotationPresent(Dev::class.java)
    val alwaysActive = this::class.java.isAnnotationPresent(AlwaysActive::class.java)

   open val category = if (isDev) CategoryType.DEV else _getCategory()

    protected inline val mc get() = NoammAddons.mc
    protected inline val scope get() = NoammAddons.scope

    fun _init() {
        if (enabled || alwaysActive) onEnable() else onDisable()

        init()

        Config.config.getOrPut(category) {
            mutableSetOf()
        }.add(Config.FeatureElement(this/*, configSettings*/))
    }

    open fun init() {}


    open fun onEnable() {
        listeners.forEach(EventListener::register)
    }

    open fun onDisable() {
        listeners.forEach(EventListener::unregister)
    }

    open fun toggle() {
        enabled = !enabled
        if (enabled) onEnable()
        else onDisable()

        ChatUtils.modMessage("$name: ${if (enabled) "&aEnabled" else "&cDisabled"}")
    }

    protected inline fun <reified T : Event> register(noinline block: EventContext<T>.() -> Unit): EventListener {
        val listener = EventBus.register<T>(block).unregister()
        listeners.add(listener)
        return listener
    }


    private fun _getCategory(): CategoryType {
        val parts = this::class.java.`package` !!.name.split(".")
        val categoryName = parts[parts.indexOf("impl") + 1].uppercase()
        if (CategoryType.entries.none { it.name == categoryName }) return CategoryType.MISC//throw Error("Category does not exist: $categoryName")
        return CategoryType.valueOf(categoryName)
    }
}