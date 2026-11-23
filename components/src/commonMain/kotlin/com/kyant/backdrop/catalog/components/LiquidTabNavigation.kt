package com.kyant.backdrop.catalog.components

import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.foundation.focusable
import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.animation.core.Animatable
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
 * iPadOS 26 floating tab bar: a Liquid Glass capsule (the same refracting material as [LiquidBottomTabs])
 * centered at the top of the window with an optional leading sidebar toggle, text tabs and an icon-only
 * search tab at the end. The selection is a glass droplet that slides between tabs; press it to swell it
 * and drag it across the bar to pick a tab, as on iPad.
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
    val glass = LocalLiquidGlassStyle.current
    val dark = isGlassDark()
    val containerColor = if (dark) Color(0xFF121212).copy(0.4f) else Color(0xFFFAFAFA).copy(0.4f)
    val barBackdrop = rememberLayerBackdrop()
    val scope = rememberCoroutineScope()
    val bounds = remember { mutableStateMapOf<String, Pair<Float, Float>>() }
    val indicatorX = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    val press = remember { Animatable(0f) }
    var dragX by remember { mutableStateOf<Float?>(null) }
    val currentSelected by rememberUpdatedState(selectedKey)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val selected = bounds[selectedKey]
    LaunchedEffect(selected) {
        val (x, w) = selected ?: return@LaunchedEffect
        if (indicatorWidth.value == 0f) {
            indicatorX.snapTo(x); indicatorWidth.snapTo(w)
        } else {
            launch { indicatorX.animateTo(x, spring(0.72f, 420f)) }
            indicatorWidth.animateTo(w, spring(0.72f, 420f))
        }
    }
    fun keyAt(x: Float): String? = bounds.entries.minByOrNull { (_, b) ->
        val (l, w) = b
        if (x < l) l - x else if (x > l + w) x - l - w else 0f
    }?.key
    val hovered = dragX?.let(::keyAt) ?: selectedKey

    Row(
        modifier
            .height(46.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx() * glass.blur)
                    lens(16.dp.toPx() * glass.refraction, 24.dp.toPx() * glass.refraction)
                },
                layerBlock = {
                    val s = lerp(1f, 1f + 8.dp.toPx() / size.width, press.value)
                    scaleX = s
                    scaleY = s
                },
                exportedBackdrop = barBackdrop,
                onDrawSurface = { if (dark) drawDarkGlass(glass.opacity) else drawRect(containerColor.copy(alpha = containerColor.alpha * glass.opacity)) }
            )
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
        Box(
            Modifier
                .fillMaxHeight()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val onIndicator = bounds[currentSelected]?.let { (l, w) -> down.position.x in l..(l + w) } == true
                        scope.launch { press.animateTo(1f, spring(0.6f, 500f)) }
                        var moved = false
                        var last = down.position
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            last = change.position
                            if (!change.pressed) break
                            if (onIndicator && (moved || abs(change.position.x - down.position.x) > viewConfiguration.touchSlop)) {
                                moved = true
                                change.consume()
                                val x = change.position.x
                                dragX = x
                                scope.launch {
                                    val w = indicatorWidth.value
                                    indicatorX.snapTo(x - w / 2f)
                                }
                            }
                        }
                        val inside = last.x in 0f..size.width.toFloat() && last.y in 0f..size.height.toFloat() && keyAt(last.x) == keyAt(down.position.x)
                        val target = if (moved) dragX?.let(::keyAt) else if (inside) keyAt(down.position.x) else null
                        dragX = null
                        scope.launch { press.animateTo(0f, spring(0.5f, 300f)) }
                        if (target != null) {
                            if (target == currentSelected) {
                                bounds[target]?.let { (x, _) -> scope.launch { indicatorX.animateTo(x, spring(0.72f, 420f)) } }
                            } else currentOnSelect(target)
                        }
                    }
                }
        ) {
            if (indicatorWidth.value > 0f) {
                val density = LocalDensity.current
                Box(
                    Modifier
                        .graphicsLayer {
                            translationX = indicatorX.value
                            val s = lerp(1f, 1.18f, press.value)
                            scaleX = s
                            scaleY = s
                        }
                        .width(with(density) { indicatorWidth.value.toDp() })
                        .fillMaxHeight()
                        .drawBackdrop(
                            backdrop = rememberCombinedBackdrop(backdrop, barBackdrop),
                            shape = { Capsule() },
                            effects = {
                                lens(
                                    10.dp.toPx() * press.value * glass.refraction,
                                    14.dp.toPx() * press.value * glass.refraction,
                                    chromaticAberration = true
                                )
                            },
                            highlight = { Highlight.Default.copy(alpha = press.value) },
                            shadow = { Shadow(alpha = press.value) },
                            innerShadow = { InnerShadow(radius = 8.dp * press.value, alpha = press.value) },
                            onDrawSurface = {
                                drawRect(
                                    if (dark) Color.White.copy(0.1f) else Color.Black.copy(0.08f),
                                    alpha = 1f - press.value
                                )
                            }
                        )
                )
            }
            Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { tab ->
                    val active = tab.key == hovered
                    val color by animateColorAsState(if (active) GlassColors.accent() else GlassColors.label())
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .onPlaced { bounds[tab.key] = it.positionInParent().x to it.size.width.toFloat() }
                            .onKeyEvent {
                                if (it.type == KeyEventType.KeyUp && (it.key == Key.Enter || it.key == Key.NumPadEnter || it.key == Key.Spacebar)) {
                                    onSelect(tab.key)
                                    true
                                } else false
                            }
                            .focusable()
                            .semantics {
                                role = Role.Tab
                                this.selected = tab.key == selectedKey
                                contentDescription = tab.title
                                onClick { onSelect(tab.key); true }
                            }
                            .padding(horizontal = if (tab.search) 11.dp else 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tab.search) GlassIcon(tab.icon, color, Modifier.size(20.dp))
                        else BasicText(tab.title, style = TextStyle(color, 15.sp, FontWeight.Medium))
                    }
                }
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
            // While Search (not in the bar) is selected the bar keeps its last tab; moving it would report a selection.
            val barIndex = remember { mutableIntStateOf(0) }
            val selectedBarIndex = barTabs.indexOfFirst { it.key == selectedKey }
            SideEffect { if (selectedBarIndex >= 0) barIndex.intValue = selectedBarIndex }
            val readSelectedIndex = remember { { barIndex.intValue } }
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
