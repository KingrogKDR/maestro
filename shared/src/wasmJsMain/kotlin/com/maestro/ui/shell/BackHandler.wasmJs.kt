package com.maestro.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import kotlinx.browser.window
import org.w3c.dom.events.KeyboardEvent

/** In the browser, Escape acts as back, like on desktop. Latest enabled handler wins. */
object WebBack {
    internal class Handler(var enabled: Boolean, var onBack: () -> Unit)

    internal val handlers = mutableListOf<Handler>()

    fun install() {
        window.addEventListener("keydown", { event ->
            if ((event as KeyboardEvent).key == "Escape") {
                handlers.lastOrNull { it.enabled }?.onBack?.invoke()
            }
        })
    }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val handler = remember { WebBack.Handler(enabled, onBack) }
    SideEffect {
        handler.enabled = enabled
        handler.onBack = onBack
    }
    DisposableEffect(handler) {
        WebBack.handlers += handler
        onDispose { WebBack.handlers -= handler }
    }
}
