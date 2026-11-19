package com.maestro.ui.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.maestro.player.Player
import com.maestro.player.PlayerState
import com.maestro.player.RepeatMode
import com.maestro.ui.components.ControlButton
import com.maestro.ui.components.Scrubber
import com.maestro.ui.components.pressable
import com.maestro.ui.theme.Radius
import com.maestro.ui.icons.MaestroIcons
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Motion

private val OnTint = Color.White
private val OnTintSecondary = Color.White.copy(alpha = 0.66f)

/**
 * Full player. The cover itself is drawn by the sheet (so it can morph from the
 * mini player); this layout only reserves its space via [onArtPlaced].
 */
@Composable
internal fun NowPlaying(
    playerState: PlayerState,
    player: Player,
    onCollapse: () -> Unit,
    onOpenAlbum: () -> Unit,
    onArtPlaced: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playerState.current ?: return
    BoxWithConstraints(modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        val width = maxWidth
        val height = maxHeight
        val landscape = width > height && width >= 560.dp
        Column(Modifier.fillMaxSize()) {
            TopBar(onCollapse)
            if (landscape) {
                val art = min(height - 96.dp, width * 0.42f)
                Row(
                    Modifier.fillMaxWidth().weight(1f).padding(horizontal = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    ArtSlot(art, onArtPlaced)
                    Spacer(Modifier.width(48.dp))
                    Column(Modifier.widthIn(max = 440.dp).weight(1f, fill = false)) {
                        TrackInfo(track.title, track.artist, track.albumTitle, onOpenAlbum)
                        Spacer(Modifier.height(24.dp))
                        Controls(playerState, player)
                    }
                }
            } else {
                val art = min(min(width - 56.dp, height * 0.46f), 440.dp)
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.weight(1f))
                    ArtSlot(art, onArtPlaced)
                    Spacer(Modifier.weight(1f))
                    Column(Modifier.widthIn(max = 440.dp)) {
                        TrackInfo(track.title, track.artist, track.albumTitle, onOpenAlbum)
                        Spacer(Modifier.height(20.dp))
                        Controls(playerState, player)
                    }
                    Spacer(Modifier.weight(0.6f))
                }
            }
        }
    }
}

@Composable
private fun TopBar(onCollapse: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp)) {
        // Grabber: says "this can be pulled down".
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
                .size(width = 36.dp, height = 5.dp)
                .clip(CircleShape)
                .background(OnTint.copy(alpha = 0.35f)),
        )
        ControlButton(
            MaestroIcons.ChevronDown,
            contentDescription = "Close player",
            onClick = onCollapse,
            modifier = Modifier.align(Alignment.CenterStart),
            tint = OnTintSecondary,
        )
    }
}

@Composable
private fun ArtSlot(size: Dp, onPlaced: (LayoutCoordinates) -> Unit) {
    Spacer(Modifier.size(size).onGloballyPositioned(onPlaced))
}

/** Song, then "Artist · Album"; the second line goes to the album, because the record comes first. */
@Composable
private fun TrackInfo(title: String, artist: String, album: String, onOpenAlbum: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = Maestro.type.title, color = OnTint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(
            Modifier
                .offset(x = (-8).dp)
                .clip(Radius.small)
                .pressable(
                    onOpenAlbum,
                    pressedScale = 0.98f,
                    highlight = OnTint.copy(alpha = 0.12f),
                    onClickLabel = "Go to album",
                    shape = Radius.small,
                )
                .heightIn(min = 48.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$artist · $album",
                style = Maestro.type.headline,
                color = OnTintSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(MaestroIcons.ChevronRight, null, Modifier.padding(start = 2.dp).size(18.dp), tint = OnTintSecondary)
        }
    }
}

@Composable
private fun Controls(playerState: PlayerState, player: Player) {
    Column(Modifier.fillMaxWidth()) {
        Scrubber(
            positionMs = playerState.positionMs,
            durationMs = playerState.durationMs,
            onSeek = player::seekTo,
            activeColor = OnTint,
            trackColor = OnTint.copy(alpha = 0.22f),
            labelColor = OnTintSecondary,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlButton(MaestroIcons.Previous, "Previous", player::skipToPrevious, size = 64.dp, iconSize = 34.dp, tint = OnTint)
            Crossfade(playerState.isPlaying, animationSpec = Motion.snappy()) { playing ->
                ControlButton(
                    if (playing) MaestroIcons.Pause else MaestroIcons.Play,
                    contentDescription = if (playing) "Pause" else "Play",
                    onClick = player::togglePlayPause,
                    size = 80.dp,
                    iconSize = 48.dp,
                    tint = OnTint,
                )
            }
            ControlButton(MaestroIcons.Next, "Next", player::skipToNext, size = 64.dp, iconSize = 34.dp, tint = OnTint)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ModeToggle(
                active = playerState.shuffle,
                content = {
                    ControlButton(MaestroIcons.Shuffle, if (playerState.shuffle) "Shuffle on" else "Shuffle off", player::toggleShuffle, iconSize = 22.dp, tint = it)
                },
            )
            ModeToggle(
                active = playerState.repeat != RepeatMode.Off,
                content = {
                    val (icon, label) = when (playerState.repeat) {
                        RepeatMode.Off -> MaestroIcons.Repeat to "Repeat off"
                        RepeatMode.All -> MaestroIcons.Repeat to "Repeat all"
                        RepeatMode.One -> MaestroIcons.RepeatOne to "Repeat one"
                    }
                    ControlButton(icon, label, player::cycleRepeatMode, iconSize = 22.dp, tint = it)
                },
            )
        }
    }
}

/** Active modes get a soft backing so state is readable without relying on color alone. */
@Composable
private fun ModeToggle(active: Boolean, content: @Composable (tint: Color) -> Unit) {
    val backing by animateColorAsState(if (active) OnTint.copy(alpha = 0.18f) else Color.Transparent, Motion.snappy())
    val tint by animateColorAsState(if (active) OnTint else OnTintSecondary, Motion.snappy())
    Box(Modifier.clip(CircleShape).background(backing)) { content(tint) }
}
