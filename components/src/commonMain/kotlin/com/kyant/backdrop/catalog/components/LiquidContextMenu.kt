package com.kyant.backdrop.catalog.components

import androidx.compose.ui.draw.drawWithContent
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

/**
 * Press state of a context-menu source, shared by [liquidContextMenuSource] and [LiquidContextMenu]:
 * the source swells while held, and the lifted preview settles back from that swollen size when the menu opens.
 */
@Stable
class LiquidContextMenuState {
    internal val scale = Animatable(1f)
    internal var previewDrawn by mutableStateOf(false)
    var bounds by mutableStateOf(Rect.Zero)
        internal set
}

internal suspend fun LiquidContextMenuState.awaitPreviewDrawn() {
    withTimeoutOrNull(500) { snapshotFlow { previewDrawn }.first { it } }
}

@Composable
fun rememberLiquidContextMenuState(): LiquidContextMenuState = remember { LiquidContextMenuState() }

/** Scale the source swells to by the time the long press fires (measured on iOS 26: +3.8 % over ~0.55 s). */
internal const val ContextMenuPressedScale = 1.038f

/**
 * Long-press source for [LiquidContextMenu]: records its bounds, swells toward [ContextMenuPressedScale]
 * while the finger is held and calls [onLongPress] at the long-press timeout; a short press calls [onClick].
 */
fun Modifier.liquidContextMenuSource(
    state: LiquidContextMenuState,
    onLongPress: () -> Unit,
    onClick: (() -> Unit)? = null,
): Modifier = this
    .onGloballyPositioned { state.bounds = it.boundsInRoot() }
    .graphicsLayer {
        scaleX = state.scale.value
        scaleY = state.scale.value
    }
    .pointerInput(state) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                state.previewDrawn = false
                val timeout = viewConfiguration.longPressTimeoutMillis
                val swell = launch {
                    delay(timeout / 5)
                    state.scale.animateTo(ContextMenuPressedScale, tween((timeout * 4 / 5).toInt(), easing = LinearOutSlowInEasing))
                }
                var ended = false
                val up = withTimeoutOrNull(timeout) { waitForUpOrCancellation().also { ended = true } }
                swell.cancel()
                if (!ended) {
                    onLongPress()
                    // The source is covered by the lifted preview; restore it underneath.
                    launch {
                        state.scale.snapTo(ContextMenuPressedScale)
                        // Keep the swell until the lifted preview has drawn over it, so no frame shows the rest size.
                        state.awaitPreviewDrawn()
                        state.scale.snapTo(1f)
                    }
                    waitForUpOrCancellation()
                } else {
                    launch { state.scale.animateTo(1f, spring(0.5f, 500f)) }
                    if (up != null && abs(up.position.x - down.position.x) < viewConfiguration.touchSlop) onClick?.invoke()
                }
            }
        }
    }

/** Records the root bounds of a long-press target for [LiquidContextMenu]. */
fun Modifier.contextMenuAnchor(onBounds: (Rect) -> Unit): Modifier =
    onGloballyPositioned { onBounds(it.boundsInRoot()) }

/**
 * iOS 26 context menu (`UIContextMenuInteraction`): the background blurs and dims, the
 * lifted [preview] is redrawn above it at [anchorBounds], and a Liquid Glass menu grows out
 * from under the preview.
 */
@Composable
fun BoxScope.LiquidContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    anchorBounds: Rect,
    entries: List<MenuEntry>,
    state: LiquidContextMenuState? = null,
    preview: @Composable () -> Unit,
) {
    val glass = LocalLiquidGlassStyle.current
    BackHandler(expanded) { onDismissRequest() }
    Box(Modifier.matchParentSize().zIndex(1f)) {
        val progress = rememberPresentation(expanded) ?: return
        val density = LocalDensity.current
        var origin by remember { mutableStateOf(Offset.Zero) }
        var container by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        var menuHeight by remember { mutableStateOf(0) }
        var submenu by remember(expanded) { mutableStateOf<MenuEntry.Submenu?>(null) }
        val p = progress.value
        // The preview takes over at the swollen press size and settles back to its own size as the menu grows (iOS 26: ~0.17 s, no overshoot).
        val lift = remember(expanded) { Animatable(if (expanded && state != null) ContextMenuPressedScale else 1f) }
        val placed = container != androidx.compose.ui.unit.IntSize.Zero
        LaunchedEffect(expanded, placed) {
            if (placed) lift.animateTo(1f, spring(1f, 600f))
        }

        Box(
            Modifier
                .matchParentSize()
                .onGloballyPositioned { origin = it.positionInRoot(); container = it.size }
                // Nothing is drawn until the preview can be placed, so no frame shows the blur without the card.
                .drawWithContent { if (placed) drawContent() }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(0.dp) },
                    effects = { blur((18f.dp.toPx() * p.coerceIn(0f, 1f)) * glass.blur) },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.12f * p.coerceIn(0f, 1f))) }
                )
                .clickable(interactionSource = null, indication = null, onClick = onDismissRequest)
                .semantics {
                    paneTitle = "Context menu"
                    dismiss { onDismissRequest(); true }
                }
        )
        val left = anchorBounds.left - origin.x
        val top = anchorBounds.top - origin.y
        with(density) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                    .size(anchorBounds.width.toDp(), anchorBounds.height.toDp())
                    .drawWithContent {
                        if (placed) {
                            drawContent()
                            if (expanded && state != null && !state.previewDrawn) state.previewDrawn = true
                        }
                    }
                    .graphicsLayer {
                        val s = lift.value
                        scaleX = s
                        scaleY = s
                        shadowElevation = 24.dp.toPx() * p.coerceIn(0f, 1f)
                        shape = RoundedRectangle(24.dp)
                        clip = true
                    }
            ) {
                preview()
            }
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        val margin = 16.dp.toPx()
                        val x = left.coerceIn(margin, (container.width - 250.dp.toPx() - margin).coerceAtLeast(margin))
                        val below = top + anchorBounds.height + 12.dp.toPx()
                        val y = if (below + menuHeight > container.height - margin) top - 12.dp.toPx() - menuHeight else below
                        IntOffset(x.roundToInt(), y.coerceIn(margin, (container.height - menuHeight - margin).coerceAtLeast(margin)).roundToInt())
                    }
                    .onGloballyPositioned { menuHeight = it.size.height }
                    .width(250.dp)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.1f, 0f)
                        scaleX = lerp(0.3f, 1f, p)
                        scaleY = lerp(0.2f, 1f, p)
                        alpha = p.coerceIn(0f, 1f)
                        // Per-draw alpha: an offscreen alpha layer drops the glass outline clip and flashes a rectangle.
                        compositingStrategy = CompositingStrategy.ModulateAlpha
                    }
                    .liquidGlassPanel(backdrop, { RoundedRectangle(26.dp) })
                    .clickable(interactionSource = null, indication = null) {}
                    .padding(vertical = 8.dp)
            ) {
                val open = submenu
                if (open == null) {
                    MenuEntries(entries, onDismissRequest) { submenu = it }
                } else {
                    MenuEntries(listOf(MenuEntry.Action(open.title, GlassIcons.ChevronDown) { submenu = null }), {}) {}
                    MenuEntries(open.entries, onDismissRequest) { submenu = it }
                }
            }
        }
    }
}
