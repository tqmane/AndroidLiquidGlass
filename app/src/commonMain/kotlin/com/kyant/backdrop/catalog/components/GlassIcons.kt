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

    private fun mixed(name: String, width: Float = 2f, fillBlock: PathBuilder.() -> Unit, strokeBlock: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black), pathBuilder = fillBlock)
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = strokeBlock
            )
        }.build()

    private fun PathBuilder.speaker() {
        moveTo(3f, 9f); lineTo(7f, 9f); lineTo(12f, 4.5f); lineTo(12f, 19.5f); lineTo(7f, 15f); lineTo(3f, 15f); close()
    }

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
    val Clock = stroke("clock") { circle(12f, 12f, 9f); moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15.5f, 14f) }
    val ArrowDownCircle = stroke("arrow.down.circle") {
        circle(12f, 12f, 9f); moveTo(12f, 7.5f); lineTo(12f, 16f); moveTo(8.5f, 12.5f); lineTo(12f, 16f); lineTo(15.5f, 12.5f)
    }
    val SidebarLeft = stroke("sidebar.left", 1.8f) {
        moveTo(5f, 4.5f); lineTo(19f, 4.5f); curveTo(20.4f, 4.5f, 21.5f, 5.6f, 21.5f, 7f); lineTo(21.5f, 17f)
        curveTo(21.5f, 18.4f, 20.4f, 19.5f, 19f, 19.5f); lineTo(5f, 19.5f); curveTo(3.6f, 19.5f, 2.5f, 18.4f, 2.5f, 17f)
        lineTo(2.5f, 7f); curveTo(2.5f, 5.6f, 3.6f, 4.5f, 5f, 4.5f); close()
        moveTo(9.5f, 4.5f); lineTo(9.5f, 19.5f)
        moveTo(5f, 8f); lineTo(7f, 8f); moveTo(5f, 11f); lineTo(7f, 11f)
    }
    val House = stroke("house", 1.8f) {
        moveTo(3f, 11f); lineTo(12f, 3.5f); lineTo(21f, 11f)
        moveTo(5.5f, 9f); lineTo(5.5f, 20f); lineTo(18.5f, 20f); lineTo(18.5f, 9f)
        moveTo(10f, 20f); lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 20f)
    }
    val Folder = stroke("folder", 1.8f) {
        moveTo(3f, 6f); lineTo(9.5f, 6f); lineTo(11.5f, 8.5f); lineTo(21f, 8.5f); lineTo(21f, 18.5f); lineTo(3f, 18.5f); close()
        moveTo(3f, 10.5f); lineTo(21f, 10.5f)
    }
    val Airplane = fill("airplane") {
        moveTo(21.5f, 12f); lineTo(17f, 10.6f); lineTo(11f, 10.6f); lineTo(7f, 3.5f); lineTo(5f, 3.5f); lineTo(7.8f, 10.6f)
        lineTo(4.5f, 10.6f); lineTo(3f, 8.5f); lineTo(1.8f, 8.5f); lineTo(2.6f, 12f); lineTo(1.8f, 15.5f); lineTo(3f, 15.5f)
        lineTo(4.5f, 13.4f); lineTo(7.8f, 13.4f); lineTo(5f, 20.5f); lineTo(7f, 20.5f); lineTo(11f, 13.4f); lineTo(17f, 13.4f); close()
    }
    val Book = stroke("book", 1.8f) {
        moveTo(12f, 6.5f); curveTo(9.5f, 4.8f, 5.5f, 4.8f, 3f, 5.5f); lineTo(3f, 18.5f); curveTo(5.5f, 17.8f, 9.5f, 17.8f, 12f, 19.5f)
        curveTo(14.5f, 17.8f, 18.5f, 17.8f, 21f, 18.5f); lineTo(21f, 5.5f); curveTo(18.5f, 4.8f, 14.5f, 4.8f, 12f, 6.5f); lineTo(12f, 19.5f)
    }
    val Pause = fill("pause.fill") {
        moveTo(6f, 4f); lineTo(10f, 4f); lineTo(10f, 20f); lineTo(6f, 20f); close()
        moveTo(14f, 4f); lineTo(18f, 4f); lineTo(18f, 20f); lineTo(14f, 20f); close()
    }
    val GoBackward = stroke("gobackward", 2f) {
        moveTo(12f, 3.5f); arcTo(8.5f, 8.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 4.64f, y1 = 7.75f)
        moveTo(14.5f, 1.3f); lineTo(12f, 3.5f); lineTo(14.5f, 5.7f)
    }
    val GoForward = stroke("goforward", 2f) {
        moveTo(12f, 3.5f); arcTo(8.5f, 8.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 19.36f, y1 = 7.75f)
        moveTo(9.5f, 1.3f); lineTo(12f, 3.5f); lineTo(9.5f, 5.7f)
    }
    val Backward = fill("backward.fill") {
        moveTo(11.5f, 6f); lineTo(11.5f, 18f); lineTo(3f, 12f); close()
        moveTo(21f, 6f); lineTo(21f, 18f); lineTo(12.5f, 12f); close()
    }
    val Forward = fill("forward.fill") {
        moveTo(12.5f, 6f); lineTo(12.5f, 18f); lineTo(21f, 12f); close()
        moveTo(3f, 6f); lineTo(3f, 18f); lineTo(11.5f, 12f); close()
    }
    val Speaker = mixed("speaker.wave.2.fill", fillBlock = { speaker() }) {
        moveTo(15.5f, 9f); quadTo(17f, 12f, 15.5f, 15f); moveTo(18f, 6.5f); quadTo(21f, 12f, 18f, 17.5f)
    }
    val SpeakerSlash = mixed("speaker.slash.fill", fillBlock = { speaker() }) {
        moveTo(15f, 9f); lineTo(21f, 15f); moveTo(21f, 9f); lineTo(15f, 15f)
    }
    val PictureInPicture = mixed("pip", 1.8f, fillBlock = {
        moveTo(12f, 12f); lineTo(19f, 12f); lineTo(19f, 17f); lineTo(12f, 17f); close()
    }) { moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close() }
    val AirPlay = mixed("airplayvideo", 1.8f, fillBlock = {
        moveTo(12f, 14f); lineTo(17f, 20f); lineTo(7f, 20f); close()
    }) { moveTo(7f, 17f); lineTo(4f, 17f); lineTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 17f); lineTo(17f, 17f) }
    val Expand = stroke("arrow.up.left.and.arrow.down.right") {
        moveTo(4f, 10f); lineTo(4f, 4f); lineTo(10f, 4f); moveTo(4f, 4f); lineTo(10f, 10f)
        moveTo(20f, 14f); lineTo(20f, 20f); lineTo(14f, 20f); moveTo(20f, 20f); lineTo(14f, 14f)
    }
    val Captions = stroke("captions.bubble", 1.8f) {
        moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 17f); lineTo(10f, 17f); lineTo(6f, 20.5f); lineTo(6f, 17f); lineTo(4f, 17f); close()
        moveTo(7f, 10f); lineTo(11f, 10f); moveTo(13f, 10f); lineTo(17f, 10f); moveTo(7f, 13.5f); lineTo(15f, 13.5f)
    }
    val Gauge = stroke("gauge.with.needle") {
        moveTo(4f, 17f); arcTo(8.5f, 8.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 20f, y1 = 17f)
        moveTo(12f, 16f); lineTo(15.5f, 10f)
    }
    val Waveform = stroke("waveform", 1.8f) {
        moveTo(4f, 10.5f); lineTo(4f, 13.5f); moveTo(7.2f, 8f); lineTo(7.2f, 16f); moveTo(10.4f, 4.5f); lineTo(10.4f, 19.5f)
        moveTo(13.6f, 7f); lineTo(13.6f, 17f); moveTo(16.8f, 9.5f); lineTo(16.8f, 14.5f); moveTo(20f, 11f); lineTo(20f, 13f)
    }
    val SunMax = stroke("sun.max") {
        circle(12f, 12f, 4f)
        moveTo(12f, 2.5f); lineTo(12f, 4.5f); moveTo(12f, 19.5f); lineTo(12f, 21.5f); moveTo(2.5f, 12f); lineTo(4.5f, 12f); moveTo(19.5f, 12f); lineTo(21.5f, 12f)
        moveTo(5.3f, 5.3f); lineTo(6.7f, 6.7f); moveTo(17.3f, 17.3f); lineTo(18.7f, 18.7f); moveTo(5.3f, 18.7f); lineTo(6.7f, 17.3f); moveTo(17.3f, 6.7f); lineTo(18.7f, 5.3f)
    }
}
