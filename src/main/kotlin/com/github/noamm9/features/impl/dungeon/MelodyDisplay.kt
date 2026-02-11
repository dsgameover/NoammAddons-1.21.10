package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ButtonSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SoundSetting
import com.github.noamm9.ui.clickgui.componnents.impl.TextInputSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.showIf
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.ui.hud.getValue
import com.github.noamm9.ui.hud.provideDelegate
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object MelodyDisplay: Feature("Displays the current progress someone for melody on screen") {
  private val leapMsg by TextInputSetting("Leap Message", "&d{name} has {progress} melody")
    .withDescription("replaces {name} with the player name and {progress} to the melody progress")

  private val alertDuration by SliderSetting("Alert Duration", 2.5f, 0f, 5f, 0.1f)

  private val shouldPlaySound by ToggleSetting("Play sound", true)
  private val sound by SoundSetting("Sound", SoundEvents.EXPERIENCE_ORB_PICKUP).showIf { shouldPlaySound.value }

  private val volume by SliderSetting("Volume", 0.5f, 0f, 1f, 0.1f)
    .withDescription("The loudness of the sound.")
    .showIf { shouldPlaySound.value }

  private val pitch by SliderSetting("Pitch", 2f, 0f, 2f, 0.1f)
    .withDescription("The pitch/frequency of the sound.")
    .showIf { shouldPlaySound.value }

  private val playSound by ButtonSetting("Test Sound", false) {
    repeat(5) { mc.soundManager.play(SimpleSoundInstance.forUI(sound.value, pitch.value, volume.value)) }
  }.withDescription("Click to test the current sound configuration.").showIf { shouldPlaySound.value }

  var currentMelody: Pair<String?, Int?> = null to null
  var lastMelodyMessage = System.currentTimeMillis()

  private val melodyDisplay by hudElement("Melody Display", centered = true) { ctx, example ->
    val line = if (example) getMelodyMessage("at0w0", 1)
    else {
      if (!LocationUtils.inDungeon || !LocationUtils.inBoss || LocationUtils.F7Phase != 3) return@hudElement 0f to 0f
      val name = currentMelody.first ?: return@hudElement 0f to 0f
      val progress = currentMelody.second ?: return@hudElement 0f to 0f
      getMelodyMessage(name, progress)
    }

    Render2D.drawCenteredString(ctx, line, 0, 0)
    return@hudElement line.width().toFloat() to 9f
  }

  fun getMelodyMessage(name: String, progress: Int): String {
    return leapMsg.value.replace("{name}", name).replace("{progress}", "${progress}/4")
  }

  override fun init() {
    register<ChatMessageEvent> {
      if (!LocationUtils.inDungeon || !LocationUtils.inBoss || LocationUtils.F7Phase != 3) return@register
      val message = event.unformattedText
      if (!message.startsWith("Party > ")) return@register

      val name = Regex("""Party > (?:\[[^]]+]\s)?(\w+):""").find(message)?.groupValues?.get(1) ?: return@register

      for (i in 0..4) {
        if (message.contains("${i}/4") || message.contains("${i*25}%")) {
          currentMelody = name to i
          lastMelodyMessage = System.currentTimeMillis()
          if (shouldPlaySound.value) {
            playSound.action.invoke()
          }
        }
      }
    }

    register<TickEvent.End> {
      if ((System.currentTimeMillis() - lastMelodyMessage) / 1000f > alertDuration.value) {
        currentMelody = null to null
      }
    }
  }
}
