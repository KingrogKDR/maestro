package com.maestro.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.maestro.AppViewModel
import com.maestro.data.Library
import com.maestro.ui.components.pressable
import com.maestro.ui.library.AlbumDetailScreen
import com.maestro.ui.library.AlbumsScreen
import com.maestro.ui.library.ArtistDetailScreen
import com.maestro.ui.library.ArtistsScreen
import com.maestro.ui.library.LibraryActions
import com.maestro.ui.library.NowPlayingInfo
import com.maestro.ui.library.SongsScreen
import com.maestro.ui.player.miniPlayerHeight
import com.maestro.ui.player.PlayerSheet
import com.maestro.ui.player.rememberPlayerSheetState
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Motion

private val TabBarHeight = 56.dp
private val SidebarWidth = 220.dp
private val WideBreakpoint = 720.dp

/**
 * Adapts to the window: a bottom tab bar on phones, a sidebar on wide windows.
 * The player sheet floats above either.
 */
@Composable
fun AppShell(viewModel: AppViewModel, navigator: Navigator) {
    val library by viewModel.library.collectAsState()
    val playerState by viewModel.player.state.collectAsState()
    val sheet = rememberPlayerSheetState()
    val reduceMotion = Maestro.reduceMotion
    val miniHeight = miniPlayerHeight()
    val playerOpen by remember { derivedStateOf { sheet.progress > 0.5f } }
    val actions = remember(navigator, viewModel) {
        LibraryActions(
            openAlbum = { navigator.open(Destination.AlbumDetail(it)) },
            openArtist = { navigator.open(Destination.ArtistDetail(it)) },
            play = viewModel::play,
            shuffle = viewModel::playShuffled,
            back = { navigator.back() },
        )
    }
    val nowPlaying = NowPlayingInfo(playerState.current?.id, playerState.isPlaying)

    // Registered last so it takes priority: back closes the player before leaving a screen.
    PlatformBackHandler(navigator.canGoBack) { navigator.back() }
    PlatformBackHandler(sheet.isExpanded) { sheet.collapse() }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val wide = maxWidth >= WideBreakpoint
        val hasPlayer = playerState.current != null
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val miniGap = if (wide) 12.dp else 8.dp
        val sheetBottomInset = if (wide) navBottom + miniGap else navBottom + TabBarHeight + miniGap
        val miniSpace = if (hasPlayer) miniHeight + miniGap * 2 else 0.dp
        val chromeBottom = if (wide) navBottom + miniSpace else navBottom + TabBarHeight + miniSpace
        val padding = PaddingValues(top = statusTop + if (wide) 24.dp else 8.dp, bottom = chromeBottom)
        val background = Maestro.colors.background

        // The library recedes while the player is open: dimmed, and on phones pushed back.
        Box(
            Modifier
                .fillMaxSize()
                // While the player covers it, the library is hidden from screen readers.
                .then(if (playerOpen) Modifier.clearAndSetSemantics { } else Modifier)
                .graphicsLayer {
                    val p = sheet.progress
                    if (!wide && !reduceMotion && p > 0f) {
                        val scale = 1f - 0.06f * p
                        scaleX = scale
                        scaleY = scale
                        shape = RoundedCornerShape((14 * p).dp)
                        clip = true
                    }
                }
                .background(background),
        ) {
            Row(Modifier.fillMaxSize()) {
                if (wide) Sidebar(navigator, statusTop)
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    ContentHost(navigator, library, nowPlaying, actions, padding, wide, reduceMotion)
                }
            }
            if (!wide) {
                BottomChrome(
                    navigator = navigator,
                    fadeHeight = chromeBottom + 24.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer { translationY = sheet.progress * size.height },
                )
            }
            Box(
                Modifier.fillMaxSize().drawBehind {
                    val p = sheet.progress
                    if (p > 0f) drawRect(Color.Black.copy(alpha = 0.45f * p))
                },
            )
        }

        PlayerSheet(
            sheet = sheet,
            playerState = playerState,
            player = viewModel.player,
            onOpenAlbum = { albumId ->
                sheet.collapse()
                navigator.open(Destination.AlbumDetail(albumId))
            },
            bottomInset = sheetBottomInset,
            miniHeight = miniHeight,
            startInset = if (wide) SidebarWidth + miniGap else miniGap,
            endInset = miniGap,
        )
    }
}

private data class Entry(val destination: Destination, val depth: Int)

@Composable
private fun ContentHost(
    navigator: Navigator,
    library: Library,
    nowPlaying: NowPlayingInfo,
    actions: LibraryActions,
    padding: PaddingValues,
    wide: Boolean,
    reduceMotion: Boolean,
) {
    val saveable = rememberSaveableStateHolder()
    val slide = Motion.spring(damping = 1f, response = 0.35f, visibilityThreshold = IntOffset.VisibilityThreshold)
    AnimatedContent(
        targetState = Entry(navigator.current, navigator.depth),
        transitionSpec = {
            when {
                reduceMotion -> fadeIn(tween(160)) togetherWith fadeOut(tween(120))
                // Pushed screens arrive from the right and leave the way they came.
                targetState.depth > initialState.depth ->
                    (slideInHorizontally(slide) { it } + fadeIn()) togetherWith
                        (slideOutHorizontally(slide) { -it / 4 } + fadeOut())
                targetState.depth < initialState.depth ->
                    (slideInHorizontally(slide) { -it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally(slide) { it } + fadeOut())
                else -> fadeIn(tween(180)) togetherWith fadeOut(tween(120))
            }
        },
        contentKey = { it.destination.key },
    ) { entry ->
        saveable.SaveableStateProvider(entry.destination.key) {
            when (val d = entry.destination) {
                is Destination.Root -> when (d.section) {
                    Section.Albums -> AlbumsScreen(library, actions, padding, wide)
                    Section.Artists -> ArtistsScreen(library, actions, padding, wide)
                    Section.Songs -> SongsScreen(library, nowPlaying, actions, padding, wide)
                }
                is Destination.AlbumDetail -> library.album(d.albumId)?.let {
                    AlbumDetailScreen(it, nowPlaying, actions, padding, wide)
                }
                is Destination.ArtistDetail -> ArtistDetailScreen(library, d.name, actions, padding, wide)
            }
        }
    }
}

/** Tab bar plus a fade so content softens as it scrolls under the floating chrome. */
@Composable
private fun BottomChrome(navigator: Navigator, fadeHeight: Dp, modifier: Modifier = Modifier) {
    val background = Maestro.colors.background
    Box(
        modifier
            .fillMaxWidth()
            .height(fadeHeight)
            .background(
                Brush.verticalGradient(
                    0f to background.copy(alpha = 0f),
                    0.35f to background.copy(alpha = 0.92f),
                    1f to background,
                ),
            ),
    ) {
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(TabBarHeight),
        ) {
            Section.entries.forEach { section ->
                TabItem(section, selected = navigator.section == section, onClick = { navigator.select(section) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TabItem(section: Section, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val tint by animateColorAsState(
        if (selected) Maestro.colors.accent else Maestro.colors.textSecondary,
        Motion.snappy(),
    )
    // Selection is shown by a backing shape as well as color.
    val indicator by animateColorAsState(
        if (selected) Maestro.colors.accent.copy(alpha = 0.16f) else Color.Transparent,
        Motion.snappy(),
    )
    Column(
        modifier
            .fillMaxHeight()
            .semantics { this.selected = selected }
            .pressable(onClick, pressedScale = 0.9f, role = Role.Tab),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(width = 56.dp, height = 30.dp).clip(RoundedCornerShape(15.dp)).background(indicator),
            contentAlignment = Alignment.Center,
        ) {
            Icon(section.icon, null, Modifier.size(24.dp), tint = tint)
        }
        Spacer(Modifier.height(2.dp))
        Text(section.label, style = Maestro.type.label, color = if (selected) Maestro.colors.textPrimary else Maestro.colors.textSecondary)
    }
}

@Composable
private fun Sidebar(navigator: Navigator, statusTop: Dp) {
    Column(
        Modifier
            .width(SidebarWidth)
            .fillMaxHeight()
            .background(Maestro.colors.surface)
            .padding(top = statusTop + 24.dp, start = 12.dp, end = 12.dp),
    ) {
        Text(
            "LIBRARY",
            style = Maestro.type.label,
            color = Maestro.colors.textTertiary,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
        )
        Section.entries.forEach { section ->
            val selected = navigator.section == section
            val backing by animateColorAsState(
                if (selected) Maestro.colors.surfaceRaised else Maestro.colors.surfaceRaised.copy(alpha = 0f),
                Motion.snappy(),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { this.selected = selected }
                    .clip(RoundedCornerShape(10.dp))
                    .background(backing)
                    .pressable({ navigator.select(section) }, pressedScale = 0.98f, highlight = Maestro.colors.hairline, role = Role.Tab)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    section.icon,
                    null,
                    Modifier.size(20.dp),
                    tint = if (selected) Maestro.colors.accent else Maestro.colors.textSecondary,
                )
                Spacer(Modifier.width(12.dp))
                Text(section.label, style = Maestro.type.body, color = Maestro.colors.textPrimary)
            }
        }
    }
}
