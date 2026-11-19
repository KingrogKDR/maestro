package com.maestro.ui.shell

import androidx.compose.runtime.Composable

/** System back (gesture or button) where the platform has one. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
