package com.kyant.backdrop.catalog.destinations

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kyant.backdrop.catalog.components.LiquidGlassEffectContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.GlassColors
import com.kyant.backdrop.catalog.components.GlassIcons
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidBorderedButton
import com.kyant.backdrop.catalog.components.BorderedButtonSize
import com.kyant.backdrop.catalog.components.LiquidButtonGroup
import com.kyant.backdrop.catalog.components.LiquidGroupItem
import com.kyant.backdrop.catalog.components.LiquidIconButton
import androidx.compose.foundation.layout.Row

@Composable
fun ButtonsContent() {
    BackdropDemoScaffold { backdrop ->
        Column(
            Modifier.verticalScroll(rememberScrollState()).systemBarsPadding().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            LiquidButton(
                {},
                backdrop
            ) {
                BasicText(
                    "Transparent Liquid Button",
                    style = TextStyle(Color.Black, 15f.sp)
                )
            }
            LiquidButton(
                {},
                backdrop,
                surfaceColor = Color.White.copy(0.3f)
            ) {
                BasicText(
                    "Surface Liquid Button",
                    style = TextStyle(Color.Black, 15f.sp)
                )
            }
            LiquidButton(
                {},
                backdrop,
                tint = Color(0xFF0088FF)
            ) {
                BasicText(
                    "Tinted Liquid Button",
                    style = TextStyle(Color.White, 15f.sp)
                )
            }
            LiquidButton(
                {},
                backdrop,
                tint = Color(0xFFFF8D28)
            ) {
                BasicText(
                    "Tinted Liquid Button",
                    style = TextStyle(Color.White, 15f.sp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
                LiquidIconButton(GlassIcons.ChevronLeft, "Back", {}, backdrop)
                LiquidIconButton(GlassIcons.Ellipsis, "More", {}, backdrop)
                LiquidIconButton(GlassIcons.Plus, "Add", {}, backdrop, tint = GlassColors.Blue)
                LiquidIconButton(GlassIcons.Play, "Play", {}, backdrop, size = 56f.dp, iconSize = 24f.dp, tint = Color(0xFFFF8D28))
            }
            // GlassEffectContainer(spacing: 20) with 8 dp gaps: the circles bridge at rest and stick harder when held.
            LiquidGlassEffectContainer(backdrop) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidIconButton(GlassIcons.Bookmark, "Bookmark", {}, backdrop, size = 56.dp, iconSize = 24.dp, iconColor = Color.Black)
                    LiquidIconButton(GlassIcons.Heart, "Favorite", {}, backdrop, size = 56.dp, iconSize = 24.dp, iconColor = Color.Black)
                    LiquidIconButton(GlassIcons.Share, "Share", {}, backdrop, size = 56.dp, iconSize = 24.dp, iconColor = Color.Black)
                }
            }
            LiquidButtonGroup(backdrop) {
                LiquidGroupItem(GlassIcons.Share, "Share", {}, color = Color.Black)
                LiquidGroupItem(GlassIcons.Heart, "Favorite", {}, color = Color.Black)
                LiquidGroupItem(GlassIcons.Trash, "Delete", {}, color = Color.Black)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
                LiquidBorderedButton("Bordered", {})
                LiquidBorderedButton("Prominent", {}, prominent = true)
                LiquidBorderedButton("Delete", {}, destructive = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12f.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                LiquidBorderedButton("Small", {}, size = BorderedButtonSize.Small, prominent = true)
                LiquidBorderedButton("Medium", {}, size = BorderedButtonSize.Medium, prominent = true)
                LiquidBorderedButton("Large", {}, size = BorderedButtonSize.Large, prominent = true)
            }
        }
    }
}
