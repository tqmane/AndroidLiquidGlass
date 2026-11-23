package com.kyant.backdrop.catalog.utils

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun LockLandscape(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled, context) {
        var c = context
        while (c is ContextWrapper && c !is Activity) c = c.baseContext
        val activity = c as? Activity
        if (!enabled || activity == null) return@DisposableEffect onDispose {}
        val previous = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { activity.requestedOrientation = previous }
    }
}
