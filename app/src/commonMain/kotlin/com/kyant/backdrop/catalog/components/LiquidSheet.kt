package com.kyant.backdrop.catalog.components

import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Heights a [LiquidSheet] can rest at, like `UISheetPresentationController.Detent`. */
sealed class SheetDetent {
    /** Fraction of the available height. */
    data class Fraction(val fraction: Float) : SheetDetent()

    companion object {
        val Medium: SheetDetent = Fraction(0.5f)
        val Large: SheetDetent = Fraction(1f)
    }
}

/**
 * iOS 26 sheet (`UISheetPresentationController`). At partial detents the sheet floats inset
 * from the screen edges as a Liquid Glass panel; dragging it to the large detent expands it
 * edge to edge and makes it more opaque. Includes grabber, dimming view, detent snapping with
 * velocity projection and swipe-to-dismiss.
 *
 * Place it last inside a full-size [BoxScope] that also contains the content [backdrop] records.
 */
@Composable
fun BoxScope.LiquidSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    detents: List<SheetDetent> = listOf(SheetDetent.Medium, SheetDetent.Large),
    showGrabber: Boolean = true,
    title: String = "Sheet",
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(visible) { onDismissRequest() }
    Box(Modifier.matchParentSize().zIndex(1f)) {
        val density = LocalDensity.current
        val isLightTheme = !isSystemInDarkTheme()
        val topInset = WindowInsets.safeDrawing.getTop(density) + with(density) { 10.dp.toPx() }
        val currentOnDismiss by rememberUpdatedState(onDismissRequest)

        BoxWithConstraints(Modifier.matchParentSize()) {
            val maxH = constraints.maxHeight.toFloat()
            val usable = maxH - topInset
            val stops = remember(detents, maxH, topInset) {
                detents.map { d ->
                    when (d) {
                        is SheetDetent.Fraction -> maxH - usable * d.fraction.coerceIn(0.1f, 1f)
                    }
                }.sortedDescending()
            }
            val lowestTop = stops.first()
            val highestTop = stops.last()
            val offsetY = remember { Animatable(maxH) }
            var shown by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(visible, stops) {
                if (visible) {
                    shown = true
                    offsetY.animateTo(lowestTop, spring(0.86f, 260f))
                } else {
                    offsetY.animateTo(maxH, spring(1f, 300f))
                    shown = false
                }
            }
            if (!shown && !visible) return@BoxWithConstraints

            val expansion = if (highestTop < lowestTop) {
                ((lowestTop - offsetY.value) / (lowestTop - highestTop)).coerceIn(0f, 1f)
            } else {
                1f
            }
            val presence = ((maxH - offsetY.value) / (maxH - lowestTop)).coerceIn(0f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = lerp(0.12f, 0.3f, expansion) * presence))
                    .clickable(interactionSource = null, indication = null) { currentOnDismiss() }
            )

            val inset = lerp(8f, 0f, expansion).dp
            val sheetHeight = with(density) { (maxH - minOf(offsetY.value, lowestTop)).toDp() } - inset
            val draggableState = rememberDraggableState { delta ->
                scope.launch {
                    val target = offsetY.value + delta
                    val resisted = if (target < highestTop) offsetY.value + delta * 0.25f else target
                    offsetY.snapTo(resisted)
                }
            }

            Column(
                modifier
                    .semantics {
                        paneTitle = title
                        dismiss { currentOnDismiss(); true }
                    }
                    .offset { IntOffset(0, (offsetY.value - minOf(offsetY.value, lowestTop)).roundToInt() + minOf(offsetY.value, lowestTop).roundToInt()) }
                    .padding(horizontal = inset)
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .liquidGlassPanel(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(lerp(36f, 30f, expansion).dp) },
                        containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = lerp(0.62f, 0.94f, expansion))
                        else Color(0xFF1C1C1E).copy(alpha = lerp(0.58f, 0.94f, expansion)),
                        blurRadius = lerp(10f, 24f, expansion).dp
                    )
                    .clickable(interactionSource = null, indication = null) {}
                    .draggable(
                        draggableState,
                        Orientation.Vertical,
                        onDragStopped = { velocity ->
                            val projected = offsetY.value + velocity * 0.18f
                            if (projected > lowestTop + (maxH - lowestTop) * 0.5f) {
                                currentOnDismiss()
                            } else {
                                val target = stops.minBy { abs(it - projected) }
                                offsetY.animateTo(target, spring(0.86f, 300f), velocity)
                            }
                        }
                    )
            ) {
                if (showGrabber) {
                    Box(
                        Modifier
                            .padding(top = 5.dp)
                            .align(Alignment.CenterHorizontally)
                            .size(36.dp, 5.dp)
                            .clip(Capsule())
                            .background(if (isLightTheme) Color(0xFF3C3C43).copy(alpha = 0.3f) else Color(0xFFEBEBF5).copy(alpha = 0.3f))
                    )
                }
                content()
            }
        }
    }
}
