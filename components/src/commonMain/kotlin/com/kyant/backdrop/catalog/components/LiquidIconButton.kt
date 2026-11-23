package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.utils.InteractiveHighlight
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import kotlin.math.min
import kotlin.math.tanh

/**
 * An icon-only Liquid Glass button: iOS 26 bar buttons / `.glass` circle buttons (44 dp).
 * Pass a [tint] for the `.glassProminent` style.
 */
@Composable
fun LiquidIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    tint: Color = Color.Unspecified,
    iconColor: Color = if (tint.isSpecified) Color.White else GlassColors.label(),
    surfaceColor: Color = Color.Unspecified,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        onClick = onClick,
        tint = tint,
        surfaceColor = surfaceColor,
        modifier = modifier
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .size(size)
    ) {
        GlassIcon(icon, iconColor, Modifier.size(iconSize))
    }
}

/**
 * iOS 26 text bar button (sheet and navigation bar Cancel / Done): a glass capsule that refracts what is behind it
 * and swells while held. Give [tint] (e.g. `GlassColors.accent()`) for the prominent confirm button.
 */
@Composable
fun LiquidGlassTextButton(
    label: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    labelColor: Color = if (tint.isSpecified) Color.White else GlassColors.label(),
    height: Dp = 44.dp,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        onClick = onClick,
        tint = tint,
        modifier = modifier.height(height)
    ) {
        BasicText(label, Modifier.padding(horizontal = 16.dp), style = TextStyle(labelColor, 17.sp))
    }
}

/**
 * Several bar buttons sharing one glass capsule, like the grouped trailing items of an
 * iOS 26 navigation bar or toolbar.
 */
@Composable
fun LiquidButtonGroup(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    tint: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .liquidGlass(backdrop, { Capsule() }, tint = tint)
            .height(height)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** An item inside [LiquidButtonGroup]. */
@Composable
fun RowScope.LiquidGroupItem(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = GlassColors.label(),
) {
    Box(
        modifier
            .fillMaxHeight()
            .width(40.dp)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        GlassIcon(icon, color, Modifier.size(20.dp))
    }
}

@Composable
fun GlassIcon(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Image(rememberVectorPainter(icon), null, modifier, colorFilter = ColorFilter.tint(color))
}

/** A capsule glass container with the press-to-grow interaction of [LiquidButton]. */
@Composable
fun LiquidGlassContainer(
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable () -> Unit,
) {
    val glass = LocalLiquidGlassStyle.current
    val isLightTheme = !isGlassDark()
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) { InteractiveHighlight(animationScope) }
    // A held button grows over its neighbours, as on iOS, instead of sliding under the next sibling.
    val lifted by remember { derivedStateOf { interactiveHighlight.pressProgress > 0.01f } }
    Box(
        modifier
            .zIndex(if (lifted) 1f else 0f)
            // Registered before the press layer so the bounds are the laid-out ones and [pressTransform] is applied once.
            .liquidGlassEffectMember({ interactiveHighlight.pressProgress }) { rect ->
                val (scale, translation) = pressTransform(interactiveHighlight, rect.size, density)
                Rect(
                    rect.center + translation - Offset(rect.width * scale / 2f, rect.height * scale / 2f),
                    Size(rect.width * scale, rect.height * scale)
                )
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur((2f.dp.toPx()) * glass.blur)
                    lens((12f.dp.toPx()) * glass.refraction, (24f.dp.toPx()) * glass.refraction)
                },
                layerBlock = {
                    val (scale, translation) = pressTransform(interactiveHighlight, size, density)
                    translationX = translation.x
                    translationY = translation.y
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    drawGlassFill(surfaceColor, tint, isLightTheme, glass.opacity)
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    }
                }
            )
            .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick)
            .then(interactiveHighlight.modifier)
            .then(interactiveHighlight.gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Press growth and drag offset of [LiquidGlassContainer]. iOS 26 interactive glass swells ~27 % while held
 * (native 62 pt search circle), capped for small bar buttons; wide capsules grow by at most 16 dp across.
 */
internal fun pressTransform(highlight: InteractiveHighlight, size: Size, density: Float): Pair<Float, Offset> {
    val progress = highlight.pressProgress
    val scale = lerp(1f, 1f + min(min(12f * density / size.height, 0.27f), 16f * density / size.width), progress)
    val maxOffset = size.minDimension
    val offset = highlight.offset
    return scale to Offset(maxOffset * tanh(0.05f * offset.x / maxOffset), maxOffset * tanh(0.05f * offset.y / maxOffset))
}
