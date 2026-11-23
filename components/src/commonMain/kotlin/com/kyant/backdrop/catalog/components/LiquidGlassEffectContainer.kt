package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Color
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
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
        CompositionLocalProvider(LocalLiquidGlassGroup provides group) { content() }
        // Drawn above the members: the neck plus a 1.5 dp band over their facing rims, so the joined glass
        // reads as one blob (as on iOS) with a rim only on its outer outline.
        Box(
            Modifier
                .matchParentSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        // A held member's rim glows wider, so the band over it widens with the glow.
                        val glow = group.members.values.maxOfOrNull { it.glow() } ?: 0f
                        group.bridgeShape(density, with(density) { spacing.toPx() }, with(density) { 1.5.dp.toPx() })
                    },
                    effects = {
                        vibrancy()
                        blur(2.dp.toPx() * glass.blur)
                    },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = {
                        drawGlassFill(Color.Unspecified, Color.Unspecified, isLight, glass.opacity)
                        // Same additive lift a held member gets from InteractiveHighlight, so the joined part does not read darker.
                        val glow = group.members.values.maxOfOrNull { it.glow() } ?: 0f
                        if (glow > 0f) drawRect(Color.White.copy(alpha = 0.2f * glow), blendMode = androidx.compose.ui.graphics.BlendMode.Plus)
                    }
                )
        )
    }
}

/**
 * Joins this capsule-shaped glass surface to the enclosing [LiquidGlassEffectContainer].
 * [transform] maps the laid-out bounds to the drawn ones (press growth, drag offset).
 */
fun Modifier.liquidGlassEffectMember(
    glow: () -> Float = { 0f },
    transform: Density.(Rect) -> Rect = { it },
): Modifier = composed {
    val group = LocalLiquidGlassGroup.current ?: return@composed this
    val key = remember { Any() }
    DisposableEffect(group) { onDispose { group.members.remove(key) } }
    onGloballyPositioned { c ->
        val parent = group.coordinates ?: return@onGloballyPositioned
        if (!parent.isAttached) return@onGloballyPositioned
        val rect = parent.localBoundingBoxOf(c, clipBounds = false)
        val member = group.members[key]
        if (member == null) group.members[key] = GroupMember(rect, transform, glow)
        else {
            member.rect = rect
            member.transform = transform
            member.glow = glow
        }
    }
}

internal val LocalLiquidGlassGroup = compositionLocalOf<LiquidGlassGroup?> { null }

internal class GroupMember(rect: Rect, var transform: Density.(Rect) -> Rect, var glow: () -> Float) {
    var rect by mutableStateOf(rect)
}

@Stable
internal class LiquidGlassGroup {
    var coordinates: LayoutCoordinates? = null
    val members = mutableStateMapOf<Any, GroupMember>()

    fun bridgeShape(density: Density, spacing: Float, rimInset: Float): Shape {
        val rects = members.values.map { with(it) { density.transform(rect) } }.sortedBy { it.left }
        val neck = Path()
        for (i in rects.indices) for (j in i + 1 until rects.size) {
            addBridge(neck, rects[i], rects[j], spacing)
        }
        // Where a held member has grown into its neighbour, the lens they share is covered too, so neither
        // member's rim shows inside the other as a short arc: on iOS only the joined outline has a rim.
        val lenses = Path()
        for (i in rects.indices) for (j in i + 1 until rects.size) {
            val a = rects[i]
            val b = rects[j]
            if (!a.overlaps(b)) continue
            lenses.addPath(
                Path.combine(
                    PathOperation.Intersect,
                    Path().apply { addRoundRect(RoundRect(a.inflate(rimInset), CornerRadius(a.height / 2f + rimInset))) },
                    Path().apply { addRoundRect(RoundRect(b.inflate(rimInset), CornerRadius(b.height / 2f + rimInset))) }
                )
            )
        }
        if (neck.isEmpty && lenses.isEmpty) return PathShape(neck)
        val inner = Path()
        for (r in rects) {
            val d = r.deflate(rimInset)
            if (d.width > 0f && d.height > 0f) inner.addRoundRect(RoundRect(d, CornerRadius(d.height / 2f)))
        }
        val joined = Path.combine(PathOperation.Difference, neck, inner)
        return PathShape(if (lenses.isEmpty) joined else Path.combine(PathOperation.Union, joined, lenses))
    }
}

/**
 * Adds a metaball neck between the facing end caps of two horizontally adjacent capsules [a] (left) and
 * [b] (right). The neck leaves each cap along its tangent, so the outline stays smooth (no horns), and
 * its spread grows with proximity: zero at [spacing], a full liquid waist once the caps touch.
 */
private fun addBridge(path: Path, a: Rect, b: Rect, spacing: Float) {
    val gap = b.left - a.right
    if (spacing <= 0f || gap >= spacing) return
    if (min(a.bottom, b.bottom) - max(a.top, b.top) < min(a.height, b.height) * 0.5f) return
    val t = ((spacing - gap) / spacing).coerceIn(0f, 1f)
    val r1 = a.height / 2f
    val r2 = b.height / 2f
    val c1 = Offset(a.right - r1, a.center.y)
    val c2 = Offset(b.left + r2, b.center.y)
    val d = (c2 - c1).getDistance()
    if (d <= abs(r1 - r2) + 0.5f) return
    val v = 0.42f * kotlin.math.sqrt(t)
    if (v < 0.02f) return
    val u1 = if (d < r1 + r2) acos(((r1 * r1 + d * d - r2 * r2) / (2 * r1 * d)).coerceIn(-1f, 1f)) else 0f
    val u2 = if (d < r1 + r2) acos(((r2 * r2 + d * d - r1 * r1) / (2 * r2 * d)).coerceIn(-1f, 1f)) else 0f
    val between = atan2(c2.y - c1.y, c2.x - c1.x)
    val maxSpread = acos(((r1 - r2) / d).coerceIn(-1f, 1f))
    val a1 = between + u1 + (maxSpread - u1) * v
    val a2 = between - u1 - (maxSpread - u1) * v
    val a3 = between + PI.toFloat() - u2 - (PI.toFloat() - u2 - maxSpread) * v
    val a4 = between - PI.toFloat() + u2 + (PI.toFloat() - u2 - maxSpread) * v
    fun dir(angle: Float) = Offset(cos(angle), sin(angle))
    val p1 = c1 + dir(a1) * r1
    val p2 = c1 + dir(a2) * r1
    val p3 = c2 + dir(a3) * r2
    val p4 = c2 + dir(a4) * r2
    val handle = min(v * 2f, (p1 - p3).getDistance() / (r1 + r2)) * min(1f, d * 2f / (r1 + r2))
    val h1 = p1 + dir(a1 - PI.toFloat() / 2f) * (r1 * handle)
    val h2 = p2 + dir(a2 + PI.toFloat() / 2f) * (r1 * handle)
    val h3 = p3 + dir(a3 + PI.toFloat() / 2f) * (r2 * handle)
    val h4 = p4 + dir(a4 - PI.toFloat() / 2f) * (r2 * handle)
    path.moveTo(p1.x, p1.y)
    path.cubicTo(h1.x, h1.y, h3.x, h3.y, p3.x, p3.y)
    path.lineTo(p4.x, p4.y)
    path.cubicTo(h4.x, h4.y, h2.x, h2.y, p2.x, p2.y)
    path.close()
}

/** A fresh instance per geometry, so the backdrop's outline cache is refreshed whenever members move. */
private class PathShape(private val path: Path) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline = Outline.Generic(path)
}
