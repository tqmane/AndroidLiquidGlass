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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.gestures.detectTapGestures
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
 * iPadOS 26 floating tab bar: a glass capsule centered at the top of the window with an optional
 * leading sidebar toggle, text tabs with a neutral pill behind the selected one and an icon-only
 * search tab at the end.
 */
@Composable
fun LiquidTopTabBar(
    tabs: List<LiquidTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onToggleSidebar: (() -> Unit)? = null,
) {
    Row(
        modifier
            .height(46.dp)
            .liquidGlassPanel(backdrop, { Capsule() })
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onToggleSidebar != null) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .clip(Capsule())
                    .clickable(role = Role.Button, onClick = onToggleSidebar)
                    .semantics { contentDescription = "Show sidebar" }
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center
            ) { GlassIcon(GlassIcons.SidebarLeft, GlassColors.label(), Modifier.size(22.dp)) }
        }
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
                    .semantics { this.selected = selected; contentDescription = tab.title }
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
 * iPadOS 26 sidebar pane: a floating, inset glass panel. The header row holds an optional centered
 * [title] and the sidebar toggle at the end. Rows use label-colored icons; the selected row gets a
 * neutral capsule with accent icon and label. Titled sections can collapse with their chevron.
 */
@Composable
fun LiquidSidebar(
    sections: List<LiquidSidebarSection>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    title: String? = null,
    onToggleSidebar: (() -> Unit)? = null,
    collapsibleSections: Boolean = true,
) {
    val collapsed = rememberSaveable(saver = listSaver<MutableList<String>, String>({ it.toList() }, { it.toMutableStateList() })) {
        mutableStateListOf()
    }
    val surface = if (isGlassDark()) Color(0xFF1C1C1E).copy(alpha = 0.82f) else Color(0xFFF7F7FA).copy(alpha = 0.86f)
    Column(
        modifier
            .liquidGlassPanel(backdrop, { RoundedRectangle(26.dp) }, containerColor = surface, blurRadius = 24.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(44.dp)) {
            if (title != null) {
                BasicText(
                    title,
                    Modifier.align(Alignment.Center),
                    style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold)
                )
            }
            if (onToggleSidebar != null) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .clip(Capsule())
                        .clickable(role = Role.Button, onClick = onToggleSidebar)
                        .semantics { contentDescription = "Hide sidebar" },
                    contentAlignment = Alignment.Center
                ) { GlassIcon(GlassIcons.SidebarLeft, GlassColors.label(), Modifier.size(22.dp)) }
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(if (title != null) 4.dp else 12.dp))
            sections.forEach { section ->
                val sectionTitle = section.title
                val open = sectionTitle == null || sectionTitle !in collapsed
                if (sectionTitle != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .then(
                                if (collapsibleSections) Modifier.clickable(role = Role.Button) {
                                    if (open) collapsed.add(sectionTitle) else collapsed.remove(sectionTitle)
                                } else Modifier
                            )
                            .padding(start = 12.dp, end = 10.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText(
                            sectionTitle,
                            Modifier.weight(1f),
                            style = TextStyle(GlassColors.secondaryLabel(), 15.sp, FontWeight.Medium)
                        )
                        if (collapsibleSections) {
                            GlassIcon(
                                if (open) GlassIcons.ChevronDown else GlassIcons.ChevronRight,
                                GlassColors.label(),
                                Modifier.size(15.dp)
                            )
                        }
                    }
                }
                if (open) section.items.forEach { item ->
                    val selected = item.key == selectedKey
                    val fill by animateColorAsState(if (selected) GlassColors.fill() else Color.Transparent)
                    val color = if (selected) GlassColors.accent() else GlassColors.label()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(Capsule())
                            .background(fill)
                            .clickable(role = Role.Tab) { onSelect(item.key) }
                            .semantics { this.selected = selected }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIcon(item.icon, color, Modifier.size(22.dp))
                        BasicText(item.title, style = TextStyle(color, 17.sp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Window width from which an open sidebar sits beside the content instead of over it (iPad landscape). */
val SidebarDockedBreakpoint = 1000.dp

/**
 * Shared sidebar presentation: slides the pane in from the leading edge; when the window is at least
 * [SidebarDockedBreakpoint] wide the content moves aside, otherwise the pane floats over a dimmed
 * content that dismisses it on tap (iPad portrait).
 */
@Composable
private fun BoxWithConstraintsScope.SidebarHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    width: Dp,
    sidebar: @Composable (Modifier) -> Unit,
    content: @Composable (startPadding: Dp) -> Unit,
) {
    val docked = maxWidth >= SidebarDockedBreakpoint
    val spring by animateFloatAsState(if (visible) 1f else 0f, spring(dampingRatio = 0.85f, stiffness = 500f))
    // The spring overshoots past 0 and 1; padding and alpha must stay in range.
    val progress = spring.coerceIn(0f, 1f)
    content(if (docked) (width + 8.dp) * progress else 0.dp)
    if (!docked && progress > 0f) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f * progress))
                .then(if (visible) Modifier.pointerInput(Unit) { detectTapGestures { onDismiss() } } else Modifier)
        )
    }
    if (progress > 0f) {
        val density = LocalDensity.current
        sidebar(
            Modifier
                .systemBarsPadding()
                .padding(8.dp)
                .width(width)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = -(1f - progress) * with(density) { (width + 16.dp).toPx() }
                }
        )
    }
}

/**
 * Adaptive tab navigation, the equivalent of SwiftUI's `TabView` with `.sidebarAdaptable`:
 * - compact width: [LiquidBottomTabs] at the bottom with the search tab as a separate glass circle (iPhone);
 * - regular width (>= [RegularWidthBreakpoint]): [LiquidTopTabBar] floating at the top center with the
 *   sidebar toggle inside it; the toggle slides in [LiquidSidebar] and hides the top bar (iPad).
 *
 * The scaffold records [content] into its own backdrop so the bars refract the page. [content] gets
 * the padding that keeps it clear of the bars and a docked sidebar.
 */
@Composable
fun LiquidAdaptiveTabScaffold(
    tabs: List<LiquidTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    sidebarSections: List<LiquidSidebarSection> = listOf(LiquidSidebarSection(null, tabs)),
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    var sidebarVisible by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val regular = maxWidth >= RegularWidthBreakpoint
        val bars = WindowInsets.systemBars.asPaddingValues()
        if (regular) {
            SidebarHost(
                visible = sidebarVisible,
                onDismiss = { sidebarVisible = false },
                width = 270.dp,
                sidebar = { m ->
                    LiquidSidebar(
                        sidebarSections, selectedKey, onSelect, backdrop, m,
                        onToggleSidebar = { sidebarVisible = false }
                    )
                }
            ) { start ->
                Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                    content(
                        PaddingValues(
                            start = start,
                            top = bars.calculateTopPadding() + 62.dp,
                            bottom = bars.calculateBottomPadding()
                        )
                    )
                }
                val barAlpha by animateFloatAsState(if (sidebarVisible) 0f else 1f)
                if (barAlpha > 0f) {
                    LiquidTopTabBar(
                        tabs, selectedKey, onSelect, backdrop,
                        Modifier
                            .align(Alignment.TopCenter)
                            .systemBarsPadding()
                            .padding(top = 8.dp)
                            .graphicsLayer { alpha = barAlpha.coerceIn(0f, 1f) },
                        onToggleSidebar = { sidebarVisible = true }
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                content(PaddingValues(top = bars.calculateTopPadding(), bottom = bars.calculateBottomPadding() + 88.dp))
            }
            val barTabs = tabs.filter { !it.search }
            val searchTab = tabs.firstOrNull { it.search }
            // A sidebar-only destination has no bottom tab; fall back to the first tab so the page and the bar agree.
            LaunchedEffect(selectedKey) {
                if (tabs.none { it.key == selectedKey }) onSelect(barTabs.first().key)
            }
            // LiquidBottomTabs keys its state on the lambda's identity, so the lambda must be stable and read a State.
            val selectedIndex = rememberUpdatedState(barTabs.indexOfFirst { it.key == selectedKey })
            val readSelectedIndex = remember { { selectedIndex.value.coerceAtLeast(0) } }
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidBottomTabs(
                    selectedTabIndex = readSelectedIndex,
                    onTabSelected = { onSelect(barTabs[it].key) },
                    backdrop = backdrop,
                    tabsCount = barTabs.size,
                    modifier = Modifier.weight(1f)
                ) {
                    barTabs.forEach { tab ->
                        LiquidBottomTab({ onSelect(tab.key) }) {
                            GlassIcon(tab.icon, GlassColors.label(), Modifier.size(26.dp))
                            BasicText(tab.title, style = TextStyle(GlassColors.label(), 11.sp, FontWeight.Medium))
                        }
                    }
                }
                if (searchTab != null) {
                    LiquidIconButton(
                        searchTab.icon, searchTab.title, { onSelect(searchTab.key) }, backdrop,
                        size = 64.dp, iconSize = 24.dp,
                        tint = if (searchTab.key == selectedKey) GlassColors.accent() else Color.Unspecified
                    )
                }
            }
        }
    }
}

/**
 * Two-column layout, the equivalent of SwiftUI's `NavigationSplitView`: a 320 dp [LiquidSidebar] with a
 * centered [title] and the detail [content]. The sidebar starts open on regular width and closed on
 * compact width; a glass toggle at the top start reopens it. [content] gets the padding that keeps it
 * clear of the system bars, the toggle and a docked sidebar.
 */
@Composable
fun LiquidSplitView(
    sections: List<LiquidSidebarSection>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    BoxWithConstraints(modifier.fillMaxSize()) {
        var sidebarVisible by rememberSaveable { mutableStateOf(maxWidth >= SidebarDockedBreakpoint) }
        val bars = WindowInsets.systemBars.asPaddingValues()
        SidebarHost(
            visible = sidebarVisible,
            onDismiss = { sidebarVisible = false },
            width = 320.dp,
            sidebar = { m ->
                LiquidSidebar(
                    sections, selectedKey,
                    { onSelect(it); if (maxWidth < SidebarDockedBreakpoint) sidebarVisible = false },
                    backdrop, m,
                    title = title,
                    onToggleSidebar = { sidebarVisible = false },
                    collapsibleSections = false
                )
            }
        ) { start ->
            Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                content(
                    PaddingValues(
                        start = start,
                        top = bars.calculateTopPadding() + 62.dp,
                        bottom = bars.calculateBottomPadding()
                    )
                )
            }
            val toggleAlpha by animateFloatAsState(if (sidebarVisible) 0f else 1f)
            if (toggleAlpha > 0f) {
                LiquidIconButton(
                    GlassIcons.SidebarLeft, "Show sidebar", { sidebarVisible = true }, backdrop,
                    Modifier
                        .systemBarsPadding()
                        .padding(start = 16.dp, top = 9.dp)
                        .graphicsLayer { alpha = toggleAlpha.coerceIn(0f, 1f) }
                )
            }
        }
    }
}
