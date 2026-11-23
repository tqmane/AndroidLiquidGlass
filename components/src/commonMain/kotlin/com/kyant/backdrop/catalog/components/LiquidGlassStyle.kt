package com.kyant.backdrop.catalog.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-wide Liquid Glass tuning, like the glass appearance settings in newer iOS releases:
 * [refraction] scales lens height/amount, [blur] scales blur radii, [opacity] scales surface tint
 * (0 is fully clear, 1 the default, 2 strongly tinted). Values are snapshot state, so changing them
 * redraws every glass surface that reads them.
 */
@Stable
class LiquidGlassStyle(refraction: Float = 1f, blur: Float = 1f, opacity: Float = 1f) {
    var refraction by mutableFloatStateOf(refraction)
    var blur by mutableFloatStateOf(blur)
    var opacity by mutableFloatStateOf(opacity)

    fun reset() {
        refraction = 1f
        blur = 1f
        opacity = 1f
    }

    companion object {
        val Clear get() = LiquidGlassStyle(refraction = 1.2f, blur = 0.5f, opacity = 0.3f)
        val Tinted get() = LiquidGlassStyle(refraction = 0.9f, blur = 1.4f, opacity = 1.8f)
    }
}

val LocalLiquidGlassStyle = staticCompositionLocalOf { LiquidGlassStyle() }
