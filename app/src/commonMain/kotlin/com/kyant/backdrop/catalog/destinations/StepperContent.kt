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
import glass.app.generated.resources.Res
import glass.app.generated.resources.wallpaper_light

@Composable
fun StepperContent() {
    IosPage("Stepper") { backdrop ->
        var count by remember { mutableIntStateOf(1) }
        var wrapped by remember { mutableIntStateOf(0) }
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicText("Count: $count", Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp))
            LiquidStepper(count, { count = it }, backdrop)
        }
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicText("Wraps (0-5): $wrapped", Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp))
            LiquidStepper(wrapped, { wrapped = it }, backdrop, range = 0..5, wraps = true)
        }
        SectionLabel("On glass")
        BoxWithWallpaper { glass ->
            Row(
                Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth()
                    .liquidGlassPanel(glass, { RoundedRectangle(26.dp) }).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicText("Quantity $count", Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp))
                LiquidStepper(count, { count = it }, glass)
            }
        }
    }
}

/** A wallpaper tile recorded into its own backdrop so glass can be shown inside a plain page. */
@Composable
fun BoxWithWallpaper(height: androidx.compose.ui.unit.Dp = 200.dp, content: @Composable BoxScope.(LayerBackdrop) -> Unit) {
    val backdrop = rememberLayerBackdrop()
    Box(Modifier.fillMaxWidth().height(height).clip(RoundedRectangle(26.dp))) {
        androidx.compose.foundation.Image(
            org.jetbrains.compose.resources.painterResource(Res.drawable.wallpaper_light),
            null,
            Modifier.matchParentSize().layerBackdrop(backdrop),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        content(backdrop)
    }
}
