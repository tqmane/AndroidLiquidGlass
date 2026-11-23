package com.kyant.backdrop.catalog.components

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import androidx.compose.foundation.text.BasicText

/**
 * Makes the whole capsule the field's hit target. A tap detector rather than `clickable`, so the capsule never
 * takes keyboard focus itself, and it shows the keyboard for a field that kept focus after the keyboard was hidden.
 */
private fun Modifier.focusOnTap(focusRequester: FocusRequester): Modifier = composed {
    val keyboard = LocalSoftwareKeyboardController.current
    pointerInput(focusRequester) {
        detectTapGestures {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
}

/** Icons for the search field and its focused close button. */
data class LiquidSearchBarIcons(
    val search: ImageVector = GlassIcons.Search,
    val clear: ImageVector = GlassIcons.Close,
    val microphone: ImageVector = GlassIcons.Mic,
    val close: ImageVector = GlassIcons.Close,
)

@Composable
fun LiquidSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: (String) -> Unit = {},
) = LiquidSearchBar(query, onQueryChange, backdrop, modifier, placeholder, LiquidSearchBarIcons(), onSearch)

/**
 * iOS 26 search field: the bottom-placed Liquid Glass capsule used by `.searchable` and the
 * search tab. Focusing it reveals a glass close button that clears and dismisses the search.
 */
@Composable
fun LiquidSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    icons: LiquidSearchBarIcons,
    onSearch: (String) -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    // The whole capsule is the hit target, as on iOS, not just the text field inside it.
    val focusRequester = remember { FocusRequester() }
    val secondary = GlassColors.secondaryLabel()
    // The × circle sticks to the field while held (it swells across the 8 dp gap).
    LiquidGlassEffectContainer(backdrop, modifier, spacing = 8.dp) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .weight(1f)
                .liquidGlassEffectMember()
                .liquidGlass(backdrop, { Capsule() })
                .height(48.dp)
                .focusOnTap(focusRequester)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIcon(icons.search, if (focused) GlassColors.label() else secondary, Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    // Read out by the field itself, so accessibility services focus the editable node.
                    BasicText(placeholder, Modifier.clearAndSetSemantics {}, style = TextStyle(secondary, 17.sp))
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focused = it.isFocused }
                        .semantics { if (query.isEmpty()) contentDescription = placeholder },
                    singleLine = true,
                    textStyle = TextStyle(GlassColors.label(), 17.sp),
                    cursorBrush = SolidColor(GlassColors.accent()),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query) })
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clickable(interactionSource = null, indication = null, role = Role.Button) { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    GlassIcon(icons.clear, secondary, Modifier.size(15.dp))
                }
            } else {
                GlassIcon(icons.microphone, secondary, Modifier.size(19.dp))
            }
        }
        AnimatedVisibility(
            focused,
            enter = fadeIn() + scaleIn(initialScale = 0.5f) + expandHorizontally(),
            exit = fadeOut() + scaleOut(targetScale = 0.5f) + shrinkHorizontally()
        ) {
            LiquidIconButton(
                icon = icons.close,
                contentDescription = "Close search",
                onClick = {
                    onQueryChange("")
                    focusManager.clearFocus()
                },
                backdrop = backdrop,
                size = 48.dp
            )
        }
    }
    }
}

/** A plain (non-glass) inline search field, as embedded in iOS 26 navigation bars and lists. */
@Composable
fun SearchFieldInline(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    searchIcon: ImageVector = GlassIcons.Search,
) {
    val secondary = GlassColors.secondaryLabel()
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier
            .height(40.dp)
            .clip(Capsule())
            .background(GlassColors.fill())
            .focusOnTap(focusRequester)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIcon(searchIcon, secondary, Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) BasicText(placeholder, Modifier.clearAndSetSemantics {}, style = TextStyle(secondary, 17.sp))
            BasicTextField(
                query, onQueryChange,
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .semantics { if (query.isEmpty()) contentDescription = placeholder },
                singleLine = true, textStyle = TextStyle(GlassColors.label(), 17.sp), cursorBrush = SolidColor(GlassColors.accent()))
        }
    }
}
