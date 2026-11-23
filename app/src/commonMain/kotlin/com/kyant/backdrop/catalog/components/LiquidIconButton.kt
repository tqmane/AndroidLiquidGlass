package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.Image
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
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) { InteractiveHighlight(animationScope) }
    Box(
        modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur((2f.dp.toPx()) * glass.blur)
                    lens((12f.dp.toPx()) * glass.refraction, (24f.dp.toPx()) * glass.refraction)
                },
                layerBlock = {
                    val progress = interactiveHighlight.pressProgress
                    val scale = lerp(1f, 1f + 6f.dp.toPx() / size.height, progress)
                    val maxOffset = size.minDimension
                    val offset = interactiveHighlight.offset
                    translationX = maxOffset * tanh(0.05f * offset.x / maxOffset)
                    translationY = maxOffset * tanh(0.05f * offset.y / maxOffset)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    if (surfaceColor.isSpecified) drawRect(surfaceColor.copy(alpha = (surfaceColor.alpha * glass.opacity).coerceIn(0f, 1f)))
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
