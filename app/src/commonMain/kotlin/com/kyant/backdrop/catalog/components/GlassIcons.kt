package com.kyant.backdrop.catalog.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object GlassIcons {
    private fun stroke(name: String, width: Float = 2f, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block
            )
        }.build()

    private fun fill(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black), pathBuilder = block)
        }.build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx + r, y1 = cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx - r, y1 = cy)
        close()
    }

    val Plus = stroke("plus") { moveTo(12f, 5f); lineTo(12f, 19f); moveTo(5f, 12f); lineTo(19f, 12f) }
    val Minus = stroke("minus") { moveTo(5f, 12f); lineTo(19f, 12f) }
    val Close = stroke("xmark") { moveTo(6.5f, 6.5f); lineTo(17.5f, 17.5f); moveTo(17.5f, 6.5f); lineTo(6.5f, 17.5f) }
    val Check = stroke("checkmark", 2.4f) { moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 6.5f) }
    val ChevronLeft = stroke("chevron.left", 2.4f) { moveTo(15f, 4.5f); lineTo(7.5f, 12f); lineTo(15f, 19.5f) }
    val ChevronRight = stroke("chevron.right", 2f) { moveTo(9f, 4.5f); lineTo(16.5f, 12f); lineTo(9f, 19.5f) }
    val ChevronDown = stroke("chevron.down", 2f) { moveTo(5f, 9f); lineTo(12f, 16f); lineTo(19f, 9f) }
    val Search = stroke("magnifyingglass", 2.2f) { circle(10.5f, 10.5f, 6f); moveTo(15f, 15f); lineTo(20f, 20f) }
    val Mic = stroke("mic", 2f) {
        moveTo(9f, 6f); arcTo(3f, 3f, 0f, false, true, 15f, 6f); lineTo(15f, 11f)
        arcTo(3f, 3f, 0f, false, true, 9f, 11f); close()
        moveTo(6f, 11f); arcTo(6f, 6f, 0f, false, false, 18f, 11f); moveTo(12f, 17f); lineTo(12f, 20f)
    }
    val Ellipsis = fill("ellipsis") { circle(5f, 12f, 2f); circle(12f, 12f, 2f); circle(19f, 12f, 2f) }
    val Share = stroke("square.and.arrow.up") {
        moveTo(12f, 3f); lineTo(12f, 15f); moveTo(8f, 7f); lineTo(12f, 3f); lineTo(16f, 7f)
        moveTo(8.5f, 10f); lineTo(6f, 10f); lineTo(6f, 21f); lineTo(18f, 21f); lineTo(18f, 10f); lineTo(15.5f, 10f)
    }
    val Trash = stroke("trash") {
        moveTo(4f, 6f); lineTo(20f, 6f); moveTo(9f, 6f); lineTo(9f, 3.5f); lineTo(15f, 3.5f); lineTo(15f, 6f)
        moveTo(6f, 6f); lineTo(7f, 20.5f); lineTo(17f, 20.5f); lineTo(18f, 6f)
    }
    val Pencil = stroke("pencil") { moveTo(4f, 20f); lineTo(5f, 15.5f); lineTo(16f, 4.5f); lineTo(19.5f, 8f); lineTo(8.5f, 19f); close() }
    val Copy = stroke("doc.on.doc") {
        moveTo(8f, 8f); lineTo(19f, 8f); lineTo(19f, 21f); lineTo(8f, 21f); close()
        moveTo(5f, 16f); lineTo(5f, 3f); lineTo(15f, 3f)
    }
    val Heart = stroke("heart") {
        moveTo(12f, 20f); lineTo(4.5f, 12.5f); arcTo(4.2f, 4.2f, 0f, false, true, 12f, 6.5f)
        arcTo(4.2f, 4.2f, 0f, false, true, 19.5f, 12.5f); close()
    }
    val Star = stroke("star") {
        moveTo(12f, 3f); lineTo(14.6f, 8.6f); lineTo(20.6f, 9.3f); lineTo(16.1f, 13.4f); lineTo(17.3f, 19.4f)
        lineTo(12f, 16.4f); lineTo(6.7f, 19.4f); lineTo(7.9f, 13.4f); lineTo(3.4f, 9.3f); lineTo(9.4f, 8.6f); close()
    }
    val Grid = stroke("square.grid.2x2") {
        moveTo(4f, 4f); lineTo(10f, 4f); lineTo(10f, 10f); lineTo(4f, 10f); close()
        moveTo(14f, 4f); lineTo(20f, 4f); lineTo(20f, 10f); lineTo(14f, 10f); close()
        moveTo(4f, 14f); lineTo(10f, 14f); lineTo(10f, 20f); lineTo(4f, 20f); close()
        moveTo(14f, 14f); lineTo(20f, 14f); lineTo(20f, 20f); lineTo(14f, 20f); close()
    }
    val List = stroke("list.bullet") {
        moveTo(9f, 6f); lineTo(20f, 6f); moveTo(9f, 12f); lineTo(20f, 12f); moveTo(9f, 18f); lineTo(20f, 18f)
        moveTo(4.5f, 6f); lineTo(4.6f, 6f); moveTo(4.5f, 12f); lineTo(4.6f, 12f); moveTo(4.5f, 18f); lineTo(4.6f, 18f)
    }
    val Rows = stroke("rectangle.split.1x2") {
        moveTo(4f, 4f); lineTo(20f, 4f); lineTo(20f, 11f); lineTo(4f, 11f); close()
        moveTo(4f, 13f); lineTo(20f, 13f); lineTo(20f, 20f); lineTo(4f, 20f); close()
    }
    val Gear = stroke("gearshape") { circle(12f, 12f, 3f); circle(12f, 12f, 8f) }
    val Play = fill("play.fill") { moveTo(7f, 4f); lineTo(20f, 12f); lineTo(7f, 20f); close() }
}
