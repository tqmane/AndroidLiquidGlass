package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor

/** iOS 26 `UIProgressView` (`.default`): a 4 dp capsule track with an accent fill. */
@Composable
fun LiquidProgressView(
    progress: Float,
    modifier: Modifier = Modifier,
    tint: Color = GlassColors.accent(),
    trackColor: Color = GlassColors.fill(),
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), spring(1f, 300f))
    Canvas(
        modifier
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f) }
            .fillMaxWidth()
            .height(4.dp)
    ) {
        drawCapsule(trackColor, size.width)
        if (animated > 0f) drawCapsule(tint, (size.width * animated).coerceAtLeast(size.height))
    }
}

enum class ActivityIndicatorSize(val diameter: Dp, val strokeWidth: Dp) {
    Medium(20.dp, 2.dp),
    Large(37.dp, 3.2.dp),
}

/** iOS `UIActivityIndicatorView`: eight fading spokes that tick around once per second. */
@Composable
fun LiquidActivityIndicator(
    modifier: Modifier = Modifier,
    size: ActivityIndicatorSize = ActivityIndicatorSize.Medium,
    color: Color = GlassColors.secondaryLabel(),
) {
    val spokes = 8
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        0f, spokes.toFloat(),
        infiniteRepeatable(tween(1000, easing = LinearEasing))
    )
    Canvas(
        modifier
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }
            .size(size.diameter)
    ) {
        val head = floor(phase).toInt()
        val stroke = size.strokeWidth.toPx()
        val outer = this.size.minDimension / 2f
        val inner = outer * 0.5f
        for (i in 0 until spokes) {
            val age = ((head - i) % spokes + spokes) % spokes
            val alpha = 1f - age * (0.85f / spokes)
            rotate(360f / spokes * i) {
                drawLine(
                    color.copy(alpha = color.alpha * alpha),
                    Offset(center.x, center.y - inner),
                    Offset(center.x, center.y - outer + stroke / 2f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/** iOS 26 circular gauge (`Gauge` with `.accessoryCircularCapacity`): a 4 dp ring with the value in its center. */
@Composable
fun LiquidCircularGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    label: String = "${(progress.coerceIn(0f, 1f) * 100).toInt()}",
    color: Color = GlassColors.label(),
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), spring(1f, 300f))
    val track = GlassColors.fill()
    androidx.compose.foundation.layout.Box(
        modifier
            .size(size)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2f
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            drawArc(color, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round))
        }
        androidx.compose.foundation.text.BasicText(
            label,
            style = androidx.compose.ui.text.TextStyle(color, (size.value * 0.36f).sp, androidx.compose.ui.text.font.FontWeight.Medium)
        )
    }
}

