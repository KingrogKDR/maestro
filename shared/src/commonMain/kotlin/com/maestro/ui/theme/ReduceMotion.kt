package com.maestro.ui.theme

import androidx.compose.runtime.Composable

/** Whether the system's reduce-motion (or "remove animations") setting is on. */
@Composable
expect fun rememberReduceMotion(): Boolean
