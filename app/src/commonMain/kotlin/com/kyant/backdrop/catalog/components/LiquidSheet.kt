package com.kyant.backdrop.catalog.components

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
import androidx.compose.ui.graphics.lerp as lerpColor
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
    content: @Composable LiquidSheetScope.() -> Unit,
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
            val formSheet = maxWidth >= 600.dp
            val formHeight = with(density) { 366.dp.toPx() }
            val formMargin = with(density) { 24.dp.toPx() }
            val formGap = if (formSheet) ((maxH - formHeight) / 2f).coerceAtLeast(formMargin) else 0f
            val stops = remember(detents, maxH, topInset, formSheet) {
                detents.map { d ->
                    when (d) {
                        is SheetDetent.Fraction -> {
                            val f = d.fraction.coerceIn(0.1f, 1f)
                            if (!formSheet) maxH - usable * f
                            else if (f >= 1f) topInset + formMargin
                            else maxOf(topInset + formMargin, maxH - formGap - formHeight * f / 0.5f)
                        }
                    }
                }.sortedDescending()
            }
            val lowestTop = stops.first()
            val highestTop = stops.last()
            val offsetY = remember { Animatable(maxH) }
            val sheetBackdrop = rememberLayerBackdrop()
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
            val expansionState = rememberUpdatedState(expansion)
            val presence = ((maxH - offsetY.value) / (maxH - lowestTop)).coerceIn(0f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = lerp(0.12f, 0.3f, expansion) * presence))
                    .clickable(interactionSource = null, indication = null) { currentOnDismiss() }
            )

            val inset = if (formSheet) 0.dp else lerp(8f, 0f, expansion).dp
            val sheetHeight = with(density) { (maxH - formGap - minOf(offsetY.value, lowestTop)).toDp() } - inset
            suspend fun settle(velocity: Float) {
                val projected = offsetY.value + velocity * 0.18f
                if (projected > lowestTop + (maxH - lowestTop) * 0.5f) {
                    currentOnDismiss()
                } else {
                    val target = stops.minBy { abs(it - projected) }
                    offsetY.animateTo(target, spring(0.86f, 300f), velocity)
                }
            }
            val draggableState = rememberDraggableState { delta ->
                scope.launch {
                    val target = offsetY.value + delta
                    val resisted = if (target < highestTop) offsetY.value + delta * 0.25f else target
                    offsetY.snapTo(resisted)
                }
            }

            val nestedScroll = remember(stops) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (available.y >= 0f || offsetY.value <= highestTop) return Offset.Zero
                        val next = maxOf(highestTop, offsetY.value + available.y)
                        val consumed = next - offsetY.value
                        scope.launch { offsetY.snapTo(next) }
                        return Offset(0f, consumed)
                    }

                    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                        if (available.y <= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                        scope.launch { offsetY.snapTo(offsetY.value + available.y) }
                        return Offset(0f, available.y)
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (stops.any { abs(it - offsetY.value) < 0.5f }) return Velocity.Zero
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
                        containerColor = if (isLightTheme) lerpColor(Color(0xFFFAFAFA).copy(alpha = 0.62f), Color(0xFFF2F2F7), expansion)
                        else lerpColor(Color(0xFF2C2C2E).copy(alpha = 0.62f), Color(0xFF1C1C1E), expansion),
                        blurRadius = lerp(10f, 24f, expansion).dp,
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
                            .background(if (isLightTheme) Color(0xFF3C3C43).copy(alpha = 0.3f) else Color(0xFFEBEBF5).copy(alpha = 0.3f))
                    )
                }
                val sheetScope = remember(this, sheetBackdrop, formSheet) { LiquidSheetScope(this, sheetBackdrop, expansionState, formSheet) }
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
    /** True on regular-width windows (tablets), where the sheet is a centered iPad-style form sheet. */
    val isFormSheet: Boolean = false,
) : ColumnScope by columnScope {
    val expansion: Float get() = expansionState.value
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

/** Inset-grouped section inside a sheet. Cells are a translucent fill at the medium detent and turn into opaque grouped cells as the sheet expands, as on iOS 26. */
@Composable
fun LiquidSheetScope.SheetSection(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isLightTheme = !isSystemInDarkTheme()
    val cell = if (isLightTheme) lerpColor(Color.Black.copy(alpha = 0.05f), Color.White, expansion)
    else lerpColor(Color.Black.copy(alpha = 0.3f), Color(0xFF2C2C2E), expansion)
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
