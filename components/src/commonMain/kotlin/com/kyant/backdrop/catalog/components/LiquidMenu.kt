package com.kyant.backdrop.catalog.components

import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.RoundedRectangle

/** An entry of a [LiquidMenu] / [LiquidContextMenu]. [iconTint] colors the row's icon (checked rows too); null uses the title color. */
sealed interface MenuEntry {
    data class Action(
        val title: String,
        val icon: ImageVector? = null,
        val destructive: Boolean = false,
        val checked: Boolean = false,
        val enabled: Boolean = true,
        val subtitle: String? = null,
        val iconTint: Color? = null,
        val onClick: () -> Unit = {},
    ) : MenuEntry

    data class Submenu(
        val title: String,
        val icon: ImageVector? = null,
        val entries: List<MenuEntry>,
        val subtitle: String? = null,
        val iconTint: Color? = null,
    ) : MenuEntry

    data object Divider : MenuEntry

    /** Small secondary header that starts a section, like a titled inline `UIMenu`; a divider goes above it unless it comes first. */
    data class Section(val title: String) : MenuEntry
}

/** Where a checked [MenuEntry.Action] shows its checkmark. */
enum class CheckmarkPlacement {
    /** The checkmark replaces the row's icon. */
    Icon,

    /** A leading gutter holds the checkmark and the row keeps its icon, like an iOS 26 Picker menu (sort order). */
    Gutter,
}

/** Icons for checked rows, submenu links, and the submenu's return row. */
@Immutable
data class LiquidMenuIcons(
    val check: ImageVector = GlassIcons.Check,
    val submenu: ImageVector = GlassIcons.ChevronRight,
    val back: ImageVector = GlassIcons.ChevronDown,
)

/**
 * Animated Liquid Glass panel shared by menus and popovers: it grows out of [transformOrigin]
 * with a spring, like UIKit's morphing glass presentations, and a tap outside dismisses it.
 */
@Composable
fun BoxScope.LiquidGlassPopup(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    alignment: Alignment,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    cornerRadius: Dp = 26.dp,
    dim: Boolean = false,
    paneTitle: String = "Menu",
    content: @Composable ColumnScope.(panelBackdrop: Backdrop) -> Unit,
) {
    BackHandler(visible) { onDismissRequest() }
    val panelBackdrop = rememberLayerBackdrop()
    Box(Modifier.matchParentSize().zIndex(1f)) {
        val progress = remember { Animatable(0f) }
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(visible) {
            if (visible) {
                shown = true
                progress.animateTo(1f, spring(0.72f, 420f))
            } else {
                progress.animateTo(0f, spring(1f, 700f))
                shown = false
            }
        }
        if (!shown && !visible) return

        Box(
            Modifier
                .matchParentSize()
                .then(if (dim) Modifier.background(Color.Black.copy(alpha = 0.18f * progress.value.coerceIn(0f, 1f))) else Modifier)
                .clickable(interactionSource = null, indication = null, onClick = onDismissRequest)
        )
        Column(
            modifier
                .align(alignment)
                .offset(offset.x, offset.y)
                .semantics {
                    this.paneTitle = paneTitle
                    dismiss { onDismissRequest(); true }
                }
                .graphicsLayer {
                    val p = progress.value
                    this.transformOrigin = transformOrigin
                    scaleX = lerp(0.3f, 1f, p)
                    scaleY = lerp(0.2f, 1f, p)
                    alpha = p.coerceIn(0f, 1f)
                    // Per-draw alpha: an offscreen alpha layer drops the glass outline clip and flashes a rectangle.
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .liquidGlassPanel(backdrop, { RoundedRectangle(cornerRadius) }, exportedBackdrop = panelBackdrop)
                .clickable(interactionSource = null, indication = null) {}
        ) { content(panelBackdrop) }
    }
}

/** iOS 26 pull-down menu (`UIMenu` from a bar button). Supports icons, checkmarks, destructive items, dividers and submenus. */
@Composable
fun BoxScope.LiquidMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    entries: List<MenuEntry>,
    alignment: Alignment,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    transformOrigin: TransformOrigin = TransformOrigin(1f, 0f),
    width: Dp = 250.dp,
    header: String? = null,
    icons: LiquidMenuIcons = LiquidMenuIcons(),
    checkmarkPlacement: CheckmarkPlacement = CheckmarkPlacement.Icon,
) {
    var submenu by remember(expanded) { mutableStateOf<MenuEntry.Submenu?>(null) }
    LiquidGlassPopup(
        visible = expanded,
        onDismissRequest = onDismissRequest,
        backdrop = backdrop,
        alignment = alignment,
        modifier = modifier.width(width),
        offset = offset,
        transformOrigin = transformOrigin,
    ) {
        // The panel morphs between the menu and submenu sizes while the rows cross-fade, as in UIKit;
        // a plain Crossfade snapped the glass to the taller page for the whole fade.
        AnimatedContent(
            submenu,
            transitionSpec = {
                fadeIn(tween(160, delayMillis = 60)) togetherWith fadeOut(tween(100)) using
                    SizeTransform(clip = true) { _, _ -> spring(0.9f, 500f, IntSize.VisibilityThreshold) }
            },
            contentAlignment = if (transformOrigin.pivotFractionY > 0.5f) Alignment.BottomCenter else Alignment.TopCenter,
            label = "submenu"
        ) { open ->
            Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                if (open == null && header != null) {
                    MenuSectionTitle(header)
                }
                if (open == null) {
                    MenuEntries(entries, onDismissRequest, icons, checkmarkPlacement, sectionAfterHeader = header != null) { submenu = it }
                } else {
                    MenuRow(
                        title = open.title,
                        icon = icons.back,
                        color = GlassColors.label(),
                        bold = true,
                        gutter = checkmarkPlacement == CheckmarkPlacement.Gutter,
                        onClick = { submenu = null }
                    )
                    MenuDivider()
                    MenuEntries(open.entries, onDismissRequest, icons, checkmarkPlacement, sectionAfterHeader = true) { submenu = it }
                }
            }
        }
    }
}

@Composable
internal fun MenuEntries(
    entries: List<MenuEntry>,
    onDismissRequest: () -> Unit,
    icons: LiquidMenuIcons,
    checkmarkPlacement: CheckmarkPlacement = CheckmarkPlacement.Icon,
    sectionAfterHeader: Boolean = false,
    onOpenSubmenu: (MenuEntry.Submenu) -> Unit,
) {
    val gutter = checkmarkPlacement == CheckmarkPlacement.Gutter
    entries.forEachIndexed { index, entry ->
        when (entry) {
            is MenuEntry.Action -> MenuRow(
                title = entry.title,
                subtitle = entry.subtitle,
                icon = if (entry.checked && !gutter) icons.check else entry.icon,
                color = when {
                    entry.destructive -> GlassColors.destructive()
                    else -> GlassColors.label()
                }.let { if (entry.enabled) it else it.copy(alpha = 0.3f) },
                iconColor = entry.iconTint?.let { if (entry.enabled) it else it.copy(alpha = it.alpha * 0.3f) },
                enabled = entry.enabled,
                gutter = gutter,
                gutterIcon = if (gutter && entry.checked) icons.check else null,
                checked = entry.checked,
                onClick = {
                    entry.onClick()
                    onDismissRequest()
                }
            )

            is MenuEntry.Submenu -> MenuRow(
                title = entry.title,
                icon = entry.icon,
                subtitle = entry.subtitle,
                trailing = icons.submenu,
                color = GlassColors.label(),
                iconColor = entry.iconTint,
                gutter = gutter,
                onClick = { onOpenSubmenu(entry) }
            )

            MenuEntry.Divider -> MenuDivider()

            is MenuEntry.Section -> {
                val first = index == 0 && !sectionAfterHeader
                if (!first && entries.getOrNull(index - 1) != MenuEntry.Divider) MenuDivider()
                MenuSectionTitle(entry.title)
            }
        }
    }
}

@Composable
private fun MenuSectionTitle(title: String) {
    BasicText(
        title,
        Modifier.padding(horizontal = 20.dp, vertical = 6.dp).semantics { heading() },
        style = TextStyle(GlassColors.secondaryLabel(), 13.sp, FontWeight.SemiBold)
    )
}

@Composable
private fun MenuDivider() {
    Box(
        Modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(GlassColors.separator())
    )
}

@Composable
private fun MenuRow(
    title: String,
    icon: ImageVector?,
    color: Color,
    onClick: () -> Unit,
    iconColor: Color? = null,
    subtitle: String? = null,
    trailing: ImageVector? = null,
    enabled: Boolean = true,
    bold: Boolean = false,
    gutter: Boolean = false,
    gutterIcon: ImageVector? = null,
    checked: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Row(
        Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(RoundedRectangle(14.dp))
            .background(if (pressed) GlassColors.fill() else Color.Transparent)
            .clickable(interactionSource, null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { if (checked) selected = true }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (gutter) {
            Box(Modifier.width(22.dp)) {
                if (gutterIcon != null) GlassIcon(gutterIcon, color, Modifier.size(15.dp))
            }
        }
        if (icon != null) {
            GlassIcon(icon, iconColor ?: color, Modifier.size(19.dp))
            Spacer(Modifier.width(12.dp))
        } else {
            Spacer(Modifier.width(31.dp))
        }
        Column(Modifier.weight(1f)) {
            BasicText(title, style = TextStyle(color, 17.sp, if (bold) FontWeight.SemiBold else FontWeight.Normal))
            if (subtitle != null) {
                BasicText(subtitle, style = TextStyle(GlassColors.secondaryLabel(), 13.sp))
            }
        }
        if (trailing != null) {
            GlassIcon(trailing, GlassColors.secondaryLabel(), Modifier.size(14.dp))
        }
    }
}

/** iOS 26 popover: arbitrary content in an anchored Liquid Glass panel. */
@Composable
fun BoxScope.LiquidPopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    alignment: Alignment,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    transformOrigin: TransformOrigin = TransformOrigin(0.5f, 0f),
    content: @Composable ColumnScope.(panelBackdrop: Backdrop) -> Unit,
) {
    LiquidGlassPopup(
        visible = visible,
        onDismissRequest = onDismissRequest,
        backdrop = backdrop,
        alignment = alignment,
        modifier = modifier,
        offset = offset,
        transformOrigin = transformOrigin,
        cornerRadius = 30.dp,
        paneTitle = "Popover",
        content = content
    )
}
