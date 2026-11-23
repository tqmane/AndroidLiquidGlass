package com.kyant.backdrop.catalog.destinations

import com.kyant.backdrop.catalog.utils.LockLandscape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.components.GlassColors
import com.kyant.backdrop.catalog.components.LiquidVideoPlayer
import com.kyant.backdrop.catalog.components.rememberVideoPlayerState
import com.kyant.backdrop.catalog.utils.BackHandler
import kotlin.random.Random

/** iOS 26 video player, inline on a page and fullscreen, over a drawn night-sky clip. */
@Composable
fun VideoPlayerContent() {
    val state = rememberVideoPlayerState(duration = 60f)
    BackHandler(state.fullscreen) { state.fullscreen = false }
    LockLandscape(state.fullscreen)
    if (state.fullscreen) {
        LiquidVideoPlayer(
            state,
            Modifier.fillMaxSize().background(Color.Black),
            captionText = ::caption,
            onClose = { state.fullscreen = false }
        ) { NightSkyFrame() }
        return
    }
    val dark = isSystemInDarkTheme()
    Column(
        Modifier
            .fillMaxSize()
            .background(if (dark) Color.Black else Color.White)
            .safeDrawingPadding()
    ) {
        BasicText(
            "Video",
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold, textAlign = TextAlign.Center)
        )
        LiquidVideoPlayer(
            state,
            Modifier.padding(horizontal = 12.dp).fillMaxWidth().aspectRatio(16f / 9f),
            captionText = ::caption
        ) { NightSkyFrame() }
        BasicText(
            "Night over the mountains",
            Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp),
            style = TextStyle(GlassColors.label(), 22.sp, FontWeight.Bold)
        )
        BasicText(
            "A local sample clip for the Liquid Glass player.",
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = TextStyle(GlassColors.secondaryLabel(), 15.sp)
        )
    }
}

@Composable
private fun NightSkyFrame() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0C1222), Color(0xFF16213A))))
        val random = Random(7)
        repeat(90) {
            drawCircle(
                Color.White.copy(alpha = 0.3f + random.nextFloat() * 0.6f),
                radius = 0.6f + random.nextFloat() * 1.6f,
                center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height * 0.7f)
            )
        }
        fun ridge(baseline: Float, amplitude: Float, seed: Int, color: Color) {
            val r = Random(seed)
            val path = Path().apply {
                moveTo(0f, size.height)
                var x = 0f
                lineTo(0f, size.height * baseline)
                while (x < size.width) {
                    x += size.width / 14f
                    lineTo(x, size.height * (baseline - r.nextFloat() * amplitude))
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color)
        }
        ridge(0.72f, 0.14f, 3, Color(0xFF1E2A44))
        ridge(0.86f, 0.12f, 11, Color(0xFF121A2C))
    }
}

private fun caption(language: String) = if (language == "Japanese") "山の上の夜" else "Night over the mountains"
