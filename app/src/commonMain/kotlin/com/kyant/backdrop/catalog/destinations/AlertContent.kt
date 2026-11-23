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
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.Rect
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.*
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
@Composable
fun AlertContent() {
    BackdropDemoScaffold { backdrop ->
        var two by remember { mutableStateOf(false) }
        var three by remember { mutableStateOf(false) }
        var one by remember { mutableStateOf(false) }
        var sheet by remember { mutableStateOf(false) }
        var rename by remember { mutableStateOf(false) }
        var login by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        var user by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var sheetAnchor by remember { mutableStateOf<Rect?>(null) }
        // Scrolls and clears the scaffold's bottom "Pick an image" button on short (landscape) windows.
        Column(
            Modifier.verticalScroll(rememberScrollState()).safeDrawingPadding().padding(vertical = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DemoButton("Alert, one action", backdrop) { one = true }
            DemoButton("Alert, two actions", backdrop) { two = true }
            DemoButton("Alert, three actions", backdrop) { three = true }
            DemoButton("Alert, text field", backdrop) { rename = true }
            DemoButton("Alert, login", backdrop) { login = true }
            Box(Modifier.contextMenuAnchor { sheetAnchor = it }) { DemoButton("Action sheet", backdrop) { sheet = true } }
        }
        LiquidAlert(one, { one = false }, backdrop, "Saved", "Your changes were saved.", listOf(AlertAction("OK", AlertActionStyle.Preferred)))
        LiquidAlert(
            two, { two = false }, backdrop, "Turn on notifications?", "Get notified when new episodes are available.",
            listOf(AlertAction("Not Now", AlertActionStyle.Cancel), AlertAction("Turn On", AlertActionStyle.Preferred))
        )
        LiquidAlert(
            three, { three = false }, backdrop, "Delete this item?", "This action cannot be undone.",
            listOf(AlertAction("Delete", AlertActionStyle.Destructive), AlertAction("Archive"), AlertAction("Cancel", AlertActionStyle.Cancel))
        )
        LiquidAlert(
            rename, { rename = false }, backdrop, "New Album", "Enter a name for this album.",
            listOf(AlertAction("Cancel", AlertActionStyle.Cancel), AlertAction("Save", AlertActionStyle.Preferred)),
            textFields = listOf(AlertTextField(name, { name = it }, "Name"))
        )
        LiquidAlert(
            login, { login = false }, backdrop, "Sign In", "Enter your username and password.",
            listOf(AlertAction("Cancel", AlertActionStyle.Cancel), AlertAction("Sign In", AlertActionStyle.Preferred)),
            textFields = listOf(AlertTextField(user, { user = it }, "Username"), AlertTextField(password, { password = it }, "Password", secure = true))
        )
        LiquidActionSheet(
            sheet, { sheet = false }, backdrop, "Photo", "Choose what to do with this photo.",
            listOf(AlertAction("Share"), AlertAction("Duplicate"), AlertAction("Delete", AlertActionStyle.Destructive), AlertAction("Cancel", AlertActionStyle.Cancel)),
            anchorBounds = sheetAnchor
        )
    }
}
