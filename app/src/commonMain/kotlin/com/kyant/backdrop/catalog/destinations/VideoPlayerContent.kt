package com.kyant.backdrop.catalog.destinations

import androidx.compose.runtime.Composable
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.LiquidVideoPlayerControls
import com.kyant.backdrop.catalog.components.rememberVideoPlayerState

/** iOS 26 video player chrome over the wallpaper as the "video" frame. */
@Composable
fun VideoPlayerContent() {
    BackdropDemoScaffold { backdrop ->
        val state = rememberVideoPlayerState()
        LiquidVideoPlayerControls(state, backdrop, "Big Buck Bunny", onClose = {})
    }
}
