package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * iOS 26 `UIStepper`: a 94 x 32 dp capsule well split by a short divider. The pressed half
 * lifts into a Liquid Glass pill, and holding auto-repeats like UIKit.
 */
@Composable
fun LiquidStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    range: IntRange = 0..10,
    step: Int = 1,
    wraps: Boolean = false,
    decrementIcon: ImageVector = GlassIcons.Minus,
    incrementIcon: ImageVector = GlassIcons.Plus,
) {
    val isLightTheme = !isGlassDark()
    val wellColor = if (isLightTheme) Color(0xFF767680).copy(alpha = 0.12f) else Color(0xFF767680).copy(alpha = 0.24f)
    val dividerColor = if (isLightTheme) Color(0xFF3C3C43).copy(alpha = 0.18f) else Color(0xFFEBEBF5).copy(alpha = 0.18f)
    val labelColor = GlassColors.label()
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    fun next(delta: Int): Int? {
        val raw = currentValue + delta
        return when {
            raw in range -> raw
            wraps -> if (raw > range.last) range.first else range.last
            else -> null
        }
    }

    Row(
        modifier
            .width(94.dp)
            .height(32.dp)
            .clip(Capsule())
            .background(wellColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperHalf(
            icon = decrementIcon,
            label = "Decrement",
            enabled = wraps || value - step >= range.first,
            labelColor = labelColor,
            backdrop = backdrop,
            onStep = { next(-step)?.let(currentOnValueChange) }
        )
        Box(Modifier.width(1.dp).height(18.dp).background(dividerColor))
        StepperHalf(
            icon = incrementIcon,
            label = "Increment",
            enabled = wraps || value + step <= range.last,
            labelColor = labelColor,
            backdrop = backdrop,
            onStep = { next(step)?.let(currentOnValueChange) }
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StepperHalf(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    labelColor: Color,
    backdrop: Backdrop,
    onStep: () -> Unit,
) {
    val glass = LocalLiquidGlassStyle.current
    val activeHighlight = glassHighlight()
    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    var pressed by remember { mutableStateOf(false) }
    var repeated by remember { mutableStateOf(false) }
    val currentOnStep by rememberUpdatedState(onStep)
    val currentEnabled by rememberUpdatedState(enabled)
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(500)
            while (pressed && currentEnabled) {
                repeated = true
                currentOnStep()
                delay(100)
            }
        }
    }
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .semantics {
                role = Role.Button
                contentDescription = label
                if (!enabled) disabled()
                onClick { if (enabled) currentOnStep(); enabled }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        scope.launch { press.animateTo(1f, spring(0.6f, 800f)) }
                        repeated = false
                        val released = tryAwaitRelease()
                        pressed = false
                        if (released && !repeated) currentOnStep()
                        scope.launch { press.animateTo(0f, spring(1f, 300f)) }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(width = 50.dp, height = 36.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur((2f.dp.toPx()) * glass.blur)
                        lens((10f.dp.toPx()) * glass.refraction, (18f.dp.toPx()) * glass.refraction, chromaticAberration = true)
                    },
                    highlight = { activeHighlight.copy(alpha = activeHighlight.alpha * press.value) },
                    shadow = { Shadow(radius = 12.dp, color = Color.Black.copy(alpha = 0.12f * press.value)) },
                    layerBlock = {
                        val p = press.value
                        alpha = p
                        scaleX = lerp(0.7f, 1f, p)
                        scaleY = lerp(0.7f, 1f, p)
                    }
                )
        )
        GlassIcon(
            icon,
            labelColor.copy(alpha = if (enabled) 1f else 0.25f),
            Modifier.size(17.dp).graphicsLayer {
                val s = lerp(1f, 1.15f, press.value)
                scaleX = s
                scaleY = s
            }
        )
    }
}
