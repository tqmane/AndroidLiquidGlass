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
@Composable
fun PageControlContent() {
    BackdropDemoScaffold { backdrop ->
        var page by remember { mutableIntStateOf(0) }
        var page2 by remember { mutableIntStateOf(2) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            BasicText("Page ${page + 1}", style = TextStyle(Color.White, 34.sp, FontWeight.Bold))
            LiquidPageControl(5, page, { page = it }, backdrop)
            BasicText("Touch and drag across the dots to scrub", style = TextStyle(Color.White, 13.sp))
            LiquidPageControl(8, page2, { page2 = it }, backdrop, alwaysShowPlatter = true)
        }
    }
}
