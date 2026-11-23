package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.background
import com.kyant.backdrop.catalog.components.LiquidSplitView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.components.GlassColors
import com.kyant.backdrop.catalog.components.GlassIcon
import com.kyant.backdrop.catalog.components.GlassIcons
import com.kyant.backdrop.catalog.components.LiquidAdaptiveTabScaffold
import com.kyant.backdrop.catalog.components.LiquidSidebarSection
import com.kyant.backdrop.catalog.components.LiquidTabItem
import com.kyant.backdrop.catalog.components.isGlassDark
import com.kyant.shapes.RoundedRectangle

private val Swatches = listOf(
    Color(0xFFFF6B6B) to Color(0xFFFFB86B), Color(0xFF4DABF7) to Color(0xFF845EF7),
    Color(0xFF51CF66) to Color(0xFF20C997), Color(0xFFFCC419) to Color(0xFFFF922B),
    Color(0xFFF06595) to Color(0xFFCC5DE8), Color(0xFF339AF0) to Color(0xFF22B8CF),
)

private val Tabs = listOf(
    LiquidTabItem("home", "Home", GlassIcons.House),
    LiquidTabItem("saved", "Saved", GlassIcons.Bookmark),
    LiquidTabItem("library", "Library", GlassIcons.Book),
    LiquidTabItem("search", "Search", GlassIcons.Search, search = true),
)

private val Collections = LiquidSidebarSection(
    "Collections",
    listOf(
        LiquidTabItem("weekend", "Weekend Plans", GlassIcons.Folder),
        LiquidTabItem("travel", "Travel Ideas", GlassIcons.Airplane),
    )
)

private val TabSections = listOf(LiquidSidebarSection(null, Tabs), Collections)
private val SplitSections = listOf(LiquidSidebarSection("Browse", Tabs.filter { !it.search }), Collections)

private val Recent = listOf(
    "Night over the mountains" to GlassIcons.Film,
    "Rainy day favorites" to GlassIcons.Play,
    "Out on the coast" to GlassIcons.Grid,
)
private val LibraryRows = listOf("Downloads" to GlassIcons.ArrowDownCircle, "Favorites" to GlassIcons.Star)

@Composable
fun TabsSidebarContent() {
    var selected by rememberSaveable { mutableStateOf("home") }
    var split by rememberSaveable { mutableStateOf(false) }
    val background = if (isGlassDark()) Color.Black else Color(0xFFF2F2F7)
    val page: @Composable (PaddingValues) -> Unit = { padding ->
        DemoPage(TabSections.flatMap { it.items }.first { it.key == selected }, split, { split = it }, padding)
    }
    if (split) {
        LiquidSplitView(SplitSections, selected, { selected = it }, "Library", Modifier.background(background), content = page)
    } else {
        LiquidAdaptiveTabScaffold(
            tabs = Tabs,
            selectedKey = selected,
            onSelect = { selected = it },
            sidebarSections = TabSections,
            modifier = Modifier.background(background),
            content = page
        )
    }
}

@Composable
private fun DemoPage(current: LiquidTabItem, split: Boolean, onSplit: (Boolean) -> Unit, padding: PaddingValues) {
    val background = if (isGlassDark()) Color.Black else Color(0xFFF2F2F7)
    Column(
        Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicText(current.title, style = TextStyle(GlassColors.label(), 34.sp, FontWeight.Bold))
        Group(if (current.key == "home") "Recently added" else "In this collection", Recent.map { Row3(it.first, it.second) })
        Group("Library", LibraryRows.map { Row3(it.first, it.second) })
        Group(
            "Layout",
            listOf(
                Row3("Tab view (sidebar adaptable)", GlassIcons.Rows, checked = !split) { onSplit(false) },
                Row3("Split view (two columns)", GlassIcons.Grid, checked = split) { onSplit(true) },
            )
        )
        repeat(3) { row ->
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { col ->
                    val (a, b) = Swatches[(row * 3 + col) % Swatches.size]
                    Box(
                        Modifier.weight(1f).aspectRatio(1.4f).clip(RoundedRectangle(20.dp))
                            .background(Brush.linearGradient(listOf(a, b)))
                    )
                }
            }
        }
    }
}

private class Row3(val title: String, val icon: ImageVector, val checked: Boolean = false, val onClick: (() -> Unit)? = null)

@Composable
private fun Group(title: String, rows: List<Row3>) {
    val cell = if (isGlassDark()) Color(0xFF1C1C1E) else Color.White
    BasicText(
        title,
        Modifier.padding(start = 16.dp, top = 8.dp),
        style = TextStyle(GlassColors.secondaryLabel(), 15.sp, FontWeight.Medium)
    )
    Column(Modifier.fillMaxWidth().clip(RoundedRectangle(26.dp)).background(cell)) {
        rows.forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .then(if (row.onClick != null) Modifier.clickable(onClick = row.onClick) else Modifier)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIcon(row.icon, GlassColors.accent(), Modifier.size(22.dp))
                BasicText(row.title, Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp))
                if (row.checked) GlassIcon(GlassIcons.Check, GlassColors.accent(), Modifier.size(18.dp))
            }
        }
    }
}
