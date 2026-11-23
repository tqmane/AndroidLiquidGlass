package com.kyant.backdrop.catalog.destinations

import kotlin.math.abs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.GlassColors
import com.kyant.backdrop.catalog.components.GlassIcons
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidGlassStyle
import com.kyant.backdrop.catalog.components.LiquidIconButton
import com.kyant.backdrop.catalog.components.LiquidSegmentedControl
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.catalog.components.LocalLiquidGlassStyle
import com.kyant.backdrop.catalog.components.SegmentItem
import com.kyant.backdrop.catalog.components.liquidGlassPanel
import com.kyant.shapes.RoundedRectangle

/** Live, app-wide Liquid Glass tuning: presets plus refraction, blur and opacity sliders that every catalog component follows. */
@Composable
fun GlassSettingsContent() {
    val style = LocalLiquidGlassStyle.current
    BackdropDemoScaffold { backdrop ->
        val presets = remember { listOf(LiquidGlassStyle(), LiquidGlassStyle.Clear, LiquidGlassStyle.Tinted) }
        val matched = presets.indexOfFirst {
            abs(it.refraction - style.refraction) < 0.005f &&
                abs(it.blur - style.blur) < 0.005f &&
                abs(it.opacity - style.opacity) < 0.005f
        }
        var lastPreset by remember { mutableIntStateOf(matched.coerceAtLeast(0)) }
        val preset = if (matched >= 0) matched else lastPreset
        var toggle by remember { mutableStateOf(true) }
        var segment by remember { mutableIntStateOf(0) }
        Column(
            Modifier
                .safeDrawingPadding()
                .padding(16.dp)
                .liquidGlassPanel(backdrop, { RoundedRectangle(32.dp) })
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BasicText("Liquid Glass", style = TextStyle(GlassColors.label(), 22.sp, FontWeight.Bold))
            LiquidSegmentedControl(
                listOf(SegmentItem("Default"), SegmentItem("Clear"), SegmentItem("Tinted")),
                preset,
                { index ->
                    lastPreset = index
                    val target = presets[index]
                    style.refraction = target.refraction
                    style.blur = target.blur
                    style.opacity = target.opacity
                },
                Modifier.fillMaxWidth(),
                backdrop = backdrop
            )
            StyleSlider("Refraction", { style.refraction }, { style.refraction = it }, backdrop)
            StyleSlider("Blur", { style.blur }, { style.blur = it }, backdrop)
            StyleSlider("Opacity", { style.opacity }, { style.opacity = it }, backdrop)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidIconButton(GlassIcons.Heart, "Favorite", {}, backdrop)
                LiquidToggle({ toggle }, { toggle = it }, backdrop)
                LiquidButton({ style.reset(); lastPreset = 0 }, backdrop) {
                    BasicText("Reset", style = TextStyle(GlassColors.label(), 15.sp))
                }
            }
            LiquidSegmentedControl(
                listOf(SegmentItem("Day"), SegmentItem("Week"), SegmentItem("Month")),
                segment,
                { segment = it },
                Modifier.fillMaxWidth(),
                backdrop = backdrop
            )
        }
    }
}

@Composable
private fun StyleSlider(label: String, value: () -> Float, onChange: (Float) -> Unit, backdrop: Backdrop) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            BasicText(label, Modifier.weight(1f), style = TextStyle(GlassColors.label(), 15.sp))
            BasicText("${(value() * 100).toInt()}%", style = TextStyle(GlassColors.secondaryLabel(), 15.sp))
        }
        LiquidSlider(value, onChange, 0f..2f, 0.01f, backdrop, Modifier.fillMaxWidth())
    }
}
