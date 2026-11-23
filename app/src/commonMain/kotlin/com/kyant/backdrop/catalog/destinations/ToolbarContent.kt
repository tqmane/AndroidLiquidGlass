package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.widthIn
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
fun ToolbarContent() {
    BackdropDemoScaffold { backdrop ->
        var selectedTab by remember { mutableIntStateOf(0) }
        Row(
            Modifier.align(Alignment.TopCenter).systemBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidIconButton(GlassIcons.ChevronLeft, "Back", {}, backdrop)
            BasicText("Library", Modifier.weight(1f), style = TextStyle(Color.White, 17.sp, FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center))
            LiquidButtonGroup(backdrop) {
                LiquidGroupItem(GlassIcons.Share, "Share", {})
                LiquidGroupItem(GlassIcons.Plus, "Add", {})
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).systemBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).widthIn(max = 560.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BasicText("Bottom toolbar", style = TextStyle(Color.White, 13.sp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidButtonGroup(backdrop) {
                    LiquidGroupItem(GlassIcons.Heart, "Favorite", {})
                    LiquidGroupItem(GlassIcons.Share, "Share", {})
                    LiquidGroupItem(GlassIcons.Trash, "Delete", {})
                }
                Spacer(Modifier.weight(1f))
                LiquidIconButton(GlassIcons.Pencil, "Compose", {}, backdrop, tint = GlassColors.accent())
            }
            BasicText("Tab bar with search", style = TextStyle(Color.White, 13.sp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidSegmentedTabs({ selectedTab }, { selectedTab = it }, backdrop, Modifier.weight(1f))
                LiquidIconButton(GlassIcons.Search, "Search", {}, backdrop, size = 62.dp, iconSize = 24.dp)
            }
            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable
private fun LiquidSegmentedTabs(selected: () -> Int, onSelect: (Int) -> Unit, backdrop: LayerBackdrop, modifier: Modifier) {
    LiquidBottomTabs(
        selectedTabIndex = selected,
        onTabSelected = onSelect,
        backdrop = backdrop,
        tabsCount = 3,
        modifier = modifier
    ) {
        listOf(GlassIcons.Play to "Home", GlassIcons.Grid to "Browse", GlassIcons.Heart to "Library").forEachIndexed { index, (icon, label) ->
            LiquidBottomTab({ onSelect(index) }) {
                GlassIcon(icon, Color.Black, Modifier.size(24.dp))
                BasicText(label, style = TextStyle(Color.Black, 10.sp, FontWeight.Medium))
            }
        }
    }
}
