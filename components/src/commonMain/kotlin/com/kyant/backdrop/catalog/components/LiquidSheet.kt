package com.kyant.backdrop.catalog.components

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.CoroutineStart
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
    content: @Composable LiquidSheetScope.() -> Unit,
) {
    BackHandler(visible) { onDismissRequest() }
    Box(Modifier.matchParentSize().zIndex(1f)) {
        val density = LocalDensity.current
        val isLightTheme = !isGlassDark()
        val topInset = WindowInsets.safeDrawing.getTop(density) + with(density) { 10.dp.toPx() }
        val currentOnDismiss by rememberUpdatedState(onDismissRequest)

        BoxWithConstraints(Modifier.matchParentSize()) {
            val maxH = constraints.maxHeight.toFloat()
            val usable = maxH - topInset
            val formSheet = maxWidth >= 600.dp
            val formHeight = with(density) { 366.dp.toPx() }
            val formMargin = with(density) { 24.dp.toPx() }
            // iPadOS form sheet: every detent is centered vertically; large is ~635 pt tall, medium ~366 pt.
            val formMinTop = topInset + formMargin
            // Centered within the safe area, so a taller detent always has a higher top even in a short window.
            val bottomInset = WindowInsets.safeDrawing.getBottom(density).toFloat()
            val formAvailable = maxH - formMinTop - formMargin - bottomInset
            val formLargeHeight = minOf(with(density) { 635.dp.toPx() }, formAvailable)
            // (top, bottom) of each detent, lowest sheet first.
            val bounds = remember(detents, maxH, topInset, bottomInset, formSheet) {
                detents.map { d ->
                    when (d) {
                        is SheetDetent.Fraction -> {
                            val f = d.fraction.coerceIn(0.1f, 1f)
                            if (!formSheet) {
                                maxH - usable * f to maxH
                            } else {
                                val h = if (f >= 1f) formLargeHeight else minOf(formLargeHeight, formHeight * f / 0.5f)
                                val top = formMinTop + ((formAvailable - h) / 2f).coerceAtLeast(0f)
                                top to top + h
                            }
                        }
                    }
                }.sortedByDescending { it.first }
            }
            val stops = remember(bounds) { bounds.map { it.first } }
            val lowestTop = stops.first()
            val highestTop = stops.last()
            val offsetY = remember { Animatable(maxH) }
            val sheetBackdrop = rememberLayerBackdrop()
            var shown by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            var detentIndex by remember { mutableIntStateOf(0) }
            var dismissVelocity by remember { mutableFloatStateOf(0f) }
            LaunchedEffect(visible, stops) {
                if (visible) {
                    if (!shown) detentIndex = 0
                    shown = true
                    offsetY.animateTo(stops.getOrElse(detentIndex) { lowestTop }, spring(0.86f, 260f))
                } else {
                    // iOS 26 slides a dismissed sheet out in ~0.2 s (a 2400 px drop is 26 % down at 50 ms, 80 % at
                    // 150 ms), keeping the speed of a swipe that dismissed it.
                    offsetY.animateTo(maxH, spring(1f, 400f), dismissVelocity.coerceAtLeast(0f))
                    dismissVelocity = 0f
                    shown = false
                }
            }
            if (!shown && !visible) return@BoxWithConstraints

            val expansion = if (highestTop < lowestTop) {
                ((lowestTop - offsetY.value) / (lowestTop - highestTop)).coerceIn(0f, 1f)
            } else {
                1f
            }
            val expansionState = rememberUpdatedState(expansion)
            // iOS 26 keeps the translucent material until the sheet passes halfway to the top detent, then fades it
            // opaque in one quick step (and back on the way down); it doesn't follow the finger linearly.
            val material = remember { Animatable(if (expansion >= 0.5f) 1f else 0f) }
            LaunchedEffect(material) {
                snapshotFlow { expansionState.value >= 0.5f }.collect { opaque ->
                    material.animateTo(if (opaque) 1f else 0f, tween(150))
                }
            }
            val materialState = remember(material) { derivedStateOf { material.value } }
            val presence = ((maxH - offsetY.value) / (maxH - lowestTop)).coerceIn(0f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = lerp(0.12f, 0.3f, expansion) * presence))
                    .clickable(interactionSource = null, indication = null) { currentOnDismiss() }
            )

            val inset = if (formSheet) 0.dp else lerp(8f, 0f, expansion).dp
            val sheetBottom = lerp(bounds.first().second, bounds.last().second, expansion)
            val sheetHeight = with(density) { (sheetBottom - minOf(offsetY.value, lowestTop)).toDp() } - inset
            // A snapTo queued behind a running animation hasn't moved offsetY yet, so the next delta in the same frame
            // must start from the pending top or it is dropped; the pending top is cleared once the snap lands.
            val dragTop = remember { floatArrayOf(Float.NaN) }
            fun currentTop() = dragTop[0].takeUnless { it.isNaN() } ?: offsetY.value
            fun dragTo(next: Float) {
                dragTop[0] = next
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    offsetY.snapTo(next)
                    if (dragTop[0] == next) dragTop[0] = Float.NaN
                }
            }
            suspend fun settle(velocity: Float) {
                val from = currentTop()
                dragTop[0] = Float.NaN
                offsetY.snapTo(from)
                val projected = from + velocity * 0.18f
                if (projected > lowestTop + (maxH - lowestTop) * 0.5f) {
                    dismissVelocity = velocity
                    currentOnDismiss()
                } else {
                    val target = stops.minBy { abs(it - projected) }
                    detentIndex = stops.indexOf(target)
                    // iOS 26 reaches a released detent in ~0.15 s, overshooting it slightly before settling.
                    offsetY.animateTo(target, spring(0.8f, 400f), velocity)
                }
            }
            val draggableState = rememberDraggableState { delta ->
                val top = currentTop()
                val target = top + delta
                dragTo(if (target < highestTop) top + delta * 0.25f else target)
            }

            val nestedScroll = remember(stops) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        val top = currentTop()
                        if (available.y >= 0f || top <= highestTop) return Offset.Zero
                        val next = maxOf(highestTop, top + available.y)
                        dragTo(next)
                        return Offset(0f, next - top)
                    }

                    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                        if (available.y <= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                        dragTo(currentTop() + available.y)
                        return Offset(0f, available.y)
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (stops.any { abs(it - currentTop()) < 0.5f }) {
                            dragTop[0] = Float.NaN
                            return Velocity.Zero
                        }
                        settle(available.y)
                        return available
                    }
                }
            }

            Column(
                modifier
                    .semantics {
                        paneTitle = title
                        dismiss { currentOnDismiss(); true }
                    }
                    .offset { IntOffset(0, (offsetY.value - minOf(offsetY.value, lowestTop)).roundToInt() + minOf(offsetY.value, lowestTop).roundToInt()) }
                    .then(
                        if (formSheet) Modifier.align(Alignment.TopCenter).width(580.dp)
                        else Modifier.padding(horizontal = inset).fillMaxWidth()
                    )
                    .height(sheetHeight)
                    .liquidGlassPanel(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(lerp(36f, 30f, expansion).dp) },
                        containerColor = if (isLightTheme) lerpPremultiplied(Color(0xFFFAFAFA).copy(alpha = 0.62f), Color(0xFFF2F2F7), materialState.value)
                        else lerpPremultiplied(Color(0xFF2A2A2A).copy(alpha = 0.72f), Color(0xFF1C1C1E), materialState.value),
                        blurRadius = lerp(10f, 24f, materialState.value).dp,
                        exportedBackdrop = sheetBackdrop
                    )
                    .clickable(interactionSource = null, indication = null) {}
                    .draggable(
                        draggableState,
                        Orientation.Vertical,
                        onDragStopped = { velocity -> settle(velocity) }
                    )
                    .nestedScroll(nestedScroll)
            ) {
                if (showGrabber) {
                    Box(
                        Modifier
                            .padding(top = 5.dp)
                            .align(Alignment.CenterHorizontally)
                            .size(36.dp, 5.dp)
                            .clip(Capsule())
                            .background(if (isLightTheme) Color(0xFF3C3C43).copy(alpha = 0.3f) else Color(0xFFEBEBF5).copy(alpha = 0.36f))
                    )
                }
                val sheetScope = remember(this, sheetBackdrop, formSheet) { LiquidSheetScope(this, sheetBackdrop, expansionState, materialState, formSheet) }
                sheetScope.content()
            }
        }
    }
}

/** Receiver of [LiquidSheet] content: the sheet's own [backdrop] for glass controls inside it, and how far it is expanded (0 at the lowest detent, 1 at the highest). */
@Stable
class LiquidSheetScope internal constructor(
    columnScope: ColumnScope,
    val backdrop: Backdrop,
    private val expansionState: State<Float>,
    private val materialState: State<Float>,
    /** True on regular-width windows (tablets), where the sheet is a centered iPad-style form sheet. */
    val isFormSheet: Boolean = false,
) : ColumnScope by columnScope {
    val expansion: Float get() = expansionState.value

    /** 0 while the sheet shows its translucent partial-detent material, 1 once it has turned opaque past halfway up. */
    val material: Float get() = materialState.value
}

/** Color lerp in premultiplied alpha, so a translucent tint fading into an opaque fill doesn't pass through grey. */
internal fun lerpPremultiplied(a: Color, b: Color, t: Float): Color {
    val alpha = lerp(a.alpha, b.alpha, t)
    if (alpha <= 0f) return Color.Transparent
    fun ch(x: Float, y: Float) = ((x * a.alpha) * (1f - t) + (y * b.alpha) * t) / alpha
    return Color(ch(a.red, b.red), ch(a.green, b.green), ch(a.blue, b.blue), alpha)
}

/** iOS 26 sheet navigation header: centered 17 pt semibold title with optional leading/trailing glass buttons; pass neither for a title-only sheet. */
@Composable
fun LiquidSheetScope.LiquidSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(modifier.fillMaxWidth().padding(top = 5.dp, start = 16.dp, end = 16.dp).height(52.dp)) {
        leading?.let { Box(Modifier.align(Alignment.CenterStart)) { it() } }
        BasicText(
            title,
            Modifier.align(Alignment.Center).padding(horizontal = 56.dp),
            style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold),
            maxLines = 1
        )
        trailing?.let { Box(Modifier.align(Alignment.CenterEnd)) { it() } }
    }
}

/** Inset-grouped section inside a sheet. Cells are a translucent fill at the medium detent and turn into opaque grouped cells once the sheet passes halfway up, as on iOS 26. */
@Composable
fun LiquidSheetScope.SheetSection(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isLightTheme = !isGlassDark()
    val cell = if (isLightTheme) lerpPremultiplied(Color.Black.copy(alpha = 0.05f), Color.White, material)
    else Color(0xFF2C2C2E)
    GroupedSection(title, cell, modifier, content = content)
}

/** Inset-grouped list section (26 dp corners, 15 pt secondary header). */
@Composable
fun GroupedSection(
    title: String?,
    cellColor: Color,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = horizontalPadding)) {
        title?.let {
            BasicText(it, Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp), style = TextStyle(GlassColors.secondaryLabel(), 15.sp))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedRectangle(26.dp)).background(cellColor), content = content)
    }
}

/** A 50 dp grouped-list row: optional tinted icon, label, optional trailing secondary value. */
@Composable
fun GroupedCell(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    value: String? = null,
    labelColor: Color = GlassColors.label(),
    showSeparator: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    if (showSeparator) {
        Box(Modifier.padding(start = if (icon != null) 54.dp else 16.dp).fillMaxWidth().height(0.5.dp).background(GlassColors.separator()))
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            GlassIcon(it, GlassColors.accent(), Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
        }
        BasicText(label, Modifier.weight(1f), style = TextStyle(labelColor, 17.sp), maxLines = 1)
        value?.let { BasicText(it, style = TextStyle(GlassColors.secondaryLabel(), 17.sp), maxLines = 1) }
    }
}
