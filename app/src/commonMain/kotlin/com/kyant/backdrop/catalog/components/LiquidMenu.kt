package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle

/** An entry of a [LiquidMenu] / [LiquidContextMenu]. */
sealed interface MenuEntry {
    data class Action(
        val title: String,
        val icon: ImageVector? = null,
        val destructive: Boolean = false,
        val checked: Boolean = false,
        val enabled: Boolean = true,
        val subtitle: String? = null,
        val onClick: () -> Unit = {},
    ) : MenuEntry

    data class Submenu(
        val title: String,
        val icon: ImageVector? = null,
        val entries: List<MenuEntry>,
    ) : MenuEntry

    data object Divider : MenuEntry
}

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
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(visible) { onDismissRequest() }
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
                }
                .liquidGlassPanel(backdrop, { RoundedRectangle(cornerRadius) })
                .clickable(interactionSource = null, indication = null) {},
            content = content
        )
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
        Crossfade(submenu) { open ->
            Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                if (open == null && header != null) {
                    BasicText(header, Modifier.padding(horizontal = 20.dp, vertical = 6.dp), style = TextStyle(GlassColors.secondaryLabel(), 13.sp))
                }
                if (open == null) {
                    MenuEntries(entries, onDismissRequest) { submenu = it }
                } else {
                    MenuRow(
                        title = open.title,
                        icon = GlassIcons.ChevronDown,
                        color = GlassColors.label(),
                        bold = true,
                        onClick = { submenu = null }
                    )
                    MenuDivider()
                    MenuEntries(open.entries, onDismissRequest) { submenu = it }
                }
            }
        }
    }
}

@Composable
internal fun MenuEntries(
    entries: List<MenuEntry>,
    onDismissRequest: () -> Unit,
    onOpenSubmenu: (MenuEntry.Submenu) -> Unit,
) {
    entries.forEach { entry ->
        when (entry) {
            is MenuEntry.Action -> MenuRow(
                title = entry.title,
                subtitle = entry.subtitle,
                icon = if (entry.checked) GlassIcons.Check else entry.icon,
                color = when {
                    entry.destructive -> GlassColors.destructive()
                    else -> GlassColors.label()
                }.let { if (entry.enabled) it else it.copy(alpha = 0.3f) },
                enabled = entry.enabled,
                onClick = {
                    entry.onClick()
                    onDismissRequest()
                }
            )

            is MenuEntry.Submenu -> MenuRow(
                title = entry.title,
                icon = entry.icon,
                trailing = GlassIcons.ChevronRight,
                color = GlassColors.label(),
                onClick = { onOpenSubmenu(entry) }
            )

            MenuEntry.Divider -> MenuDivider()
        }
    }
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
    subtitle: String? = null,
    trailing: ImageVector? = null,
    enabled: Boolean = true,
    bold: Boolean = false,
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            GlassIcon(icon, color, Modifier.size(19.dp))
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
    content: @Composable ColumnScope.() -> Unit,
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
