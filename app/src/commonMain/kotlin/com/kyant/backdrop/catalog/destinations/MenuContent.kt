package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.fillMaxSize
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
        var folder by remember { mutableStateOf("Downloads") }
        var searchMenu by remember { mutableStateOf(false) }
        var source by remember { mutableStateOf("Library") }
        var field by remember { mutableStateOf("Title") }
        var order by remember { mutableStateOf("Date Added") }
        val teal = if (isGlassDark()) Color(0xFF40C8E0) else Color(0xFF30B0C7)
        val contextState = rememberLiquidContextMenuState()
        val rows = listOf("Movies" to GlassIcons.Folder, "Trip to Kyoto.mp4" to GlassIcons.Film)
        val rowStates = rows.map { rememberLiquidContextMenuState() }
        val rowAnchors = rows.map { rememberLiquidMenuAnchor() }
        var heldRow by remember { mutableStateOf<Int?>(null) }
        val rowCell = if (isGlassDark()) Color(0xFF1C1C1E) else Color.White
        val row: @Composable (Int) -> Unit = { i ->
            Row(Modifier.fillMaxSize().background(rowCell).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassIcon(rows[i].second, if (i == 0) teal else GlassColors.accent(), Modifier.size(24.dp))
                Spacer(Modifier.width(14.dp))
                BasicText(rows[i].first, Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp), maxLines = 1)
                BasicText(if (i == 0) "12 items" else "4:32", style = TextStyle(GlassColors.secondaryLabel(), 15.sp))
            }
        }
        val entries = listOf(
            MenuEntry.Action("Copy", GlassIcons.Copy),
            MenuEntry.Action("Add to Favorites", GlassIcons.Heart, checked = favorite) { favorite = !favorite },
            MenuEntry.Submenu("Sort By", GlassIcons.List, listOf("Date", "Name", "Size").map { MenuEntry.Action(it, checked = sort == it) { sort = it } }),
            MenuEntry.Submenu(
                "Save to", GlassIcons.Folder,
                listOf("iCloud Drive", "Downloads", "Documents").map { MenuEntry.Action(it, GlassIcons.Folder, iconTint = teal) { folder = it } },
                subtitle = folder
            ),
            MenuEntry.Divider,
            MenuEntry.Action("Delete", GlassIcons.Trash, destructive = true),
        )
        val searchEntries = listOf(
            MenuEntry.Section("Content Source"),
            MenuEntry.Action("Library", GlassIcons.Folder, checked = source == "Library", iconTint = teal) { source = "Library" },
            MenuEntry.Action("Downloads", GlassIcons.ArrowDownCircle, checked = source == "Downloads", iconTint = teal) { source = "Downloads" },
            MenuEntry.Section("Search Field"),
            MenuEntry.Action("Title", GlassIcons.Pencil, checked = field == "Title") { field = "Title" },
            MenuEntry.Action("All Fields", GlassIcons.List, checked = field == "All Fields") { field = "All Fields" },
            MenuEntry.Section("Sort By"),
            MenuEntry.Action("Date Added", GlassIcons.Clock, checked = order == "Date Added") { order = "Date Added" },
            MenuEntry.Action("Name", GlassIcons.Book, checked = order == "Name") { order = "Name" },
        )
        val card: @Composable () -> Unit = {
            Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp), verticalArrangement = Arrangement.Bottom) {
                BasicText("Photo", style = TextStyle(Color.Black, 20.sp, FontWeight.SemiBold))
                BasicText("Long-press for context menu", style = TextStyle(Color.Black.copy(0.6f), 13.sp))
            }
        }
        Column(Modifier.align(Alignment.TopCenter).fillMaxSize().verticalScroll(rememberScrollState()).systemBarsPadding().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LiquidIconButton(GlassIcons.Star, "Popover", { popover = true }, backdrop)
                LiquidIconButton(GlassIcons.Search, "Search options", { searchMenu = true }, backdrop)
                LiquidIconButton(GlassIcons.Ellipsis, "More", { menu = true }, backdrop)
            }
            Spacer(Modifier.height(48.dp))
            Box(
                Modifier.align(Alignment.CenterHorizontally).size(220.dp, 160.dp)
                    .liquidContextMenuSource(contextState, onLongPress = { context = true })
                    .clip(RoundedRectangle(24.dp))
            ) { card() }
            Spacer(Modifier.height(24.dp))
            GroupedSection("Remote (long-press a row)", rowCell) {
                rows.indices.forEach { i ->
                    if (i > 0) GroupedSeparator(Modifier.padding(start = 38.dp))
                    Box(
                        Modifier.fillMaxWidth().height(52.dp)
                            .liquidMenuAnchor(rowAnchors[i])
                            .liquidContextMenuSource(rowStates[i], onLongPress = { heldRow = i })
                    ) { row(i) }
                }
            }
            Spacer(Modifier.height(16.dp))
            BasicText("Sort: $sort, favorite: $favorite", Modifier.align(Alignment.CenterHorizontally), style = TextStyle(Color.White, 15.sp))
        }
        LiquidMenu(
            menu, { menu = false }, backdrop, entries, Alignment.TopEnd,
            Modifier.systemBarsPadding(), offset = DpOffset((-16).dp, 16.dp), transformOrigin = TransformOrigin(1f, 0f)
        )
        LiquidMenu(
            searchMenu, { searchMenu = false }, backdrop, searchEntries, Alignment.TopCenter,
            Modifier.systemBarsPadding(), offset = DpOffset(0.dp, 16.dp), transformOrigin = TransformOrigin(0.5f, 0f),
            checkmarkPlacement = CheckmarkPlacement.Gutter
        )
        LiquidPopover(
            popover, { popover = false }, backdrop, Alignment.TopStart,
            Modifier.systemBarsPadding().width(260.dp), offset = DpOffset(16.dp, 68.dp), transformOrigin = TransformOrigin(0f, 0f)
        ) { panel ->
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BasicText("Popover", style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold))
                BasicText("Popovers present arbitrary content in a Liquid Glass panel anchored to their source.", style = TextStyle(GlassColors.secondaryLabel(), 15.sp))
                var on by remember { mutableStateOf(true) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText("Show badges", Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp))
                    LiquidToggle({ on }, { on = it }, panel)
                }
            }
        }
        rows.indices.forEach { i ->
            LiquidAnchoredMenu(
                heldRow == i, { heldRow = null }, backdrop, rowAnchors[i],
                listOf(
                    MenuEntry.Action(if (i == 0) "Open" else "Play", if (i == 0) GlassIcons.Folder else GlassIcons.Play),
                    MenuEntry.Action("Rename", GlassIcons.Pencil),
                    MenuEntry.Action("Download", GlassIcons.ArrowDownCircle),
                    MenuEntry.Divider,
                    MenuEntry.Action("Delete", GlassIcons.Trash, destructive = true),
                ),
                state = rowStates[i]
            ) { row(i) }
        }
        LiquidContextMenu(context, { context = false }, backdrop, contextState.bounds, entries, contextState, card)
    }
}
