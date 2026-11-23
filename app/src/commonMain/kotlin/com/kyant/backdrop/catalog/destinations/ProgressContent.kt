package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.*
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
@Composable
fun ProgressContent() {
    IosPage("Progress") { backdrop ->
        var progress by remember { mutableFloatStateOf(0.35f) }
        SectionLabel("Progress view")
        LiquidProgressView(progress)
        Spacer(Modifier.height(16.dp))
        LiquidSlider({ progress }, { progress = it }, 0f..1f, 0.001f, backdrop)
        SectionLabel("Activity indicator")
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
            LiquidActivityIndicator()
            LiquidActivityIndicator(size = ActivityIndicatorSize.Large)
            LiquidActivityIndicator(color = GlassColors.accent())
        }
        SectionLabel("Circular gauge")
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            LiquidCircularGauge(progress)
            LiquidCircularGauge(0.65f, size = 56.dp, color = GlassColors.accent())
        }
        SectionLabel("On glass")
        BoxWithWallpaper { glass ->
            Column(
                Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth()
                    .liquidGlassPanel(glass, { RoundedRectangle(26.dp) }).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LiquidActivityIndicator(size = ActivityIndicatorSize.Large)
                BasicText("Downloading...", style = TextStyle(GlassColors.label(), 15.sp))
                LiquidProgressView(progress)
            }
        }
    }
}
