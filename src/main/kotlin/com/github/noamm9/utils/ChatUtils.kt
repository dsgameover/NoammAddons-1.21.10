package com.github.noamm9.utils

import com.github.noamm9.NoammAddons
import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.Utils.remove
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

object ChatUtils {
    val formattingRegex = "(?<!\\\\\\\\)&(?=[0-9a-fk-or])".toRegex()
    fun String.removeFormatting() = remove(formattingRegex)

    fun sendMessage(message: String) = mc.player?.connection?.sendChat(message)
    fun sendCommand(command: String) = mc.player?.connection?.sendCommand(command.removePrefix("/"))
    fun modMessage(msg: Any?) = chat("${NoammAddons.PREFIX} $msg")

    fun chat(msg: Any?) = mc.gui?.chat?.addMessage(Component.literal(msg.toString().addColor()))
    fun chat(comp: Component) = mc.gui?.chat?.addMessage(comp)

    fun String.addColor(): String {
        return this.replace("&".toRegex(), "§")
    }

    fun getChatBreak(): String {
        val chatWidth = mc.gui?.chat?.width ?: return ""
        val textRenderer = mc.font
        val dashWidth = textRenderer.width("-")

        val repeatCount = chatWidth / dashWidth
        return "-".repeat(repeatCount)
    }

    fun getCenteredText(text: String): String {
        val chatWidth = mc.gui?.chat?.width ?: return text
        val textRenderer = mc.font
        val textWidth = textRenderer.width(text)
        if (textWidth >= chatWidth) return text
        val spaceWidth = textRenderer.width(" ")

        val padding = ((chatWidth - textWidth) / 2f / spaceWidth).roundToInt()
        return " ".repeat(padding) + text
    }
}