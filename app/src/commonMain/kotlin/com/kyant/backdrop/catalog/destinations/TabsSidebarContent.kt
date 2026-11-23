package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.background
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

private val Tabs = listOf(
    LiquidTabItem("home", "Home", GlassIcons.House),
    LiquidTabItem("saved", "Saved", GlassIcons.Heart),
    LiquidTabItem("library", "Library", GlassIcons.Book),
    LiquidTabItem("search", "Search", GlassIcons.Search, search = true),
)

private val Sections = listOf(
    LiquidSidebarSection(null, Tabs),
    LiquidSidebarSection(
        "Collections",
        listOf(
            LiquidTabItem("recent", "Recently added", GlassIcons.Clock),
            LiquidTabItem("downloads", "Downloads", GlassIcons.ArrowDownCircle),
        )
    ),
)

private val Swatches = listOf(
    Color(0xFFFF6B6B) to Color(0xFFFFB86B), Color(0xFF4DABF7) to Color(0xFF845EF7),
    Color(0xFF51CF66) to Color(0xFF20C997), Color(0xFFFCC419) to Color(0xFFFF922B),
    Color(0xFFF06595) to Color(0xFFCC5DE8), Color(0xFF339AF0) to Color(0xFF22B8CF),
)

@Composable
fun TabsSidebarContent() {
    var selected by rememberSaveable { mutableStateOf("home") }
    val all = Sections.flatMap { it.items }
    val current = all.first { it.key == selected }
    val background = if (isGlassDark()) Color.Black else Color(0xFFF2F2F7)
    val cell = if (isGlassDark()) Color(0xFF1C1C1E) else Color.White

    LiquidAdaptiveTabScaffold(
        tabs = Tabs,
        selectedKey = selected,
        onSelect = { selected = it },
        sidebarSections = Sections,
        sidebarTitle = "Catalog",
        modifier = Modifier.background(background)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicText(current.title, style = TextStyle(GlassColors.label(), 34.sp, FontWeight.Bold))
            BasicText(
                "Library",
                Modifier.padding(start = 16.dp, top = 4.dp),
                style = TextStyle(GlassColors.secondaryLabel(), 15.sp)
            )
            Column(Modifier.fillMaxWidth().clip(RoundedRectangle(26.dp)).background(cell)) {
                Sections[1].items.plus(LiquidTabItem("fav", "Favorites", GlassIcons.Heart)).forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIcon(item.icon, GlassColors.accent(), Modifier.size(22.dp))
                        BasicText(item.title, style = TextStyle(GlassColors.label(), 17.sp))
                    }
                }
            }
            repeat(4) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
}
