package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.shapes.Capsule

enum class BorderedButtonSize(val height: Int, val horizontalPadding: Int, val fontSize: Int) {
    Small(28, 10, 15),
    Medium(34, 14, 15),
    Large(50, 20, 17),
}

/** iOS 26 `.bordered` / `.borderedProminent` capsule button (tinted fill, no glass). [neutral] gives the grey fill and label-colored text of an untinted `.bordered` button (iPad form-sheet Cancel). */
@Composable
fun LiquidBorderedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
    destructive: Boolean = false,
    neutral: Boolean = false,
    size: BorderedButtonSize = BorderedButtonSize.Medium,
    enabled: Boolean = true,
) {
    val tint = if (destructive) GlassColors.red() else if (neutral) GlassColors.label() else GlassColors.accent()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, spring(0.7f, 600f))
    Box(
        modifier
            .graphicsLayer {
                val s = 1f - 0.04f * press
                scaleX = s
                scaleY = s
                alpha = if (enabled) 1f - 0.25f * press else 0.4f
            }
            .defaultMinSize(minHeight = size.height.dp)
            .clip(Capsule())
            .background(if (prominent) tint else if (neutral) GlassColors.fill() else tint.copy(alpha = 0.15f))
            .clickable(interaction, null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = size.horizontalPadding.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(label, style = TextStyle(if (prominent) Color.White else tint, size.fontSize.sp))
    }
}
