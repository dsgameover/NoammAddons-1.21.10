package com.github.noamm9.utils

import java.awt.Color

object ColorUtils {
    fun Color.withAlpha(i: Int) = Color(this.red, this.green, this.blue, i)

    fun Color.lerp(color: Color, value: Float): Color {
        return MathUtils.lerpColor(this, color, value)
    }
}