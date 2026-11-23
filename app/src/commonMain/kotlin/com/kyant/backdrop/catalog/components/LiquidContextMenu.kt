package com.kyant.backdrop.catalog.components

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

        Box(
            Modifier
                .matchParentSize()
                .onGloballyPositioned { origin = it.positionInRoot(); container = it.size }
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
                    .graphicsLayer {
                        val s = lerp(1f, 1.04f, p)
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
