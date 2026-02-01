package com.github.noamm9.commands.impl

import com.github.noamm9.NoammAddons.debugFlags
import com.github.noamm9.NoammAddons.screen
import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder
import com.github.noamm9.ui.clickgui.ClickGuiScreen
import com.github.noamm9.ui.hud.HudEditorScreen
import com.github.noamm9.utils.ChatUtils
import com.mojang.brigadier.arguments.StringArgumentType

object NaCommand: BaseCommand("na") {
    override fun CommandNodeBuilder.build() {
        runs {
            screen = ClickGuiScreen
        }

        literal("help") {
            runs {
                ChatUtils.chat("§6§lNoammAddons§r\n§e/na §7- GUI\n§e/na hud §7- HUD")
            }
        }

        literal("hud") {
            runs { screen = HudEditorScreen }
        }

        literal("debug") {
            runs {
                ChatUtils.modMessage("§7Flags: §f${debugFlags.joinToString(", ")}")
            }

            argument("flag", StringArgumentType.word()) {
                runs { ctx ->
                    val flag = StringArgumentType.getString(ctx, "flag")
                    if (debugFlags.remove(flag)) ChatUtils.modMessage("§cRemoved: §b$flag")
                    else {
                        debugFlags.add(flag)
                        ChatUtils.modMessage("§aAdded: §b$flag")
                    }
                }
            }
        }
    }
}

