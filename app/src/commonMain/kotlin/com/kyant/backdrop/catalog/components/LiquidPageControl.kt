package com.kyant.backdrop.catalog.components

import kotlin.math.roundToInt
import kotlin.math.abs
import androidx.compose.ui.semantics.setProgress
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule

/**
 * iOS 26 `UIPageControl`: 7 dp dots on 16 dp pitch. Touching it raises a Liquid Glass platter
 * and lets the user scrub across pages, as UIKit's interactive page control does.
 */
@Composable
fun LiquidPageControl(
    pageCount: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    alwaysShowPlatter: Boolean = false,
    indicatorColor: Color = Color.White.copy(alpha = 0.3f),
    currentIndicatorColor: Color = Color.White,
) {
    if (pageCount <= 0) return
    val pitch = 16.dp
    val dot = 7.dp
    var interacting by remember { mutableStateOf(false) }
    val platter by animateFloatAsState(
        if (interacting || alwaysShowPlatter) 1f else 0f,
        spring(0.8f, 500f)
    )
    val animatedPage by animateFloatAsState(currentPage.toFloat(), spring(0.8f, 600f))
    val currentOnPageChange by rememberUpdatedState(onPageChange)
    val currentPageState by rememberUpdatedState(currentPage)

    Box(
        modifier
            .semantics {
                stateDescription = "Page ${currentPage + 1} of $pageCount"
                progressBarRangeInfo = ProgressBarRangeInfo(currentPage.toFloat(), 0f..(pageCount - 1).toFloat(), pageCount - 2)
                setProgress { value ->
                    val target = value.roundToInt().coerceIn(0, pageCount - 1)
                    if (target != currentPageState) currentOnPageChange(target)
                    true
                }
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(8f.dp.toPx(), 16f.dp.toPx())
                },
                highlight = { Highlight.Default.copy(alpha = platter) },
                shadow = null,
                layerBlock = {
                    val s = lerp(1f, 1.08f, platter)
                    scaleX = s
                    scaleY = s
                },
                onDrawBackdrop = { draw -> if (platter > 0f) { draw() } },
                onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.12f * platter)) }
            )
            .height(26.dp)
            .width(pitch * pageCount + 16.dp)
            .pointerInput(pageCount) {
                fun pageAt(x: Float): Int {
                    val start = 8.dp.toPx()
                    return ((x - start) / pitch.toPx()).toInt().coerceIn(0, pageCount - 1)
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    interacting = true
                    var dragged = false
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        if (!dragged && abs(change.position.x - down.position.x) > viewConfiguration.touchSlop) dragged = true
                        if (dragged) {
                            change.consume()
                            val page = pageAt(change.position.x)
                            if (page != currentPageState) currentOnPageChange(page)
                        }
                    }
                    interacting = false
                    if (!dragged) {
                        val target = if (down.position.x < size.width / 2f) currentPageState - 1 else currentPageState + 1
                        currentOnPageChange(target.coerceIn(0, pageCount - 1))
                    }
                }
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(Modifier.width(pitch * pageCount).height(26.dp)) {
            val pitchPx = pitch.toPx()
            val dotPx = dot.toPx()
            val cy = size.height / 2f
            for (i in 0 until pageCount) {
                val cx = pitchPx * i + pitchPx / 2f
                val emphasis = (1f - kotlin.math.abs(animatedPage - i)).coerceIn(0f, 1f)
                drawCircle(
                    lerpColor(indicatorColor, currentIndicatorColor, emphasis),
                    radius = dotPx / 2f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

internal fun lerpColor(a: Color, b: Color, t: Float): Color =
    androidx.compose.ui.graphics.lerp(a, b, t)

/** Draws a rounded progress capsule, shared by [LiquidProgressView]. */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCapsule(color: Color, width: Float) {
    drawRoundRect(color, size = Size(width, size.height), cornerRadius = CornerRadius(size.height / 2f))
}
