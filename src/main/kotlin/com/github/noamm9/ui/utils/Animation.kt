package com.github.noamm9.ui.utils

class Animation(var duration: Long = 200, initialValue: Float = 0f) {
    var value: Float = initialValue
        private set

    private var startValue: Float = initialValue
    private var targetValue: Float = initialValue
    private var startTime: Long = 0L

    fun update(newTarget: Float) {
        if (newTarget != targetValue) {
            targetValue = newTarget
            startValue = value
            startTime = System.currentTimeMillis()
        }

        val elapsed = System.currentTimeMillis() - startTime
        val progress = (elapsed.toDouble() / duration).coerceIn(0.0, 1.0)

        value = if (progress >= 1.0) targetValue
        else startValue + (targetValue - startValue) * easeOutQuad(progress).toFloat()
    }

    fun set(v: Float) {
        this.value = v
        this.startValue = v
        this.targetValue = v
        this.startTime = 0L
    }

    companion object {
        fun easeOutQuad(t: Double): Double = 1.0 - (1.0 - t) * (1.0 - t)
        fun easeInOutQuad(t: Double): Double = if (t < 0.5) 2.0 * t * t else 1.0 - (-2.0 * t + 2.0).let { it * it } / 2.0
    }
}