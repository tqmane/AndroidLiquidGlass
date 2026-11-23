package com.kyant.backdrop.catalog.destinations

import androidx.compose.ui.text.style.TextAlign
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

/** A plain iOS-style page (system background + large title) whose background is recorded into a backdrop. */
@Composable
fun IosPage(
    title: String,
    grouped: Boolean = false,
    inlineTitle: Boolean = false,
    overlay: @Composable BoxScope.(backdrop: LayerBackdrop) -> Unit = {},
    content: @Composable ColumnScope.(backdrop: LayerBackdrop) -> Unit,
) {
    val isLightTheme = !isSystemInDarkTheme()
    val background = when {
        !isLightTheme -> Color.Black
        grouped -> Color(0xFFF2F2F7)
        else -> Color.White
    }
    val backdrop = rememberLayerBackdrop()
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.matchParentSize().layerBackdrop(backdrop).background(background))
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            if (inlineTitle) {
                BasicText(
                    title,
                    Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp),
                    style = TextStyle(GlassColors.label(), 17.sp, FontWeight.SemiBold, textAlign = TextAlign.Center)
                )
            } else {
                BasicText(
                    title,
                    Modifier.padding(top = 24.dp, bottom = 16.dp),
                    style = TextStyle(GlassColors.label(), 34.sp, FontWeight.Bold)
                )
            }
            content(backdrop)
        }
        overlay(backdrop)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    BasicText(
        text,
        modifier.padding(top = 20.dp, bottom = 8.dp),
        style = TextStyle(GlassColors.secondaryLabel(), 15.sp)
    )
}

@Composable
fun DemoButton(label: String, backdrop: LayerBackdrop, onClick: () -> Unit) {
    LiquidButton(onClick, backdrop) {
        BasicText(label, style = TextStyle(Color.Black, 15.sp))
    }
}
