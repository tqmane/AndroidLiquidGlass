package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop

/** Inset separator between grouped form rows (16 dp in from both edges, as in iOS 26 forms). */
@Composable
fun GroupedSeparator(modifier: Modifier = Modifier) {
    Box(modifier.padding(horizontal = 16.dp).fillMaxWidth().height(0.5.dp).background(GlassColors.separator()))
}

/**
 * A grouped-form text field row (`TextField` / `SecureField` in a SwiftUI `Form`): 17 pt text with a placeholder.
 * [imeAction] Next moves focus to the next field.
 */
@Composable
fun GroupedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    secure: Boolean = false,
    keyboardType: KeyboardType = if (secure) KeyboardType.Password else KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    showSeparator: Boolean = false,
) {
    if (showSeparator) GroupedSeparator()
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value,
        onValueChange,
        modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(GlassColors.label(), 17.sp),
        cursorBrush = SolidColor(GlassColors.accent()),
        visualTransformation = if (secure) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus() }
        ),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    BasicText(placeholder, style = TextStyle(GlassColors.secondaryLabel().copy(alpha = 0.6f), 17.sp), maxLines = 1)
                }
                inner()
            }
        }
    )
}

/** Where a [LiquidAnchoredMenu] opens from: attach it with [Modifier.liquidMenuAnchor]. */
@Stable
class LiquidMenuAnchor internal constructor() {
    internal var bounds by mutableStateOf(Rect.Zero)
}

@Composable
fun rememberLiquidMenuAnchor(): LiquidMenuAnchor = remember { LiquidMenuAnchor() }

fun Modifier.liquidMenuAnchor(anchor: LiquidMenuAnchor): Modifier =
    onGloballyPositioned { anchor.bounds = it.boundsInRoot() }

/**
 * A grouped-form menu picker row (`Picker` with `.pickerStyle(.menu)`): label, then the selection in the accent color with
 * an up/down chevron. Tapping calls [onClick]; open a [LiquidAnchoredMenu] on the same [anchor] to pick.
 */
@Composable
fun GroupedPickerCell(
    label: String,
    value: String,
    anchor: LiquidMenuAnchor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSeparator: Boolean = false,
) {
    if (showSeparator) GroupedSeparator()
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(role = Role.DropdownList, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(label, Modifier.weight(1f), style = TextStyle(GlassColors.label(), 17.sp), maxLines = 1)
        Row(Modifier.liquidMenuAnchor(anchor), verticalAlignment = Alignment.CenterVertically) {
            BasicText(value, style = TextStyle(GlassColors.accent(), 17.sp), maxLines = 1)
            Spacer(Modifier.width(4.dp))
            GlassIcon(GlassIcons.ChevronUpDown, GlassColors.accent(), Modifier.size(15.dp))
        }
    }
}

/** A multi-line explanatory row in a grouped form, like the footer text cells of a settings screen. */
@Composable
fun GroupedNoteCell(text: String, modifier: Modifier = Modifier, showSeparator: Boolean = false) {
    if (showSeparator) GroupedSeparator()
    BasicText(
        text,
        modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 16.dp, vertical = 12.dp),
        style = TextStyle(GlassColors.label(), 15.sp, lineHeight = 20.sp)
    )
}

/**
 * [LiquidMenu] that opens from an [anchor] (e.g. a [GroupedPickerCell] value) instead of a fixed alignment: below it,
 * right-aligned, or above it when the anchor sits in the lower half of this box.
 */
@Composable
fun BoxScope.LiquidAnchoredMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    anchor: LiquidMenuAnchor,
    entries: List<MenuEntry>,
    modifier: Modifier = Modifier,
    width: Dp = 250.dp,
    icons: LiquidMenuIcons = LiquidMenuIcons(),
) {
    var hostOrigin by remember { mutableStateOf(Offset.Zero) }
    var hostSize by remember { mutableStateOf(IntSize.Zero) }
    Box(Modifier.matchParentSize().onGloballyPositioned { hostOrigin = it.positionInRoot(); hostSize = it.size })
    val density = LocalDensity.current
    val b = anchor.bounds.translate(-hostOrigin)
    val gap = with(density) { 8.dp.toPx() }
    val menuWidth = with(density) { width.toPx() }
    val below = b.center.y < hostSize.height / 2f
    val x = (b.right - menuWidth).coerceIn(gap, (hostSize.width - menuWidth - gap).coerceAtLeast(gap))
    val y = if (below) b.bottom + gap else -(hostSize.height - b.top + gap)
    LiquidMenu(
        expanded, onDismissRequest, backdrop, entries,
        alignment = if (below) Alignment.TopStart else Alignment.BottomStart,
        modifier = modifier,
        offset = with(density) { DpOffset(x.toDp(), y.toDp()) },
        transformOrigin = TransformOrigin(((b.right - x) / menuWidth).coerceIn(0f, 1f), if (below) 0f else 1f),
        width = width,
        icons = icons
    )
}
