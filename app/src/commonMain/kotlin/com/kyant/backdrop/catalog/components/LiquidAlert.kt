package com.kyant.backdrop.catalog.components

import com.kyant.backdrop.catalog.utils.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

/**
 * iOS 26 `UIAlertController` (`.alert`): a Liquid Glass card with capsule action buttons;
 * two actions sit side by side, more stack vertically.
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
                    val s = lerp(1.15f, 1f, p)
                    scaleX = s
                    scaleY = s
                    alpha = p.coerceIn(0f, 1f)
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
) {
    BackHandler(visible) { onDismissRequest() }
    Box(Modifier.matchParentSize().zIndex(1f)) {
        val progress = rememberPresentation(visible) ?: return
        val p = progress.value
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.2f * p.coerceIn(0f, 1f)))
                .clickable(interactionSource = null, indication = null, onClick = onDismissRequest)
        )
        Column(
            modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(10.dp)
                .fillMaxWidth()
                .semantics {
                    paneTitle = title ?: "Actions"
                    dismiss { onDismissRequest(); true }
                }
                .graphicsLayer {
                    translationY = (1f - p) * size.height
                    alpha = p.coerceIn(0f, 1f)
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
                actions.forEach { AlertButton(it, Modifier.fillMaxWidth(), onAction) }
            }
        }
    }
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
internal fun rememberPresentation(visible: Boolean): Animatable<Float, *>? {
    val progress = remember { Animatable(0f) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            shown = true
            progress.animateTo(1f, spring(0.78f, 380f))
        } else {
            progress.animateTo(0f, spring(1f, 600f))
            shown = false
        }
    }
    return if (shown || visible) progress else null
}
