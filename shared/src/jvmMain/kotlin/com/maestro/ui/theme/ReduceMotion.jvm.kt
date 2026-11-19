package com.maestro.ui.theme

import androidx.compose.runtime.Composable

/** Can be forced with -Dmaestro.reduceMotion=true until desktop settings are read. */
@Composable
actual fun rememberReduceMotion(): Boolean = System.getProperty("maestro.reduceMotion") == "true"
