package com.maestro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maestro.ui.shell.AppShell
import com.maestro.ui.shell.Navigator
import com.maestro.ui.theme.MaestroTheme

@Composable
fun App(
    viewModel: AppViewModel = viewModel { AppViewModel() },
    navigator: Navigator = remember { Navigator() },
) {
    MaestroTheme {
        AppShell(viewModel, navigator)
    }
}
