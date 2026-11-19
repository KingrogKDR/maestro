package com.maestro

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.maestro.player.RepeatMode
import com.maestro.ui.shell.DesktopBack
import com.maestro.ui.shell.Navigator
import com.maestro.ui.shell.Section
import java.awt.Dimension

private val SectionKeys = listOf(Key.One, Key.Two, Key.Three)

fun main() = application {
    val viewModel = remember { AppViewModel() }
    val navigator = remember { Navigator() }
    val player = viewModel.player
    val state by player.state.collectAsState()
    val hasTrack = state.current != null

    Window(
        onCloseRequest = ::exitApplication,
        title = "Maestro",
        state = rememberWindowState(size = DpSize(1200.dp, 800.dp)),
        onPreviewKeyEvent = { event ->
            // Escape acts as "back": closes the player first, then pops a screen.
            event.type == KeyEventType.KeyDown && event.key == Key.Escape && DesktopBack.dispatch()
        },
    ) {
        window.minimumSize = Dimension(420, 560)

        // Every command is reachable from the menu bar; shortcuts only for the frequent ones.
        MenuBar {
            Menu("Playback", mnemonic = 'P') {
                Item(
                    if (state.isPlaying) "Pause" else "Play",
                    enabled = hasTrack,
                    shortcut = KeyShortcut(Key.Spacebar),
                    onClick = player::togglePlayPause,
                )
                Item("Next Song", enabled = hasTrack, shortcut = KeyShortcut(Key.DirectionRight, ctrl = true), onClick = player::skipToNext)
                Item("Previous Song", enabled = hasTrack, shortcut = KeyShortcut(Key.DirectionLeft, ctrl = true), onClick = player::skipToPrevious)
                Separator()
                CheckboxItem("Shuffle", checked = state.shuffle, onCheckedChange = { player.toggleShuffle() })
                Menu("Repeat") {
                    RepeatMode.entries.forEach { mode ->
                        RadioButtonItem(
                            when (mode) {
                                RepeatMode.Off -> "Off"
                                RepeatMode.All -> "All"
                                RepeatMode.One -> "One"
                            },
                            selected = state.repeat == mode,
                            onClick = { player.setRepeatMode(mode) },
                        )
                    }
                }
            }
            Menu("View", mnemonic = 'V') {
                Section.entries.forEachIndexed { index, section ->
                    Item(
                        section.label,
                        shortcut = KeyShortcut(SectionKeys[index], ctrl = true),
                        onClick = { navigator.select(section) },
                    )
                }
                Separator()
                Item("Back", enabled = navigator.canGoBack, onClick = { navigator.back() })
            }
        }

        App(viewModel, navigator)
    }
}
