package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.components.GlassColors
import com.kyant.backdrop.catalog.components.LiquidAdaptiveTabScaffold
import com.kyant.backdrop.catalog.components.LiquidBorderedButton
import com.kyant.backdrop.catalog.components.LiquidMenu
import com.kyant.backdrop.catalog.components.LiquidMenuIcons
import com.kyant.backdrop.catalog.components.LiquidSearchBar
import com.kyant.backdrop.catalog.components.LiquidSearchBarIcons
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidStepper
import com.kyant.backdrop.catalog.components.LiquidTabItem
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.catalog.components.LiquidVideoPlayer
import com.kyant.backdrop.catalog.components.LiquidVideoPlayerIcons
import com.kyant.backdrop.catalog.components.LocalGlassAccent
import com.kyant.backdrop.catalog.components.MenuEntry
import com.kyant.backdrop.catalog.components.isGlassDark
import com.kyant.backdrop.catalog.components.rememberVideoPlayerState
import com.kyant.backdrop.catalog.utils.BackHandler
import com.kyant.shapes.RoundedRectangle

@Composable
fun CustomGlassContent() {
    val dark = isGlassDark()
    val accent = if (dark) Color(0xFF40C8E0) else Color(0xFF30B0C7)
    var selected by rememberSaveable { mutableStateOf(CustomTabs.first().key) }

    CompositionLocalProvider(LocalGlassAccent provides accent) {
        LiquidAdaptiveTabScaffold(CustomTabs, selected, { selected = it }, sidebarIcon = ListIcon) { padding ->
            CustomControls(padding, dark)
        }
    }
}

@Composable
private fun CustomControls(padding: PaddingValues, dark: Boolean) {
    val backdrop = rememberLayerBackdrop()
    val video = rememberVideoPlayerState(duration = 60f)
    val page = if (dark) Color(0xFF131B23) else Color(0xFFE9F5FA)
    val frame = Brush.linearGradient(listOf(Color(0xFF15345E), Color(0xFF326C75), Color(0xFF443769)))
    var query by rememberSaveable { mutableStateOf("") }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var slider by rememberSaveable { mutableFloatStateOf(0.65f) }
    var count by rememberSaveable { mutableIntStateOf(3) }
    var menuOpen by remember { mutableStateOf(false) }
    BackHandler(video.fullscreen) { video.fullscreen = false }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.matchParentSize().layerBackdrop(backdrop).background(page))
        if (video.fullscreen) {
            LiquidVideoPlayer(video, Modifier.matchParentSize(), onClose = { video.fullscreen = false }, icons = PlayerIcons) {
                Box(Modifier.fillMaxSize().background(frame))
            }
        } else {
            Column(
                Modifier.align(Alignment.TopCenter).padding(padding).padding(horizontal = 20.dp)
                    .widthIn(max = 520.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                BasicText("Custom accent & icons", style = TextStyle(GlassColors.label(), 27.sp, FontWeight.Bold))
                BasicText("Teal glass · caller-supplied vector icons", style = TextStyle(GlassColors.secondaryLabel(), 15.sp))
                LiquidSearchBar(query, { query = it }, backdrop, Modifier.fillMaxWidth(), icons = SearchIcons)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LiquidSlider({ slider }, { slider = it }, 0f..1f, 0.001f, backdrop, Modifier.weight(1f))
                    LiquidToggle({ enabled }, { enabled = it }, backdrop)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    LiquidStepper(count, { count = it }, backdrop, decrementIcon = DownIcon, incrementIcon = UpIcon)
                    BasicText("Count: $count", style = TextStyle(GlassColors.label(), 15.sp))
                    LiquidBorderedButton("Menu", onClick = { menuOpen = true }, prominent = true)
                }
                LiquidVideoPlayer(
                    video,
                    Modifier.fillMaxWidth().height(200.dp).clip(RoundedRectangle(24.dp)),
                    icons = PlayerIcons,
                ) { Box(Modifier.fillMaxSize().background(frame)) }
            }
            LiquidMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                backdrop = backdrop,
                entries = CustomEntries,
                alignment = Alignment.Center,
                offset = DpOffset(0.dp, 30.dp),
                icons = MenuIcons,
            )
        }
    }
}

private fun customIcon(name: String, draw: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black), pathBuilder = draw)
    }.build()

private val ListIcon = customIcon("List") {
    for (y in listOf(5f, 10.5f, 16f)) {
        moveTo(3f, y); horizontalLineTo(21f); verticalLineTo(y + 3f); horizontalLineTo(3f); close()
    }
}
private val UpIcon = customIcon("Step up") {
    moveTo(3f, 17f); lineTo(12f, 6f); lineTo(21f, 17f); close()
}
private val DownIcon = customIcon("Step down") {
    moveTo(3f, 7f); lineTo(12f, 18f); lineTo(21f, 7f); close()
}
private val RightIcon = customIcon("Open submenu") {
    moveTo(8f, 3f); lineTo(19f, 12f); lineTo(8f, 21f); close()
}
private val StarIcon = customIcon("Star") {
    moveTo(12f, 2f); lineTo(15f, 8.8f); lineTo(22f, 9.5f); lineTo(17f, 14.5f); lineTo(18.5f, 22f)
    lineTo(12f, 18f); lineTo(5.5f, 22f); lineTo(7f, 14.5f); lineTo(2f, 9.5f); lineTo(9f, 8.8f); close()
}
private val SearchIcon = customIcon("Square search") {
    moveTo(2f, 2f); horizontalLineTo(16f); verticalLineTo(16f); horizontalLineTo(2f); close()
    moveTo(5f, 5f); verticalLineTo(13f); horizontalLineTo(13f); verticalLineTo(5f); close()
    moveTo(14f, 16f); lineTo(16f, 14f); lineTo(22f, 20f); lineTo(20f, 22f); close()
}
private val PlayIcon = customIcon("Framed play") {
    moveTo(2f, 2f); horizontalLineTo(22f); verticalLineTo(22f); horizontalLineTo(2f); close()
    moveTo(9f, 6f); verticalLineTo(18f); lineTo(18f, 12f); close()
}
private val CustomTabs = listOf(
    LiquidTabItem("library", "Library", ListIcon),
    LiquidTabItem("favorites", "Favorites", StarIcon),
    LiquidTabItem("search", "Search", SearchIcon, search = true),
)
private val SearchIcons = LiquidSearchBarIcons(search = SearchIcon)
private val MenuIcons = LiquidMenuIcons(check = StarIcon, submenu = RightIcon, back = DownIcon)
private val PlayerIcons = LiquidVideoPlayerIcons(play = PlayIcon, more = ListIcon, menu = MenuIcons)
private val CustomEntries = listOf(
    MenuEntry.Action("Favorite", checked = true) {},
    MenuEntry.Submenu("Collection", entries = listOf(MenuEntry.Action("Featured") {})),
)
