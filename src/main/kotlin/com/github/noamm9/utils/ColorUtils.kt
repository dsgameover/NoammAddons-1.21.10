package com.github.noamm9.utils

import java.awt.Color

object ColorUtils {
    fun Color.withAlpha(i: Int)= Color(this.red, this.green, this.blue, i)
}