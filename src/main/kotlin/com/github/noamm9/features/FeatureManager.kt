package com.github.noamm9.features

import com.github.noamm9.config.Config
import com.github.noamm9.features.impl.render.StarMobEsp
import com.github.noamm9.ui.clickgui.CategoryType

object FeatureManager {
    val features = mutableSetOf(
        StarMobEsp,
    ).sortedBy { it.name }

    fun registerFeatures() {
        features.forEach(Feature::_init)
        Config.load()
    }

    fun getFeaturesByCategory(category: CategoryType): List<Feature> {
        return features.filter { it.category == category }
    }

    fun getFeatureByName(name: String): Feature? {
        return features.find { it.name == name }
    }
}