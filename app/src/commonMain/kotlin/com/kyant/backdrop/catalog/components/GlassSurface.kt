package com.kyant.backdrop.catalog.components

import com.kyant.backdrop.backdrops.LayerBackdrop
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

/** iOS 26 system colors used by the glass components. */
object GlassColors {
    val Blue = Color(0xFF0088FF)
    val BlueDark = Color(0xFF0091FF)
    val Red = Color(0xFFFF383C)
    val RedDark = Color(0xFFFF4245)
    val Green = Color(0xFF34C759)

    @Composable
    @ReadOnlyComposable
    fun accent(): Color = if (isSystemInDarkTheme()) BlueDark else Blue

    @Composable
    fun red(): Color = if (isSystemInDarkTheme()) RedDark else Red

    @Composable
    @ReadOnlyComposable
    fun destructive(): Color = if (isSystemInDarkTheme()) RedDark else Red

    @Composable
    @ReadOnlyComposable
    fun label(): Color = if (isSystemInDarkTheme()) Color.White else Color.Black

    @Composable
    @ReadOnlyComposable
    fun secondaryLabel(): Color =
        if (isSystemInDarkTheme()) Color(0xFFEBEBF5).copy(alpha = 0.6f)
        else Color(0xFF3C3C43).copy(alpha = 0.6f)

    @Composable
    @ReadOnlyComposable
    fun separator(): Color =
        if (isSystemInDarkTheme()) Color(0xFF545458).copy(alpha = 0.6f)
        else Color(0xFF3C3C43).copy(alpha = 0.29f)

    @Composable
    @ReadOnlyComposable
    fun fill(): Color =
        if (isSystemInDarkTheme()) Color(0xFF787880).copy(alpha = 0.36f)
        else Color(0xFF787880).copy(alpha = 0.2f)
}

/**
 * The "regular" Liquid Glass material: a blurred, refracted backdrop with a specular rim
 * and a soft drop shadow, optionally tinted.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    shape: () -> Shape,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    blurRadius: Dp = 2.dp,
    refractionHeight: Dp = 12.dp,
    refractionAmount: Dp = 24.dp,
    shadow: Boolean = true,
): Modifier {
    val isLightTheme = !isSystemInDarkTheme()
    return drawBackdrop(
        backdrop = backdrop,
        shape = shape,
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
            lens(refractionHeight.toPx(), refractionAmount.toPx(), chromaticAberration = true)
        },
        highlight = { Highlight.Default },
        shadow = if (shadow) {
            { Shadow(radius = 24.dp, color = Color.Black.copy(alpha = if (isLightTheme) 0.12f else 0.3f)) }
        } else {
            null
        },
        onDrawSurface = {
            if (tint.isSpecified) {
                drawRect(tint, blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.75f))
            }
            if (surfaceColor.isSpecified) {
                drawRect(surfaceColor)
            }
        }
    )
}

/**
 * The thicker panel material used by sheets, alerts, menus and popovers: more blur, lighter
 * refraction and a translucent fill so text stays legible over any content.
 */
@Composable
fun Modifier.liquidGlassPanel(
    backdrop: Backdrop,
    shape: () -> Shape,
    containerColor: Color = Color.Unspecified,
    blurRadius: Dp = 12.dp,
    refractionHeight: Dp = 16.dp,
    refractionAmount: Dp = 32.dp,
    exportedBackdrop: LayerBackdrop? = null,
): Modifier {
    val isLightTheme = !isSystemInDarkTheme()
    val surface = if (containerColor.isSpecified) containerColor
    else if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = 0.6f)
    else Color(0xFF1C1C1E).copy(alpha = 0.55f)
    return drawBackdrop(
        backdrop = backdrop,
        shape = shape,
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
            lens(refractionHeight.toPx(), refractionAmount.toPx(), depthEffect = true)
        },
        highlight = { Highlight.Plain },
        shadow = { Shadow(radius = 40.dp, color = Color.Black.copy(alpha = if (isLightTheme) 0.14f else 0.4f)) },
        exportedBackdrop = exportedBackdrop,
        onDrawSurface = { drawRect(surface) }
    )
}
