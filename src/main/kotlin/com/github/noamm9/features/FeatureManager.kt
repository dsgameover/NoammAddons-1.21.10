package com.github.noamm9.features

import com.github.noamm9.features.impl.render.PlayerEsp

object FeatureManager {
    val features = mutableSetOf(
        PlayerEsp,
    ).sortedBy { it.name }

    fun registerFeatures() {
        features.forEach(Feature::_init)
        //Config.load()
    }
}