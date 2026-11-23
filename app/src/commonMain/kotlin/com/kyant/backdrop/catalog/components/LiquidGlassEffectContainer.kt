package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * SwiftUI `GlassEffectContainer(spacing:)`: glass members closer than [spacing] are joined by a liquid
 * neck that thickens as they approach, so a circle that grows while held (search circle, × beside the
 * search field) visibly sticks to its neighbour, and members placed within [spacing] bridge at rest.
 *
 * [LiquidGlassContainer] / [LiquidIconButton] join automatically; mark any other capsule glass surface
 * (a tab bar, a search field) with [liquidGlassEffectMember]. Members must use the same [backdrop].
 */
@Composable
fun LiquidGlassEffectContainer(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    spacing: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val group = remember { LiquidGlassGroup() }
    val glass = LocalLiquidGlassStyle.current
    val isLight = !isGlassDark()
    val density = LocalDensity.current
    Box(modifier.onPlaced { group.coordinates = it }) {
        Box(
            Modifier
                .matchParentSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { group.bridgeShape(density, with(density) { spacing.toPx() }) },
                    effects = {
                        vibrancy()
                        blur(2.dp.toPx() * glass.blur)
                    },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = { drawRect(glassSurfaceFill(androidx.compose.ui.graphics.Color.Unspecified, androidx.compose.ui.graphics.Color.Unspecified, isLight, glass.opacity)) }
                )
        )
        CompositionLocalProvider(LocalLiquidGlassGroup provides group) { content() }
    }
}

/**
 * Joins this capsule-shaped glass surface to the enclosing [LiquidGlassEffectContainer].
 * [transform] maps the laid-out bounds to the drawn ones (press growth, drag offset).
 */
fun Modifier.liquidGlassEffectMember(transform: Density.(Rect) -> Rect = { it }): Modifier = composed {
    val group = LocalLiquidGlassGroup.current ?: return@composed this
    val key = remember { Any() }
    DisposableEffect(group) { onDispose { group.members.remove(key) } }
    onGloballyPositioned { c ->
        val parent = group.coordinates ?: return@onGloballyPositioned
        if (!parent.isAttached) return@onGloballyPositioned
        val rect = parent.localBoundingBoxOf(c, clipBounds = false)
        val member = group.members[key]
        if (member == null) group.members[key] = GroupMember(rect, transform)
        else {
            member.rect = rect
            member.transform = transform
        }
    }
}

internal val LocalLiquidGlassGroup = compositionLocalOf<LiquidGlassGroup?> { null }

internal class GroupMember(rect: Rect, var transform: Density.(Rect) -> Rect) {
    var rect by mutableStateOf(rect)
}

@Stable
internal class LiquidGlassGroup {
    var coordinates: LayoutCoordinates? = null
    val members = mutableStateMapOf<Any, GroupMember>()

    fun bridgeShape(density: Density, spacing: Float): Shape {
        val rects = members.values.map { with(it) { density.transform(rect) } }.sortedBy { it.left }
        val path = Path()
        for (i in rects.indices) for (j in i + 1 until rects.size) {
            addBridge(path, rects[i], rects[j], spacing)
        }
        return PathShape(path)
    }
}

/** Adds the neck between two horizontally adjacent capsules [a] (left) and [b] (right). */
private fun addBridge(path: Path, a: Rect, b: Rect, spacing: Float) {
    val gap = b.left - a.right
    if (spacing <= 0f || gap >= spacing) return
    if (min(a.bottom, b.bottom) - max(a.top, b.top) < min(a.height, b.height) * 0.5f) return
    val t = ((spacing - gap) / spacing).coerceIn(0f, 1f)
    val ra = a.height / 2f
    val rb = b.height / 2f
    val r = min(ra, rb)
    // Neck half-thickness: ~0.2 r for the native 8 pt gap in a 20 pt container, 0.6 r once touching.
    val neck = r * 0.6f * t * t
    if (neck < 0.5f) return
    val ca = Offset(a.right - ra, a.center.y)
    val cb = Offset(b.left + rb, b.center.y)
    val phi = 0.95f
    val pa = Offset(ca.x + ra * cos(phi), ca.y - ra * sin(phi))
    val pb = Offset(cb.x - rb * cos(phi), cb.y - rb * sin(phi))
    val mid = Offset((a.right + b.left) / 2f, (ca.y + cb.y) / 2f)
    val k = max(gap, 0f) / 2f + r * 0.35f
    // Tangent of a circle at angle phi, heading towards the gap.
    val ta = Offset(sin(phi), cos(phi))
    path.moveTo(pa.x, pa.y)
    path.cubicTo(pa.x + ta.x * k, pa.y + ta.y * k, mid.x - k, mid.y - neck, mid.x, mid.y - neck)
    path.cubicTo(mid.x + k, mid.y - neck, pb.x - ta.x * k, pb.y + ta.y * k, pb.x, pb.y)
    path.lineTo(pb.x, cb.y + (cb.y - pb.y))
    val pb2 = Offset(pb.x, cb.y + (cb.y - pb.y))
    val pa2 = Offset(pa.x, ca.y + (ca.y - pa.y))
    path.cubicTo(pb2.x - ta.x * k, pb2.y - ta.y * k, mid.x + k, mid.y + neck, mid.x, mid.y + neck)
    path.cubicTo(mid.x - k, mid.y + neck, pa2.x + ta.x * k, pa2.y - ta.y * k, pa2.x, pa2.y)
    path.close()
}

/** A fresh instance per geometry, so the backdrop's outline cache is refreshed whenever members move. */
private class PathShape(private val path: Path) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline = Outline.Generic(path)
}
