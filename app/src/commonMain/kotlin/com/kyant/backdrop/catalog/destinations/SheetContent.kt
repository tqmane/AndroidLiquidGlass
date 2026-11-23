package com.kyant.backdrop.catalog.destinations

import com.kyant.backdrop.catalog.components.LiquidGlassTextButton
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.catalog.components.GlassColors
import com.kyant.backdrop.catalog.components.GlassIcons
import com.kyant.backdrop.catalog.components.GroupedCell
import com.kyant.backdrop.catalog.components.GroupedSection
import com.kyant.backdrop.catalog.components.LiquidIconButton
import com.kyant.backdrop.catalog.components.LiquidSheet
import com.kyant.backdrop.catalog.components.LiquidSheetHeader
import com.kyant.backdrop.catalog.components.LiquidSheetScope
import com.kyant.backdrop.catalog.components.SheetDetent
import com.kyant.backdrop.catalog.components.SheetSection

/** iOS 26 sheets: a title-only "Browse" sheet (medium/large), a toolbar sheet with cancel and prominent done buttons, and single-detent variants. */
@Composable
fun SheetContent() {
    var browse by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf(false) }
    var mediumOnly by remember { mutableStateOf(false) }
    var largeOnly by remember { mutableStateOf(false) }
    var noGrabber by remember { mutableStateOf(false) }
    val pageCell = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color.White

    IosPage(
        "Sheets",
        grouped = true,
        inlineTitle = true,
        overlay = { backdrop ->
            LiquidSheet(browse, { browse = false }, backdrop, title = "Browse") { BrowseBody() }
            LiquidSheet(edit, { edit = false }, backdrop, title = "Edit collection") { EditCollectionBody { edit = false } }
            LiquidSheet(mediumOnly, { mediumOnly = false }, backdrop, detents = listOf(SheetDetent.Medium), title = "Browse") { BrowseBody() }
            LiquidSheet(largeOnly, { largeOnly = false }, backdrop, detents = listOf(SheetDetent.Large), title = "Browse") { BrowseBody() }
            LiquidSheet(noGrabber, { noGrabber = false }, backdrop, showGrabber = false, title = "Edit collection") {
                EditCollectionBody { noGrabber = false }
            }
        }
    ) {
        GroupedSection(null, pageCell, horizontalPadding = 0.dp) {
            GroupedCell("Open sheet", labelColor = GlassColors.accent(), onClick = { browse = true })
            GroupedCell("Open toolbar sheet", labelColor = GlassColors.accent(), showSeparator = true, onClick = { edit = true })
            GroupedCell("Medium-only sheet", labelColor = GlassColors.accent(), showSeparator = true, onClick = { mediumOnly = true })
            GroupedCell("Large-only sheet", labelColor = GlassColors.accent(), showSeparator = true, onClick = { largeOnly = true })
            GroupedCell("Sheet without grabber", labelColor = GlassColors.accent(), showSeparator = true, onClick = { noGrabber = true })
        }
        GroupedSection("Library", pageCell, Modifier.padding(top = 14.dp), horizontalPadding = 0.dp) {
            GroupedCell("Recently added", icon = GlassIcons.Clock)
            GroupedCell("Downloads", icon = GlassIcons.ArrowDownCircle, showSeparator = true)
            GroupedCell("Favorites", icon = GlassIcons.Heart, showSeparator = true)
        }
    }
}

@Composable
private fun LiquidSheetScope.BrowseBody() {
    val sheet = this
    LiquidSheetHeader("Browse")
    Column(
        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        sheet.SheetSection("Library") {
            GroupedCell("Recently added", icon = GlassIcons.Clock)
            GroupedCell("Downloads", icon = GlassIcons.ArrowDownCircle, showSeparator = true)
            GroupedCell("Favorites", icon = GlassIcons.Heart, showSeparator = true)
        }
        sheet.SheetSection("Collections") {
            GroupedCell("Weekend plans", icon = GlassIcons.Folder)
            GroupedCell("Travel ideas", icon = GlassIcons.Airplane, showSeparator = true)
            GroupedCell("Recipes", icon = GlassIcons.Book, showSeparator = true)
        }
    }
}

@Composable
private fun LiquidSheetScope.EditCollectionBody(onClose: () -> Unit) {
    val sheet = this
    LiquidSheetHeader(
        "Edit collection",
        leading = {
            if (isFormSheet) LiquidGlassTextButton("Cancel", onClose, backdrop)
            else LiquidIconButton(GlassIcons.Close, "Cancel", onClose, backdrop)
        },
        trailing = {
            if (isFormSheet) LiquidGlassTextButton("Done", onClose, backdrop, tint = GlassColors.accent())
            else LiquidIconButton(GlassIcons.Check, "Done", onClose, backdrop, tint = GlassColors.accent())
        }
    )
    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        sheet.SheetSection("Details") {
            GroupedCell("Name", value = "Weekend plans")
            GroupedCell("Location", value = "On this iPhone", showSeparator = true)
        }
    }
}
