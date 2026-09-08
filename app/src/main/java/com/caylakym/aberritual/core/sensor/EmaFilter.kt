package com.caylakym.aberritual.core.sensor

import kotlin.math.abs

class EmaFilter(
    private val alpha: Float = 0.18f,
    private val deadband: Float = 0.005f
) {
    private var currentValue: Float? = null

    fun filter(targetValue: Float): Float {
        val current = currentValue
        if (current == null) {
            currentValue = targetValue
            return targetValue
        }

        if (abs(targetValue - current) < deadband) {
            return current
        }

        val smoothed = (alpha * targetValue) + ((1.0f - alpha) * current)
        currentValue = smoothed
        return smoothed
    }

    fun reset() {
        currentValue = null
    }
}
