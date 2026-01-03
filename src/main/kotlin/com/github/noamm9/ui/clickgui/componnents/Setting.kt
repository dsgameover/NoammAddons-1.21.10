package com.github.noamm9.ui.clickgui.componnents

import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.impl.CategorySetting
import com.github.noamm9.ui.clickgui.componnents.impl.SeparatorSetting
import net.minecraft.client.gui.GuiGraphics
import kotlin.reflect.KProperty

abstract class Setting<T>(val name: String, open var value: T) {
    var x = 0
    var y = 0

    var width = 0
    open val height: Int get() = 20

    var description: String? = null

    var visibility: () -> Boolean = { true }
    val isVisible: Boolean get() = visibility.invoke()

    var headerName: String? = null

    abstract fun draw(ctx: GuiGraphics, mouseX: Int, mouseY: Int)
    abstract fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean

    open fun mouseReleased(button: Int) {}
    open fun keyPressed(keyCode: Int): Boolean = false
    open fun charTyped(codePoint: Char): Boolean = false
}

/**
 * Attaches a Category and Separator to this setting.
 * They will be injected into the GUI right above this setting.
 */
fun <T: Setting<*>> T.section(name: String): T {
    this.headerName = name
    return this
}

fun <T: Setting<*>> T.withDescription(desc: String): T {
    this.description = desc.let {
        return@let if (! it.endsWith('.')) "$it."
        else it
    }
    return this
}

fun <T: Setting<*>> T.showIf(condition: () -> Boolean): T {
    this.visibility = condition
    return this
}

fun <T: Setting<*>> T.hideIf(condition: () -> Boolean): T {
    this.visibility = { ! condition() }
    return this
}


operator fun <T, S: Setting<T>> S.provideDelegate(thisRef: Feature, prop: KProperty<*>): S {
    this.headerName?.let { name ->
        thisRef.configSettings.add(SeparatorSetting())
        thisRef.configSettings.add(CategorySetting(name))
    }

    thisRef.configSettings.add(this)
    return this
}

operator fun <T, S: Setting<T>> S.getValue(thisRef: Feature, prop: KProperty<*>): S {
    return this
}