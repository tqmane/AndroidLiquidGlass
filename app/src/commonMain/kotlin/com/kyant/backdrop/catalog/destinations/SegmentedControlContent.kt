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

import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun SegmentedControlContent() {
    IosPage("Segmented control") { backdrop ->
        var two by remember { mutableIntStateOf(0) }
        var three by remember { mutableIntStateOf(0) }
        var four by remember { mutableIntStateOf(0) }
        var symbol by remember { mutableIntStateOf(0) }
        var disabled by remember { mutableIntStateOf(0) }
        var byContent by remember { mutableIntStateOf(0) }
        var momentaryCount by remember { mutableIntStateOf(0) }

        SectionLabel("Two segments")
        LiquidSegmentedControl(listOf(SegmentItem("First"), SegmentItem("Second")), two, { two = it }, Modifier.fillMaxWidth(), backdrop = backdrop)
        SectionLabel("Three segments")
        LiquidSegmentedControl(listOf(SegmentItem("First"), SegmentItem("Second"), SegmentItem("Third")), three, { three = it }, Modifier.fillMaxWidth(), backdrop = backdrop)
        SectionLabel("Four segments")
        LiquidSegmentedControl(listOf("One", "Two", "Three", "Four").map { SegmentItem(it) }, four, { four = it }, Modifier.fillMaxWidth(), backdrop = backdrop)
        SectionLabel("Symbol segments")
        LiquidSegmentedControl(
            listOf(
                SegmentItem(image = rememberVectorPainter(GlassIcons.Grid), contentDescription = "Grid"),
                SegmentItem(image = rememberVectorPainter(GlassIcons.List), contentDescription = "List"),
                SegmentItem(image = rememberVectorPainter(GlassIcons.Rows), contentDescription = "Rows"),
            ),
            symbol, { symbol = it }, Modifier.fillMaxWidth(), backdrop = backdrop
        )
        SectionLabel("Disabled segment")
        LiquidSegmentedControl(
            listOf(SegmentItem("First"), SegmentItem("Disabled", enabled = false), SegmentItem("Third")),
            disabled, { disabled = it }, Modifier.fillMaxWidth(), backdrop = backdrop
        )
        SectionLabel("Sized by content")
        LiquidSegmentedControl(
            listOf(SegmentItem("All"), SegmentItem("Favorites"), SegmentItem("Recently added")),
            byContent, { byContent = it }, sizing = SegmentSizing.ByContent, backdrop = backdrop
        )
        SectionLabel("Momentary (tapped $momentaryCount times)")
        LiquidSegmentedControl(
            listOf(SegmentItem("Undo"), SegmentItem("Redo")),
            null, { momentaryCount++ }, Modifier.fillMaxWidth(), momentary = true, backdrop = backdrop
        )
        Spacer(Modifier.height(32.dp))
    }
}
