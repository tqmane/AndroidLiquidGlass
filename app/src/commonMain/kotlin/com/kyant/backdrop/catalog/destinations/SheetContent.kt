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
fun SheetContent() {
    BackdropDemoScaffold { backdrop ->
        var sheet by remember { mutableStateOf(false) }
        var largeSheet by remember { mutableStateOf(false) }
        var mediumOnly by remember { mutableStateOf(false) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DemoButton("Medium / large sheet", backdrop) { sheet = true }
            DemoButton("Medium-only sheet", backdrop) { mediumOnly = true }
            DemoButton("Large sheet", backdrop) { largeSheet = true }
        }
        LiquidSheet(sheet, { sheet = false }, backdrop) { SheetBody(backdrop, "Details") { sheet = false } }
        LiquidSheet(mediumOnly, { mediumOnly = false }, backdrop, detents = listOf(SheetDetent.Medium)) {
            SheetBody(backdrop, "Medium") { mediumOnly = false }
        }
        LiquidSheet(largeSheet, { largeSheet = false }, backdrop, detents = listOf(SheetDetent.Large)) {
            SheetBody(backdrop, "Large") { largeSheet = false }
        }
    }
}

@Composable
private fun ColumnScope.SheetBody(backdrop: LayerBackdrop, title: String, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        LiquidIconButton(GlassIcons.Close, "Close", onClose, backdrop)
        BasicText(title, Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center))
        LiquidIconButton(GlassIcons.Check, "Done", onClose, backdrop, tint = GlassColors.accent())
    }
    Column(
        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(16.dp).fillMaxWidth().clip(RoundedRectangle(26.dp)).background(GlassColors.fill().copy(alpha = 0.3f))
    ) {
        listOf("Recently added", "Downloads", "Favorites", "Shared with you").forEachIndexed { i, label ->
            if (i > 0) Box(Modifier.padding(start = 16.dp).fillMaxWidth().height(1.dp).background(GlassColors.separator()))
            BasicText(label, Modifier.padding(16.dp), style = TextStyle(GlassColors.label(), 17.sp))
        }
    }
}
