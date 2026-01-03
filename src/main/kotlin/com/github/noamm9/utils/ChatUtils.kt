package com.github.noamm9.utils

import com.github.noamm9.NoammAddons
import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.Utils.remove
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional
import kotlin.math.roundToInt

object ChatUtils {
    val formattingRegex = "(?i)[§&]([0-9a-fk-or]|x[0-9a-f]{6})".toRegex()
    fun String.removeFormatting() = remove(formattingRegex)

    fun sendMessage(message: String) = mc.player?.connection?.sendChat(message)
    fun sendCommand(command: String) = mc.player?.connection?.sendCommand(command.removePrefix("/"))
    fun modMessage(msg: Any?) = chat("${NoammAddons.PREFIX} $msg")

    fun chat(msg: Any?) = ThreadUtils.runOnMcThread { mc.gui?.chat?.addMessage(Component.literal(msg.toString().addColor())) }
    fun chat(comp: Component) = ThreadUtils.runOnMcThread { mc.gui?.chat?.addMessage(comp) }

    fun String.addColor() = replace("&".toRegex(), "§")

    val Component.unformattedText: String get() = this.string.removeFormatting()
    val Component.formattedText: String get() {
        val sb = StringBuilder()

        visit({ style, string ->
            style.color?.let { textColor ->
                val colorMatch = ChatFormatting.entries.firstOrNull {
                    it.isColor && it.color == textColor.value
                }

                if (colorMatch != null) {
                    sb.append("§${colorMatch.char}")
                }
            }

            if (style.isBold) sb.append("§${ChatFormatting.BOLD.char}")
            if (style.isItalic) sb.append("§${ChatFormatting.ITALIC.char}")
            if (style.isUnderlined) sb.append("§${ChatFormatting.UNDERLINE.char}")
            if (style.isStrikethrough) sb.append("§${ChatFormatting.STRIKETHROUGH.char}")
            if (style.isObfuscated) sb.append("§${ChatFormatting.OBFUSCATED.char}")

            sb.append(string)

            Optional.empty<String>()
        }, Style.EMPTY)

        return sb.toString()
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