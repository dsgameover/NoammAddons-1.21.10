package com.github.noamm9.features

import com.github.noamm9.NoammAddons
import com.github.noamm9.config.Savable
import com.github.noamm9.event.Event
import com.github.noamm9.event.EventBus.EventContext
import com.github.noamm9.event.EventListener
import com.github.noamm9.event.EventPriority
import com.github.noamm9.features.annotations.AlwaysActive
import com.github.noamm9.features.annotations.Dev
import com.github.noamm9.ui.clickgui.CategoryType
import com.github.noamm9.ui.clickgui.componnents.Setting
import com.github.noamm9.ui.clickgui.componnents.impl.ButtonSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SoundSetting
import com.github.noamm9.ui.clickgui.componnents.showIf
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.ui.hud.HudElement
import com.github.noamm9.utils.Utils.spaceCaps
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents

open class Feature(
    val description: String? = null,
    name: String? = null,
    toggled: Boolean = false,
) {
    val name = name ?: this::class.simpleName.toString().spaceCaps()
    val listeners = mutableSetOf<EventListener<*>>()

    val configSettings = mutableSetOf<Setting<*>>()
    val hudElements = mutableSetOf<HudElement>()

    @JvmField
    var enabled = toggled

    private val isDev = this::class.java.isAnnotationPresent(Dev::class.java)
    val alwaysActive = this::class.java.isAnnotationPresent(AlwaysActive::class.java)

    open val category = if (isDev) CategoryType.DEV else initCategory()

    protected inline val mc get() = NoammAddons.mc
    protected inline val scope get() = NoammAddons.scope
    protected inline val cacheData get() = NoammAddons.cacheData

    fun initialize() {
        if (enabled || alwaysActive) onEnable() else onDisable()

        init()
    }

    open fun init() {}


    open fun onEnable() {
        listeners.forEach(EventListener<*>::register)
    }

    open fun onDisable() {
        listeners.forEach(EventListener<*>::unregister)
    }

    open fun toggle() {
        enabled = ! enabled
        if (enabled) onEnable()
        else onDisable()
    }

    protected inline fun <reified T: Event> register(
        priority: EventPriority = EventPriority.NORMAL,
        noinline block: EventContext<T>.() -> Unit
    ): EventListener<T> {
        val listener = EventListener(T::class.java, priority, block)
        listeners.add(listener)
        return listener
    }

    fun hudElement(
        name: String,
        enabled: () -> Boolean = { true },
        shouldDraw: () -> Boolean = { true },
        render: (GuiGraphics, Boolean) -> Pair<Float, Float>
    ): HudElement {
        return object: HudElement() {
            override val name = name
            override val toggle: Boolean get() = this@Feature.enabled && shouldDraw.invoke()
            override val shouldDraw: Boolean get() = enabled.invoke()
            override fun draw(ctx: GuiGraphics, example: Boolean): Pair<Float, Float> = render(ctx, example)
        }.also(hudElements::add)
    }

    data class SoundSettings(val sound: SoundSetting, val volume: SliderSetting<Float>, val pitch: SliderSetting<Float>, val play: ButtonSetting)

    fun createSoundSettings(name: String, sound: SoundEvent, showIf: () -> Boolean = { true }): SoundSettings {
        val sound = SoundSetting("Sound", SoundEvents.EXPERIENCE_ORB_PICKUP)
            .withDescription("The internal Minecraft sound key to play.")
            .showIf(showIf)

        val volume = SliderSetting("Volume", 0.5f, 0f, 1f, 0.1f)
            .withDescription("The loudness of the sound.")
            .showIf(showIf)

        val pitch = SliderSetting("Pitch", 1f, 0f, 2f, 0.1f)
            .withDescription("The pitch/frequency of the sound.")
            .showIf(showIf)

        val play = ButtonSetting("Play Sound", false) {
            repeat(5) { mc.soundManager.play(SimpleSoundInstance.forUI(sound.value, pitch.value, volume.value)) }
        }.withDescription("Click to test the current sound configuration.").showIf(showIf)

        configSettings.add(sound)
        configSettings.add(volume)
        configSettings.add(pitch)
        configSettings.add(play)

        return SoundSettings(sound, volume, pitch, play)
    }


    fun getSettingByName(key: String?): Setting<*>? {
        return configSettings.find { it.name == key && it is Savable }
    }

    private fun initCategory(): CategoryType {
        val parts = this::class.java.`package` !!.name.split(".")
        val categoryName = parts[parts.indexOf("impl") + 1].uppercase()
        if (CategoryType.entries.none { it.name.equals(categoryName, true) }) throw Error("Category does not exist: $categoryName")
        return CategoryType.valueOf(categoryName.uppercase())
    }
}