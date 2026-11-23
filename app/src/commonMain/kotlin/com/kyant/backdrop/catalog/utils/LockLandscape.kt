package com.kyant.backdrop.catalog.utils

import androidx.compose.runtime.Composable

/** Rotates the window to landscape while [enabled] (fullscreen video), restoring the previous orientation afterwards. No-op where the platform controls rotation. */
@Composable
expect fun LockLandscape(enabled: Boolean)
