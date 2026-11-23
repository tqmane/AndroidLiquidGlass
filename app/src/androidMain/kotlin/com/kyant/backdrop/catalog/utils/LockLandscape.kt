package com.kyant.backdrop.catalog.utils

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun LockLandscape(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled, context) {
        var c = context
        while (c is ContextWrapper && c !is Activity) c = c.baseContext
        val activity = c as? Activity
        if (!enabled || activity == null) return@DisposableEffect onDispose {}
        // Tablets keep their orientation like iPad fullscreen playback; locking a large screen would letterbox the app.
        val lock = activity.resources.configuration.smallestScreenWidthDp < 600
        val previous = activity.requestedOrientation
        if (lock) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        // Fullscreen playback hides the status and navigation bars, like AVKit; a swipe shows them transiently.
        val insets = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insets.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            insets.show(WindowInsetsCompat.Type.systemBars())
            if (lock) activity.requestedOrientation = previous
        }
    }
}
