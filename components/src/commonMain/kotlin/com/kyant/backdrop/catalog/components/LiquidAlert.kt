package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

enum class AlertActionStyle { Default, Cancel, Destructive, Preferred }

data class AlertAction(
    val title: String,
    val style: AlertActionStyle = AlertActionStyle.Default,
    val onClick: () -> Unit = {},
)

/** A text field in a [LiquidAlert] (`UIAlertController.addTextField`). [secure] hides the input like a password field. */
data class AlertTextField(
    val value: String,
    val onValueChange: (String) -> Unit,
    val placeholder: String = "",
    val secure: Boolean = false,
    val keyboardType: KeyboardType = if (secure) KeyboardType.Password else KeyboardType.Text,
)

/**
 * iOS 26 `UIAlertController` (`.alert`): a Liquid Glass card with capsule action buttons;
 * two actions sit side by side, more stack vertically. [textFields] adds input fields under the message;
 * the first one is focused on presentation and the card stays centered above the keyboard.
 */
@Composable
fun BoxScope.LiquidAlert(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    title: String,
    message: String?,
    actions: List<AlertAction>,
    modifier: Modifier = Modifier,
    textFields: List<AlertTextField> = emptyList(),
) {
    BackHandler(visible) { onDismissRequest() }
    Box(Modifier.matchParentSize().zIndex(1f)) {
        val progress = rememberPresentation(visible) ?: return
        val p = progress.value
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.2f * p.coerceIn(0f, 1f)))
                .clickable(interactionSource = null, indication = null) {}
        )
        Box(Modifier.matchParentSize().imePadding()) {
        Column(
            modifier
                .align(Alignment.Center)
                .padding(horizontal = 36.dp)
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .semantics {
                    paneTitle = title
                    dismiss { onDismissRequest(); true }
                }
                .graphicsLayer {
                    // iOS 26: shrinks in from 1.08 while fading in, and shrinks slightly away while fading out.
                    val s = if (visible) lerp(1.08f, 1f, p) else lerp(0.94f, 1f, p)
                    scaleX = s
                    scaleY = s
                    alpha = p.coerceIn(0f, 1f)
                    // Per-draw alpha: an offscreen alpha layer drops the glass outline clip and flashes a rectangle.
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .liquidGlassPanel(backdrop, { RoundedRectangle(34.dp) })
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 18.dp)) {
                BasicText(
                    title,
                    Modifier.fillMaxWidth(),
                    style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold, textAlign = TextAlign.Start)
                )
                if (message != null) {
                    Spacer(Modifier.height(6.dp))
                    BasicText(
                        message,
                        Modifier.fillMaxWidth(),
                        style = TextStyle(GlassColors.label().copy(alpha = 0.8f), 15.sp, textAlign = TextAlign.Start)
                    )
                }
            }
            if (textFields.isNotEmpty()) {
                AlertTextFields(textFields, visible)
                Spacer(Modifier.height(16.dp))
            }
            val onAction: (AlertAction) -> Unit = { it.onClick(); onDismissRequest() }
            if (actions.size == 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    actions.forEach { AlertButton(it, Modifier.weight(1f), onAction) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    actions.forEach { AlertButton(it, Modifier.fillMaxWidth(), onAction) }
                }
            }
        }
        }
    }
}

@Composable
private fun AlertTextFields(fields: List<AlertTextField>, visible: Boolean) {
    val first = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(visible) { if (visible) first.requestFocus() else focusManager.clearFocus() }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        fields.forEachIndexed { i, field ->
            val last = i == fields.lastIndex
            BasicTextField(
                field.value,
                field.onValueChange,
                Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(first) else Modifier),
                singleLine = true,
                textStyle = TextStyle(GlassColors.label(), 17.sp),
                cursorBrush = SolidColor(GlassColors.accent()),
                visualTransformation = if (field.secure) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = field.keyboardType,
                    imeAction = if (last) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                decorationBox = { inner ->
                    Box(
                        Modifier.fillMaxWidth().height(44.dp).clip(Capsule()).background(GlassColors.fill())
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (field.value.isEmpty()) {
                            BasicText(field.placeholder, style = TextStyle(GlassColors.secondaryLabel(), 17.sp), maxLines = 1)
                        }
                        inner()
                    }
                }
            )
        }
    }
}

/**
 * iOS 26 action sheet (`UIAlertController` `.actionSheet` / `confirmationDialog`): a glass panel
 * floating above the bottom edge with a header, stacked actions and a separate cancel button.
 */
@Composable
fun BoxScope.LiquidActionSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    title: String?,
    message: String?,
    actions: List<AlertAction>,
    modifier: Modifier = Modifier,
    anchorBounds: Rect? = null,
) {
    BackHandler(visible) { onDismissRequest() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    BoxWithConstraints(
        Modifier.matchParentSize().zIndex(1f).onGloballyPositioned { origin = it.positionInRoot() }
    ) {
        val regular = maxWidth >= 600.dp
        // iOS 26 grows an iPad popover dialog out of its source evenly over ~0.25 s, without overshoot.
        val progress = rememberPresentation(
            visible,
            if (regular && anchorBounds != null) spring(1f, 220f) else spring(0.78f, 380f)
        ) ?: return@BoxWithConstraints
        val p = progress.value
        val density = LocalDensity.current
        // iPad: a popover above (or below) its source with an arrow pointing at it; centered when there is no source.
        val anchored = regular && anchorBounds != null && cardSize != IntSize.Zero
        val gap = with(density) { ActionSheetArrow.height.toPx() + 4.dp.toPx() }
        val margin = with(density) { 16.dp.toPx() }
        val anchor = anchorBounds?.translate(-origin.x, -origin.y)
        val maxH = constraints.maxHeight.toFloat()
        val spaceAbove = if (anchor == null) 0f else anchor.top - gap - margin
        val spaceBelow = if (anchor == null) 0f else maxH - anchor.bottom - gap - margin
        val above = anchor != null && (cardSize.height <= spaceAbove || (cardSize.height > spaceBelow && spaceAbove > spaceBelow))
        val cardX = if (anchor == null) 0f else
            (anchor.center.x - cardSize.width / 2f).coerceIn(margin, maxOf(margin, constraints.maxWidth - cardSize.width - margin))
        val idealY = if (anchor == null) 0f else if (above) anchor.top - gap - cardSize.height else anchor.bottom + gap
        // When neither side fits, keep every action on screen; the arrow is dropped because it no longer touches the card.
        val cardY = idealY.coerceIn(margin, maxOf(margin, maxH - cardSize.height - margin))
        val arrowColor = if (isGlassDark()) Color(0xFF3A3A3C) else Color(0xFFF2F2F7)
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.2f * p.coerceIn(0f, 1f)))
                .clickable(interactionSource = null, indication = null, onClick = onDismissRequest)
        )
        if (anchored && anchor != null && cardY == idealY) {
            Box(
                Modifier
                    .offset {
                        val w = ActionSheetArrow.width.roundToPx()
                        val h = ActionSheetArrow.height.roundToPx()
                        IntOffset(
                            (anchor.center.x - w / 2f).roundToInt(),
                            (if (above) anchor.top - gap else anchor.bottom + gap - h).roundToInt()
                        )
                    }
                    .size(ActionSheetArrow)
                    .graphicsLayer { alpha = p.coerceIn(0f, 1f); compositingStrategy = CompositingStrategy.ModulateAlpha; rotationZ = if (above) 0f else 180f }
                    .background(arrowColor, ArrowShape)
            )
        }
        Column(
            modifier
                .then(
                    when {
                        anchored -> Modifier.align(Alignment.TopStart).offset { IntOffset(cardX.roundToInt(), cardY.roundToInt()) }
                        regular -> Modifier.align(Alignment.Center)
                        // iOS 26 iPhone: the action sheet is a centered card, not docked to the bottom edge.
                        else -> Modifier.align(Alignment.Center).padding(horizontal = 20.dp)
                    }
                )
                .then(if (regular) Modifier.width(280.dp) else Modifier.fillMaxWidth())
                .onSizeChanged { cardSize = it }
                .semantics {
                    paneTitle = title ?: "Actions"
                    dismiss { onDismissRequest(); true }
                }
                .graphicsLayer {
                    // iOS 26: an anchored dialog grows out of its anchor (~0.45 -> 1); the others shrink in from
                    // slightly larger like an alert. All shrink a little while fading out.
                    val s = when {
                        !visible -> lerp(0.94f, 1f, p)
                        anchored -> lerp(0.45f, 1f, p)
                        regular -> lerp(1.08f, 1f, p)
                        else -> lerp(1.1f, 1f, p)
                    }
                    scaleX = s; scaleY = s
                    if (anchored) transformOrigin = TransformOrigin(0.5f, if (above) 1f else 0f)
                    alpha = p.coerceIn(0f, 1f)
                    // Per-draw alpha: an offscreen alpha layer drops the glass outline clip and flashes a rectangle.
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .liquidGlassPanel(backdrop, { RoundedRectangle(34.dp) })
                .padding(16.dp)
        ) {
            if (title != null || message != null) {
                Column(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 14.dp)) {
                    if (title != null) {
                        BasicText(title, Modifier.fillMaxWidth(), style = TextStyle(GlassColors.label(), 15.sp, FontWeight.SemiBold, textAlign = TextAlign.Start))
                    }
                    if (message != null) {
                        Spacer(Modifier.height(2.dp))
                        BasicText(message, Modifier.fillMaxWidth(), style = TextStyle(GlassColors.secondaryLabel(), 13.sp, textAlign = TextAlign.Start))
                    }
                }
            }
            val onAction: (AlertAction) -> Unit = { it.onClick(); onDismissRequest() }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                actions.filter { !regular || it.style != AlertActionStyle.Cancel }.forEach { AlertButton(it, Modifier.fillMaxWidth(), onAction) }
            }
        }
    }
}

private val ActionSheetArrow = DpSize(26.dp, 12.dp)

private val ArrowShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

@Composable
private fun AlertButton(action: AlertAction, modifier: Modifier, onAction: (AlertAction) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val preferred = action.style == AlertActionStyle.Preferred
    val background = if (preferred) GlassColors.accent() else GlassColors.fill()
    val textColor = when (action.style) {
        AlertActionStyle.Preferred -> Color.White
        AlertActionStyle.Destructive -> GlassColors.destructive()
        else -> GlassColors.label()
    }
    Box(
        modifier
            .height(48.dp)
            .graphicsLayer {
                val s = if (pressed) 1.04f else 1f
                scaleX = s
                scaleY = s
            }
            .clip(Capsule())
            .background(background)
            .then(if (pressed) Modifier.background(Color.White.copy(alpha = 0.15f)) else Modifier)
            .clickable(interactionSource, null, role = Role.Button) { onAction(action) },
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            action.title,
            Modifier.padding(horizontal = 16.dp),
            style = TextStyle(
                textColor,
                17.sp,
                if (action.style == AlertActionStyle.Cancel || preferred) FontWeight.SemiBold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
internal fun rememberPresentation(
    visible: Boolean,
    presentSpec: AnimationSpec<Float> = spring(0.78f, 380f)
): Animatable<Float, *>? {
    val progress = remember { Animatable(0f) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            shown = true
            progress.animateTo(1f, presentSpec)
        } else {
            // iOS 26 dismissals take ~0.12 s.
            progress.animateTo(0f, spring(1f, 900f))
            shown = false
        }
    }
    return if (shown || visible) progress else null
}
