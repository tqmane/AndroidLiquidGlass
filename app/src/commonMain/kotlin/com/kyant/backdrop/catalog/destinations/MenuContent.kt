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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.DpOffset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun MenuContent() {
    BackdropDemoScaffold { backdrop ->
        var menu by remember { mutableStateOf(false) }
        var context by remember { mutableStateOf(false) }
        var popover by remember { mutableStateOf(false) }
        var favorite by remember { mutableStateOf(false) }
        var sort by remember { mutableStateOf("Date") }
        var anchor by remember { mutableStateOf(Rect.Zero) }
        val entries = listOf(
            MenuEntry.Action("Copy", GlassIcons.Copy),
            MenuEntry.Action("Add to Favorites", GlassIcons.Heart, checked = favorite) { favorite = !favorite },
            MenuEntry.Submenu("Sort By", GlassIcons.List, listOf("Date", "Name", "Size").map { MenuEntry.Action(it, checked = sort == it) { sort = it } }),
            MenuEntry.Divider,
            MenuEntry.Action("Delete", GlassIcons.Trash, destructive = true),
        )
        val card: @Composable () -> Unit = {
            Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp), verticalArrangement = Arrangement.Bottom) {
                BasicText("Photo", style = TextStyle(Color.Black, 20.sp, FontWeight.SemiBold))
                BasicText("Long-press for context menu", style = TextStyle(Color.Black.copy(0.6f), 13.sp))
            }
        }
        Column(Modifier.align(Alignment.TopCenter).systemBarsPadding().padding(16.dp).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LiquidIconButton(GlassIcons.Star, "Popover", { popover = true }, backdrop)
                LiquidIconButton(GlassIcons.Ellipsis, "More", { menu = true }, backdrop)
            }
            Spacer(Modifier.height(48.dp))
            Box(
                Modifier.align(Alignment.CenterHorizontally).size(220.dp, 160.dp)
                    .contextMenuAnchor { anchor = it }
                    .clip(RoundedRectangle(24.dp))
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { context = true }) }
            ) { card() }
            Spacer(Modifier.height(16.dp))
            BasicText("Sort: $sort, favorite: $favorite", Modifier.align(Alignment.CenterHorizontally), style = TextStyle(Color.White, 15.sp))
        }
        LiquidMenu(
            menu, { menu = false }, backdrop, entries, Alignment.TopEnd,
            Modifier.systemBarsPadding(), offset = DpOffset((-16).dp, 16.dp), transformOrigin = TransformOrigin(1f, 0f)
        )
        LiquidPopover(
            popover, { popover = false }, backdrop, Alignment.TopStart,
            Modifier.systemBarsPadding().width(260.dp), offset = DpOffset(16.dp, 68.dp), transformOrigin = TransformOrigin(0f, 0f)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BasicText("Popover", style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold))
                BasicText("Popovers present arbitrary content in a Liquid Glass panel anchored to their source.", style = TextStyle(GlassColors.secondaryLabel(), 15.sp))
                var on by remember { mutableStateOf(true) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText("Show badges", Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp))
                    LiquidToggle({ on }, { on = it }, backdrop)
                }
            }
        }
        LiquidContextMenu(context, { context = false }, backdrop, anchor, entries, card)
    }
}
