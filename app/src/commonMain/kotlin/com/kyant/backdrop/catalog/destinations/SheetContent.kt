package com.kyant.backdrop.catalog.destinations

import com.kyant.backdrop.catalog.components.LiquidGlassTextButton
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.kyant.backdrop.catalog.components.GroupedNoteCell
import com.kyant.backdrop.catalog.components.GroupedPickerCell
import com.kyant.backdrop.catalog.components.GroupedTextField
import com.kyant.backdrop.catalog.components.LiquidAnchoredMenu
import com.kyant.backdrop.catalog.components.MenuEntry
import com.kyant.backdrop.catalog.components.SheetLargeTitle
import com.kyant.backdrop.catalog.components.FormSectionTitleStyle
import com.kyant.backdrop.catalog.components.topEdgeFade
import androidx.compose.ui.platform.LocalDensity
import com.kyant.backdrop.catalog.components.rememberLiquidMenuAnchor
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
    var connection by remember { mutableStateOf(false) }
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
            LiquidSheet(connection, { connection = false }, backdrop, detents = listOf(SheetDetent.Large), showGrabber = false, title = "Connection") {
                ConnectionBody { connection = false }
            }
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
            GroupedCell("Form sheet with fields", labelColor = GlassColors.accent(), showSeparator = true, onClick = { connection = true })
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
        // Long enough to overflow the large detent, so the list scrolls there (at the medium detent a drag expands the sheet instead).
        sheet.SheetSection("Albums") {
            AlbumNames.forEachIndexed { i, name -> GroupedCell(name, icon = GlassIcons.Folder, showSeparator = i > 0) }
        }
    }
}

private val AlbumNames = listOf(
    "Beach day", "Birthday", "Camping", "City lights", "Coffee", "Concerts", "Family", "Food", "Garden",
    "Hiking", "Holidays", "Museums", "Night sky", "Pets", "Road trip", "Snow", "Sunsets", "Weddings",
)

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

/** A form in a sheet, like a SwiftUI `NavigationStack { Form { TextField / Picker / SecureField } }` with Cancel and Save. */
@Composable
private fun LiquidSheetScope.ConnectionBody(onClose: () -> Unit) {
    val sheet = this
    val scroll = rememberScrollState()
    var name by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("WebDAV") }
    var transport by remember { mutableStateOf("HTTPS (recommended)") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("/") }
    var menu by remember { mutableStateOf<String?>(null) }
    val protocolAnchor = rememberLiquidMenuAnchor()
    val transportAnchor = rememberLiquidMenuAnchor()
    Box(Modifier.weight(1f, fill = false)) {
        Column(
            Modifier.topEdgeFade(57.dp).verticalScroll(scroll).imePadding().padding(top = 57.dp, bottom = 24.dp)
        ) {
            SheetLargeTitle("Connection")
            sheet.SheetSection("Connection settings", titleStyle = FormSectionTitleStyle) {
                GroupedTextField(name, { name = it }, "Connection name")
                GroupedPickerCell("Protocol", protocol, protocolAnchor, { menu = "protocol" }, showSeparator = true)
                GroupedPickerCell("Transport", transport, transportAnchor, { menu = "transport" }, showSeparator = true)
                GroupedTextField(host, { host = it }, "Host", keyboardType = KeyboardType.Uri, showSeparator = true)
                GroupedTextField(port, { port = it.filter(Char::isDigit) }, "Port", keyboardType = KeyboardType.Number, showSeparator = true)
                GroupedTextField(user, { user = it }, "Username", showSeparator = true)
                GroupedTextField(password, { password = it }, "Password", secure = true, showSeparator = true)
                GroupedTextField(path, { path = it }, "Start directory", imeAction = ImeAction.Done, showSeparator = true)
            }
            Spacer(Modifier.height(34.dp))
            sheet.SheetSection(null) {
                GroupedNoteCell("Enter only the server name as the host, without the transport or any path. Enter the public WebDAV path as the start directory, such as /remote.php/dav/files/username. If the server uses a different port, replace the suggested port with the one supplied by your provider.")
                GroupedNoteCell("New videos are sent while they are received, with a memory limit. The whole video is not stored on this device. Retrying after an interruption starts from the beginning. If your server requires a username and password, use the credentials issued for WebDAV access.", showSeparator = true)
            }
        }
        sheet.LiquidSheetHeader(
            "Connection",
            leading = { LiquidGlassTextButton("Cancel", onClose, sheet.backdrop) },
            trailing = { LiquidGlassTextButton("Save", onClose, sheet.backdrop) },
            showTitle = scroll.value > with(LocalDensity.current) { 44.dp.toPx() }
        )
        LiquidAnchoredMenu(
            menu == "protocol", { menu = null }, sheet.backdrop, protocolAnchor,
            listOf("WebDAV", "SMB", "FTP").map { MenuEntry.Action(it, checked = it == protocol) { protocol = it } }
        )
        LiquidAnchoredMenu(
            menu == "transport", { menu = null }, sheet.backdrop, transportAnchor,
            listOf("HTTPS (recommended)", "HTTP").map { MenuEntry.Action(it, checked = it == transport) { transport = it } }
        )
    }
}
