package com.github.noamm9


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.api.ModInitializer
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory


object NoammAddons : ModInitializer {
	const val MOD_NAME = "NoammAddons"
	const val PREFIX = "§6§l[§b§lN§d§lA§6§l]§r"
	const val MOD_ID = "noammaddons"

	val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	val mc = Minecraft.getInstance()
    val logger = LoggerFactory.getLogger(MOD_NAME)

	override fun onInitialize() {
	}
}