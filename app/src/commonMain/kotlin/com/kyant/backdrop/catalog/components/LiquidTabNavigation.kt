package com.kyant.backdrop.catalog.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/** A destination shown in the tab bar and/or the sidebar. A [search] tab is drawn icon-only at the end of the bar. */
@Immutable
data class LiquidTabItem(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val search: Boolean = false,
)

/** A sidebar group; a null [title] is the ungrouped top block (normally the tab bar's tabs). */
@Immutable
data class LiquidSidebarSection(val title: String?, val items: List<LiquidTabItem>)

/** Width at which tabs move from the bottom bar to the iPad-style floating top bar and the sidebar becomes available. */
val RegularWidthBreakpoint = 600.dp

/**
 * iPadOS 26 floating tab bar: a glass capsule centered at the top of the window, text tabs with a
 * neutral pill behind the selected one and an icon-only search tab at the end.
 */
@Composable
fun LiquidTopTabBar(
    tabs: List<LiquidTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .height(46.dp)
            .liquidGlassPanel(backdrop, { Capsule() })
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val selected = tab.key == selectedKey
            val pill by animateColorAsState(if (selected) GlassColors.fill() else Color.Transparent)
            val color = if (selected) GlassColors.accent() else GlassColors.label()
            Box(
                Modifier
                    .fillMaxHeight()
                    .clip(Capsule())
                    .background(pill)
                    .clickable(role = Role.Tab) { onSelect(tab.key) }
                    .semantics { this.selected = selected }
                    .padding(horizontal = if (tab.search) 11.dp else 13.dp),
                contentAlignment = Alignment.Center
            ) {
                if (tab.search) GlassIcon(tab.icon, color, Modifier.size(20.dp))
                else BasicText(tab.title, style = TextStyle(color, 15.sp, FontWeight.Medium))
            }
        }
    }
}

/**
 * iPadOS 26 sidebar: a floating, inset glass pane with an optional large title, icon rows and titled
 * sections. The selected row gets a neutral fill and accent-colored icon and label.
 */
@Composable
fun LiquidSidebar(
    sections: List<LiquidSidebarSection>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier
            .liquidGlassPanel(backdrop, { RoundedRectangle(26.dp) })
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(44.dp))
        if (title != null) {
            BasicText(
                title,
                Modifier.padding(start = 10.dp, bottom = 8.dp),
                style = TextStyle(GlassColors.label(), 28.sp, FontWeight.Bold)
            )
        }
        sections.forEach { section ->
            if (section.title != null) {
                BasicText(
                    section.title,
                    Modifier.padding(start = 10.dp, top = 18.dp, bottom = 6.dp),
                    style = TextStyle(GlassColors.label(), 17.sp, FontWeight.Bold)
                )
            }
            section.items.forEach { item ->
                val selected = item.key == selectedKey
                val fill by animateColorAsState(if (selected) GlassColors.fill() else Color.Transparent)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedRectangle(12.dp))
                        .background(fill)
                        .clickable(role = Role.Tab) { onSelect(item.key) }
                        .semantics { this.selected = selected }
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIcon(item.icon, GlassColors.accent(), Modifier.size(22.dp))
                    BasicText(
                        item.title,
                        style = TextStyle(
                            if (selected) GlassColors.accent() else GlassColors.label(),
                            17.sp,
                            if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

/**
 * Adaptive tab navigation, the equivalent of SwiftUI's `TabView` with `.sidebarAdaptable`:
 * - compact width: [LiquidBottomTabs] at the bottom (iPhone);
 * - regular width (>= [RegularWidthBreakpoint]): [LiquidTopTabBar] floating at the top center plus a
 *   sidebar toggle that slides in [LiquidSidebar] and hides the top bar (iPad).
 *
 * The scaffold records [content] into its own backdrop so the bars refract the page. [content] gets
 * the padding that keeps it clear of the bars and the sidebar.
 */
@Composable
fun LiquidAdaptiveTabScaffold(
    tabs: List<LiquidTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    sidebarSections: List<LiquidSidebarSection> = listOf(LiquidSidebarSection(null, tabs)),
    sidebarTitle: String? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    var sidebarVisible by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val regular = maxWidth >= RegularWidthBreakpoint
        val bars = WindowInsets.systemBars.asPaddingValues()
        val sidebarWidth = 320.dp
        val sidebarProgress by animateFloatAsState(
            if (regular && sidebarVisible) 1f else 0f,
            spring(dampingRatio = 0.85f, stiffness = 500f)
        )
        val contentPadding =
            if (regular) PaddingValues(
                start = (sidebarWidth + 16.dp) * sidebarProgress,
                top = bars.calculateTopPadding() + 62.dp,
                bottom = bars.calculateBottomPadding()
            )
            else PaddingValues(top = bars.calculateTopPadding(), bottom = bars.calculateBottomPadding() + 88.dp)

        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) { content(contentPadding) }

        if (regular) {
            val density = LocalDensity.current
            if (sidebarProgress > 0f) {
                LiquidSidebar(
                    sidebarSections, selectedKey, onSelect, backdrop,
                    Modifier
                        .systemBarsPadding()
                        .padding(8.dp)
                        .width(sidebarWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = -(1f - sidebarProgress) * with(density) { (sidebarWidth + 16.dp).toPx() }
                        },
                    title = sidebarTitle
                )
            }
            if (sidebarProgress < 1f) {
                LiquidTopTabBar(
                    tabs, selectedKey, onSelect, backdrop,
                    Modifier
                        .align(Alignment.TopCenter)
                        .systemBarsPadding()
                        .padding(top = 8.dp)
                        .graphicsLayer {
                            alpha = 1f - sidebarProgress
                            translationY = -sidebarProgress * 24.dp.toPx()
                        }
                )
            }
            LiquidIconButton(
                GlassIcons.SidebarLeft,
                if (sidebarVisible) "Hide sidebar" else "Show sidebar",
                { sidebarVisible = !sidebarVisible },
                backdrop,
                Modifier.systemBarsPadding().padding(start = 18.dp, top = 9.dp)
            )
        } else {
            val selectedIndex = tabs.indexOfFirst { it.key == selectedKey }.coerceAtLeast(0)
            LiquidBottomTabs(
                selectedTabIndex = { selectedIndex },
                onTabSelected = { onSelect(tabs[it].key) },
                backdrop = backdrop,
                tabsCount = tabs.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                tabs.forEach { tab ->
                    LiquidBottomTab({ onSelect(tab.key) }) {
                        GlassIcon(tab.icon, GlassColors.label(), Modifier.size(26.dp))
                        BasicText(tab.title, style = TextStyle(GlassColors.label(), 11.sp, FontWeight.Medium))
                    }
                }
            }
        }
    }
}
