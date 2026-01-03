package com.github.noamm9.config

import com.github.noamm9.NoammAddons.MOD_NAME
import com.github.noamm9.NoammAddons.logger
import com.github.noamm9.features.FeatureManager
import com.google.gson.*
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object Config {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val parser = JsonParser()

    private val configDir = FabricLoader.getInstance().configDir.resolve(MOD_NAME).toFile()
    private val configFile = File(configDir, "config.json").apply {
        if (! configDir.exists()) configDir.mkdirs()
        runCatching(::createNewFile).apply {
            onFailure { logger.error("Error initializing config", it) }
            onSuccess { logger.info("Successfully initialized config file path") }
        }
    }

    fun load() {
        runCatching {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (isEmpty()) return

                val jsonArray = parser.parse(this).asJsonArray ?: return
                for (features in jsonArray) {
                    val featureObj = features?.asJsonObject ?: continue
                    val feature = FeatureManager.getFeatureByName(featureObj.get("name").asString) ?: continue
                    if (featureObj.get("enabled").asBoolean != feature.enabled) {
                        feature.toggle()
                    }

                    for (j in featureObj.get("configSettings").asJsonArray) {
                        val settingObj = j?.asJsonObject?.entrySet() ?: continue
                        val setting = feature.getSettingByName(settingObj.firstOrNull()?.key) ?: continue
                        if (setting is Savable) setting.read(settingObj.first().value)
                    }
                }
            }
        }.apply {
            onFailure { logger.error("Error loading config", it) }
            onSuccess { logger.info("Successfully loaded config") }
        }
    }

    fun save() {
        runCatching {
            val jsonArray = JsonArray().apply {
                for (feature in FeatureManager.features) {
                    add(JsonObject().apply {
                        add("name", JsonPrimitive(feature.name))
                        add("enabled", JsonPrimitive(feature.enabled))
                        add("configSettings", JsonArray().apply {
                            for (setting in feature.configSettings) {
                                if (setting is Savable) {
                                    add(JsonObject().apply { add(setting.name, setting.write()) })
                                }
                            }
                        })
                    })
                }
            }
            configFile.bufferedWriter().use { it.write(gson.toJson(jsonArray)) }
        }.apply {
            onFailure { logger.error("Error on saving config", it) }
            onSuccess { logger.info("Successfully saved config") }
        }
    }
}

