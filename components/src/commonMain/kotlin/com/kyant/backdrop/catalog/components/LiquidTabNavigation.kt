package com.kyant.backdrop.catalog.components

import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.foundation.focusable
import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.zIndex
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
    /** Overrides the bar's preset width for this tab ([LiquidTopTabBarDefaults]). */
    val width: Dp = Dp.Unspecified,
)

/** Preset tab widths of [LiquidTopTabBar]; override them per bar with its parameters or per tab with [LiquidTabItem.width]. */
object LiquidTopTabBarDefaults {
    val TextTabWidth: Dp = 82.dp
    val IconTabWidth: Dp = 48.dp
}

/** A sidebar group; a null [title] is the ungrouped top block (normally the tab bar's tabs). */
@Immutable
data class LiquidSidebarSection(val title: String?, val items: List<LiquidTabItem>)

/** Width at which tabs move from the bottom bar to the iPad-style floating top bar and the sidebar becomes available. */
val RegularWidthBreakpoint = 600.dp

/**
 * iPadOS 26 floating tab bar: [LiquidBottomTabs] moved to the top of the window and sized for it (a 46 dp
 * capsule with a 38 dp selection pill), with the sidebar toggle inside the capsule ahead of the tabs. The
 * pill is the bottom tab pill itself: press it to swell it into a clear lens and drag it across the bar to
 * pick a tab, as on iPad. Text tabs and the icon-only search tab share the bar equally.
 */
@Composable
fun LiquidTopTabBar(
    tabs: List<LiquidTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onToggleSidebar: (() -> Unit)? = null,
    selectedColor: Color = GlassColors.accent(),
    sidebarIcon: ImageVector = GlassIcons.SidebarLeft,
    tabWidth: Dp = LiquidTopTabBarDefaults.TextTabWidth,
    iconTabWidth: Dp = LiquidTopTabBarDefaults.IconTabWidth,
) {
    val widths = tabs.map { if (it.width.isSpecified) it.width else if (it.search) iconTabWidth else tabWidth }
    // LiquidBottomTabs keys its state on the lambda's identity, so the lambda must be stable and read a State.
    val selectedIndex = rememberUpdatedState(tabs.indexOfFirst { it.key == selectedKey }.coerceAtLeast(0))
    val readSelectedIndex = remember { { selectedIndex.value } }
    val currentTabs by rememberUpdatedState(tabs)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val leadingWidth = if (onToggleSidebar != null) 44.dp else 0.dp
    LiquidBottomTabs(
        selectedTabIndex = readSelectedIndex,
        onTabSelected = { index -> currentOnSelect(currentTabs[index].key) },
        backdrop = backdrop,
        tabsCount = tabs.size,
        modifier = modifier.width(leadingWidth + widths.fold(0.dp) { a, b -> a + b } + 8.dp),
        tabWidths = widths,
        accentColor = selectedColor,
        height = 46.dp,
        pillHeight = 38.dp,
        leadingWidth = leadingWidth,
        leading = {
            if (onToggleSidebar != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(Capsule())
                        .clickable(role = Role.Button, onClick = onToggleSidebar)
                        .semantics { contentDescription = "Show sidebar" },
                    contentAlignment = Alignment.Center
                ) { GlassIcon(sidebarIcon, GlassColors.label(), Modifier.size(22.dp)) }
            }
        }
    ) {
        tabs.forEachIndexed { i, tab ->
            LiquidBottomTab(
                { onSelect(tab.key) },
                width = widths[i],
                modifier = Modifier.semantics {
                    this.selected = tab.key == selectedKey
                    contentDescription = tab.title
                }
            ) {
                if (tab.search) GlassIcon(tab.icon, GlassColors.label(), Modifier.size(20.dp))
                else BasicText(tab.title, style = TextStyle(GlassColors.label(), 15.sp, FontWeight.Medium))
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
    selectedColor: Color = GlassColors.accent(),
    sidebarIcon: ImageVector = GlassIcons.SidebarLeft,
    expandedSectionIcon: ImageVector = GlassIcons.ChevronDown,
    collapsedSectionIcon: ImageVector = GlassIcons.ChevronRight,
) {
    val collapsed = rememberSaveable(saver = listSaver<MutableList<String>, String>({ it.toList() }, { it.toMutableStateList() })) {
        mutableStateListOf()
    }
    // Same refracting glass as the tab bars; a little denser so rows stay legible over busy content.
    val surface = if (isGlassDark()) Color(0xFF121212).copy(alpha = 0.5f) else Color(0xFFFAFAFA).copy(alpha = 0.55f)
    Column(
        modifier
            .liquidGlassPanel(
                backdrop, { RoundedRectangle(26.dp) },
                containerColor = surface, blurRadius = 16.dp, refractionHeight = 20.dp, refractionAmount = 40.dp
            )
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
                ) { GlassIcon(sidebarIcon, GlassColors.label(), Modifier.size(22.dp)) }
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
                                if (open) expandedSectionIcon else collapsedSectionIcon,
                                GlassColors.label(),
                                Modifier.size(15.dp)
                            )
                        }
                    }
                }
                if (open) section.items.forEach { item ->
                    val selected = item.key == selectedKey
                    val fill by animateColorAsState(if (selected) GlassColors.fill() else Color.Transparent)
                    val color = if (selected) selectedColor else GlassColors.label()
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
    BackHandler(visible && !docked, onDismiss)
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
    selectedColor: Color = GlassColors.accent(),
    sidebarIcon: ImageVector = GlassIcons.SidebarLeft,
    expandedSectionIcon: ImageVector = GlassIcons.ChevronDown,
    collapsedSectionIcon: ImageVector = GlassIcons.ChevronRight,
    searchQuery: String? = null,
    onSearchQueryChange: (String) -> Unit = {},
    searchPlaceholder: String = "Search",
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    var ownQuery by rememberSaveable { mutableStateOf("") }
    val query = searchQuery ?: ownQuery
    val setQuery: (String) -> Unit = { if (searchQuery == null) ownQuery = it; onSearchQueryChange(it) }
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
                        onToggleSidebar = { sidebarVisible = false },
                        selectedColor = selectedColor,
                        sidebarIcon = sidebarIcon,
                        expandedSectionIcon = expandedSectionIcon,
                        collapsedSectionIcon = collapsedSectionIcon,
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
                        onToggleSidebar = { sidebarVisible = true },
                        selectedColor = selectedColor,
                        sidebarIcon = sidebarIcon,
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
            // While Search (not in the bar) is selected the bar keeps its last tab; moving it would report a selection.
            val barIndex = remember { mutableIntStateOf(0) }
            val selectedBarIndex = barTabs.indexOfFirst { it.key == selectedKey }
            SideEffect { if (selectedBarIndex >= 0) barIndex.intValue = selectedBarIndex }
            val readSelectedIndex = remember { { barIndex.intValue } }
            // iOS 26: selecting Search shrinks the bar into a circle holding the last tab while the search circle
            // stretches into the field (~0.2 s); the circle returns to that tab.
            val searching = searchTab != null && searchTab.key == selectedKey
            val morph by animateFloatAsState(if (searching) 1f else 0f, spring(1f, 600f, 0.001f))
            var fieldFocused by remember { mutableStateOf(false) }
            if (morph > 0f && searchTab != null) {
                val lastTab = barTabs[barIndex.intValue.coerceIn(0, barTabs.lastIndex)]
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        // Unconditional so a dismissed field rides down with the keyboard, like iOS 26.
                        .imePadding()
                        .systemBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        .graphicsLayer { alpha = morph },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // iOS 26 brings the tab circle back only after the keyboard has gone.
                    AnimatedVisibility(
                        !fieldFocused,
                        enter = fadeIn(tween(150, 150)) + scaleIn(tween(150, 150), 0.5f) +
                            expandHorizontally(tween(150, 150)),
                        exit = fadeOut(tween(100)) + scaleOut(tween(100), 0.5f) + shrinkHorizontally(tween(100))
                    ) {
                        LiquidIconButton(
                            lastTab.icon, lastTab.title, { onSelect(lastTab.key) }, backdrop,
                            Modifier.graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                val s = lerp(1.4f, 1f, morph)
                                scaleX = s
                                scaleY = s
                            },
                            size = 48.dp, iconSize = 22.dp,
                        )
                    }
                    LiquidSearchBar(
                        query, setQuery, backdrop,
                        Modifier
                            .weight(1f)
                            .onFocusChanged { fieldFocused = it.hasFocus }
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                scaleX = lerp(0.2f, 1f, morph)
                            },
                        placeholder = searchPlaceholder,
                    )
                }
            }
            if (morph < 1f) Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    .graphicsLayer {
                        alpha = 1f - morph
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        scaleX = lerp(1f, 0.25f, morph)
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidBottomTabs(
                    selectedTabIndex = readSelectedIndex,
                    onTabSelected = { onSelect(barTabs[it].key) },
                    backdrop = backdrop,
                    tabsCount = barTabs.size,
                    modifier = Modifier.weight(1f),
                    accentColor = selectedColor
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
                        tint = if (searchTab.key == selectedKey) selectedColor else Color.Unspecified
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
    selectedColor: Color = GlassColors.accent(),
    sidebarIcon: ImageVector = GlassIcons.SidebarLeft,
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
                    collapsibleSections = false,
                    selectedColor = selectedColor,
                    sidebarIcon = sidebarIcon,
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
                    sidebarIcon, "Show sidebar", { sidebarVisible = true }, backdrop,
                    Modifier
                        .systemBarsPadding()
                        .padding(start = 16.dp, top = 9.dp)
                        .graphicsLayer { alpha = toggleAlpha.coerceIn(0f, 1f) }
                )
            }
        }
    }
}
