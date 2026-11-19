package com.maestro.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * Desktop has no system back, so the window forwards Escape here. The most
 * recently registered enabled handler wins, mirroring Android's behavior.
 */
object DesktopBack {
    internal class Handler(var enabled: Boolean, var onBack: () -> Unit)

    internal val handlers = mutableListOf<Handler>()

    /** Returns true if something handled the back press. */
    fun dispatch(): Boolean {
        val handler = handlers.lastOrNull { it.enabled } ?: return false
        handler.onBack()
        return true
    }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val handler = remember { DesktopBack.Handler(enabled, onBack) }
    SideEffect {
        handler.enabled = enabled
        handler.onBack = onBack
    }
    DisposableEffect(handler) {
        DesktopBack.handlers += handler
        onDispose { DesktopBack.handlers -= handler }
    }
}
