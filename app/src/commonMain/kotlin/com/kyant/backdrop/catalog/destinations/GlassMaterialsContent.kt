package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.*
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

@Composable
fun GlassMaterialsContent() {
    BackdropDemoScaffold { backdrop ->
        Column(
            Modifier.systemBarsPadding().padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MaterialTile("Regular", backdrop, Modifier.liquidGlass(backdrop, { RoundedRectangle(24.dp) }))
            MaterialTile(
                "Clear", backdrop,
                Modifier.drawBackdrop(
                    backdrop, { RoundedRectangle(24.dp) },
                    effects = { lens(16.dp.toPx(), 32.dp.toPx(), chromaticAberration = true) },
                    highlight = { Highlight.Default },
                    onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.1f)) }
                )
            )
            MaterialTile("Tinted", backdrop, Modifier.liquidGlass(backdrop, { RoundedRectangle(24.dp) }, tint = GlassColors.Blue), Color.White)
            MaterialTile("Panel (sheets, menus, alerts)", backdrop, Modifier.liquidGlassPanel(backdrop, { RoundedRectangle(24.dp) }))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Ultra thin" to 4f, "Thin" to 8f, "Regular" to 16f, "Thick" to 28f).forEach { (name, radius) ->
                    Box(
                        Modifier.weight(1f).height(72.dp).drawBackdrop(
                            backdrop, { RoundedRectangle(18.dp) },
                            effects = { vibrancy(); blur(radius.dp.toPx()) },
                            highlight = null,
                            onDrawSurface = { drawRect(Color.White.copy(alpha = radius / 60f)) }
                        ),
                        contentAlignment = Alignment.Center
                    ) { BasicText(name, style = TextStyle(Color.Black, 12.sp, FontWeight.Medium)) }
                }
            }
        }
    }
}

@Composable
private fun MaterialTile(name: String, backdrop: LayerBackdrop, glass: Modifier, color: Color = GlassColors.label()) {
    Box(Modifier.fillMaxWidth().height(84.dp).then(glass), contentAlignment = Alignment.Center) {
        BasicText(name, style = TextStyle(color, 17.sp, FontWeight.SemiBold))
    }
}
