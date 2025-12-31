package com.github.noamm9


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.api.ModInitializer
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

object NoammAddons : ModInitializer {
	val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	val mc = Minecraft.getInstance()
    val logger = LoggerFactory.getLogger("noammaddons")

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
	}
}