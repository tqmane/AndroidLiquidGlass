package com.kyant.backdrop.catalog.components

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.runtime.staticCompositionLocalOf
import com.kyant.backdrop.backdrops.LayerBackdrop
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.Shadow

/** iOS 26 system colors used by the glass components. */
/** Forces the dark (true) or light (false) glass appearance for a subtree, e.g. video player chrome, which is always dark on iOS. Null follows the system theme. */
val LocalGlassDarkAppearance = staticCompositionLocalOf<Boolean?> { null }

/** Accent override for a glass subtree. Null retains each control's system color. */
val LocalGlassAccent = staticCompositionLocalOf<Color?> { null }

/** Drop shadow of the glass surfaces in a subtree (buttons, button groups, toolbar capsules). Null uses [glassShadow]'s iOS 26 default. */
val LocalGlassShadow = staticCompositionLocalOf<Shadow?> { null }

/**
 * Rim highlight of regular glass surfaces (buttons, bars, capsules). Null uses [glassHighlight]'s default,
 * which blends softly on dark surfaces instead of drawing an additive white edge.
 */
val LocalGlassHighlight = staticCompositionLocalOf<Highlight?> { null }

/**
 * Rim highlight of thicker glass panels (sheets, menus, popovers, alerts). Null uses [glassPanelHighlight]'s
 * default, which blends subtly into dark sheet surfaces.
 */
val LocalGlassPanelHighlight = staticCompositionLocalOf<Highlight?> { null }

/**
 * Drop shadow of regular glass: [LocalGlassShadow], else iOS 26's soft toolbar shadow
 * (measured on an iPhone toolbar capsule: no x offset, 2 pt down, 6 pt blur, 8 % black).
 */
@Composable
@ReadOnlyComposable
fun glassShadow(): Shadow = LocalGlassShadow.current ?: Shadow(
    radius = 6.dp,
    offset = DpOffset(0.dp, 2.dp),
    color = Color.Black.copy(alpha = if (isGlassDark()) 0.2f else 0.08f)
)

/**
 * Rim highlight of regular glass: [LocalGlassHighlight], else [Highlight.Default] in light mode and a
 * softly blended directional rim in dark mode so borders do not blow out against dark backgrounds.
 */
@Composable
@ReadOnlyComposable
fun glassHighlight(): Highlight = LocalGlassHighlight.current ?: if (isGlassDark()) {
    Highlight(
        width = 0.5.dp,
        alpha = 0.24f,
        style = HighlightStyle.Default(
            color = Color.White,
            blendMode = DrawScope.DefaultBlendMode,
        ),
    )
} else {
    Highlight.Default
}

/**
 * Rim highlight of thicker glass panels: [LocalGlassPanelHighlight], else [Highlight.Plain] in light mode
 * and a subtle low-contrast hairline in dark mode.
 */
@Composable
@ReadOnlyComposable
fun glassPanelHighlight(): Highlight = LocalGlassPanelHighlight.current ?: if (isGlassDark()) {
    Highlight(
        width = 0.5.dp,
        alpha = 1f,
        style = HighlightStyle.Plain(
            color = Color.White.copy(alpha = 0.08f),
            blendMode = DrawScope.DefaultBlendMode,
        ),
    )
} else {
    Highlight.Plain
}

@Composable
@ReadOnlyComposable
fun isGlassDark(): Boolean = LocalGlassDarkAppearance.current ?: isSystemInDarkTheme()

object GlassColors {
    val Blue = Color(0xFF0088FF)
    val BlueDark = Color(0xFF0091FF)
    val Red = Color(0xFFFF383C)
    val RedDark = Color(0xFFFF4245)
    val Green = Color(0xFF34C759)

    @Composable
    @ReadOnlyComposable
    fun accent(): Color = LocalGlassAccent.current ?: if (isGlassDark()) BlueDark else Blue

    @Composable
    @ReadOnlyComposable
    internal fun toggleAccent(): Color = LocalGlassAccent.current ?: if (isGlassDark()) Color(0xFF30D158) else Green

    @Composable
    fun red(): Color = if (isGlassDark()) RedDark else Red

    @Composable
    @ReadOnlyComposable
    fun destructive(): Color = if (isGlassDark()) RedDark else Red

    @Composable
    @ReadOnlyComposable
    fun label(): Color = if (isGlassDark()) Color.White else Color.Black

    @Composable
    @ReadOnlyComposable
    fun secondaryLabel(): Color =
        if (isGlassDark()) Color(0xFFEBEBF5).copy(alpha = 0.6f)
        else Color(0xFF3C3C43).copy(alpha = 0.6f)

    @Composable
    @ReadOnlyComposable
    fun separator(): Color =
        if (isGlassDark()) Color(0xFF545458).copy(alpha = 0.6f)
        else Color(0xFF3C3C43).copy(alpha = 0.29f)

    @Composable
    @ReadOnlyComposable
    fun fill(): Color =
        if (isGlassDark()) Color(0xFF787880).copy(alpha = 0.36f)
        else Color(0xFF787880).copy(alpha = 0.2f)
}

/**
 * The "regular" Liquid Glass material: a blurred, refracted backdrop with a specular rim
 * and a soft drop shadow ([shadowStyle], see [glassShadow]), optionally tinted.
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
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    shadowStyle: Shadow = glassShadow(),
    highlightStyle: Highlight? = glassHighlight(),
): Modifier {
    val glass = LocalLiquidGlassStyle.current
    val isLightTheme = !isGlassDark()
    return drawBackdrop(
        backdrop = backdrop,
        shape = shape,
        layerBlock = layerBlock,
        effects = {
            vibrancy()
            blur((blurRadius.toPx()) * glass.blur)
            lens((refractionHeight.toPx()) * glass.refraction, (refractionAmount.toPx()) * glass.refraction, chromaticAberration = true)
        },
        highlight = highlightStyle?.let { { it } },
        shadow = if (shadow) {
            { shadowStyle }
        } else {
            null
        },
        onDrawSurface = {
            if (tint.isSpecified) {
                drawRect(tint, blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                drawRect(tint.copy(alpha = (0.75f * glass.opacity).coerceIn(0f, 1f)))
            }
            drawGlassFill(surfaceColor, tint, isLightTheme, glass.opacity)
        }
    )
}

/**
 * Fill of the regular material. Untinted glass gets the same neutral 40 % fill as the tab bars, so a button
 * beside a bar (search circle, X beside the search field) reads as the same material instead of a brighter one.
 */
internal fun glassSurfaceFill(surfaceColor: Color, tint: Color, isLightTheme: Boolean, opacity: Float): Color {
    val surface = if (surfaceColor.isSpecified) surfaceColor
    else if (tint.isSpecified) Color.Transparent
    else if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = 0.4f)
    else Color(0xFF121212).copy(alpha = 0.4f)
    return surface.copy(alpha = (surface.alpha * opacity).coerceIn(0f, 1f))
}

/**
 * iOS 26 dark regular glass: content behind is dimmed, then lifted, so over black it reads as the native
 * (25, 25, 25) and over colour it darkens instead of turning into a flat grey film.
 */
internal fun DrawScope.drawDarkGlass(opacity: Float, dim: Float = 0.35f, lift: Float = 0.1f) {
    drawRect(Color.Black.copy(alpha = (dim * opacity).coerceIn(0f, 1f)))
    drawRect(Color.White.copy(alpha = (lift * opacity).coerceIn(0f, 1f)), blendMode = androidx.compose.ui.graphics.BlendMode.Plus)
}

/** Draws the regular-material fill: [glassSurfaceFill], or [drawDarkGlass] for untinted dark glass. */
internal fun DrawScope.drawGlassFill(surfaceColor: Color, tint: Color, isLightTheme: Boolean, opacity: Float) {
    if (!isLightTheme && !surfaceColor.isSpecified && !tint.isSpecified) drawDarkGlass(opacity)
    else drawRect(glassSurfaceFill(surfaceColor, tint, isLightTheme, opacity))
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
    highlightStyle: Highlight? = glassPanelHighlight(),
): Modifier {
    val glass = LocalLiquidGlassStyle.current
    val isLightTheme = !isGlassDark()
    val surface = if (containerColor.isSpecified) containerColor
    else if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = 0.6f)
    else Color.Unspecified
    return drawBackdrop(
        backdrop = backdrop,
        shape = shape,
        effects = {
            vibrancy()
            blur((blurRadius.toPx()) * glass.blur)
            lens((refractionHeight.toPx()) * glass.refraction, (refractionAmount.toPx()) * glass.refraction, depthEffect = true)
        },
        highlight = highlightStyle?.let { { it } },
        shadow = { Shadow(radius = 40.dp, color = Color.Black.copy(alpha = if (isLightTheme) 0.14f else 0.4f)) },
        exportedBackdrop = exportedBackdrop,
        onDrawSurface = {
            // Dark panels (menus, popovers, alerts) measure ~(24, 26, 24) over black on iOS 26.
            if (surface.isSpecified) drawRect(surface.copy(alpha = (surface.alpha * glass.opacity).coerceIn(0f, 1f)))
            else drawDarkGlass(glass.opacity, dim = 0.45f, lift = 0.095f)
        }
    )
}
