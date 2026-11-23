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
fun SearchContent() {
    BackdropDemoScaffold { backdrop ->
        var query by remember { mutableStateOf("") }
        var inline by remember { mutableStateOf("") }
        val items = listOf("Recently added", "Downloads", "Favorites", "Playlists", "Artists", "Albums")
        Column(Modifier.align(Alignment.TopCenter).systemBarsPadding().padding(16.dp).fillMaxWidth()) {
            BasicText("Search", Modifier.padding(top = 16.dp, bottom = 12.dp), style = TextStyle(Color.White, 34.sp, FontWeight.Bold))
            Column(Modifier.fillMaxWidth().liquidGlassPanel(backdrop, { RoundedRectangle(26.dp) }).padding(12.dp)) {
                SearchFieldInline(inline, { inline = it }, Modifier.fillMaxWidth())
                items.filter { it.contains(query, ignoreCase = true) && it.contains(inline, ignoreCase = true) }.forEach {
                    BasicText(it, Modifier.padding(horizontal = 4.dp, vertical = 12.dp), style = TextStyle(GlassColors.label(), 17.sp))
                }
            }
        }
        LiquidSearchBar(
            query, { query = it }, backdrop,
            Modifier.align(Alignment.BottomCenter).imePadding().systemBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
        )
    }
}
