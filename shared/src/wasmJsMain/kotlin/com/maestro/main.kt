package com.maestro

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.maestro.ui.shell.WebBack
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    WebBack.install()
    ComposeViewport(document.body!!) {
        App(viewModel = remember { AppViewModel() })
    }
}
