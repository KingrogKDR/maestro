package com.maestro.ui.library

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maestro.model.Track
import com.maestro.ui.components.ArtworkImage
import com.maestro.ui.components.ControlButton
import com.maestro.ui.components.formatDuration
import com.maestro.ui.components.pressable
import com.maestro.ui.icons.MaestroIcons
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Radius

/** What a screen needs to know about playback to mark the current song. */
data class NowPlayingInfo(val trackId: String?, val isPlaying: Boolean)

@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = Maestro.type.largeTitle,
        color = Maestro.colors.textPrimary,
        modifier = modifier.padding(top = 8.dp, bottom = 12.dp),
    )
}

@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    ControlButton(
        MaestroIcons.ArrowBack,
        contentDescription = "Back",
        onClick = onBack,
        modifier = modifier.padding(top = 4.dp).offset(x = (-12).dp),
        tint = Maestro.colors.textPrimary,
    )
}

/**
 * A 48dp action button. Tonal by default; pass [container]/[content] for the
 * filled primary action (Material's filled + tonal pairing).
 */
@Composable
fun PillButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Maestro.colors.surfaceRaised,
    content: Color = Maestro.colors.accent,
) {
    Row(
        modifier
            .height(48.dp)
            .clip(Radius.medium)
            .background(container)
            .pressable(onClick, pressedScale = 0.96f, highlight = Maestro.colors.hairline, shape = Radius.medium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = content)
        Spacer(Modifier.width(8.dp))
        Text(label, style = Maestro.type.headline, color = content)
    }
}

@Composable
fun PlayShuffleRow(
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    playContainer: Color = Maestro.colors.surfaceRaised,
    playContent: Color = Maestro.colors.accent,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PillButton("Play", MaestroIcons.Play, onPlay, Modifier.weight(1f), playContainer, playContent)
        PillButton("Shuffle", MaestroIcons.Shuffle, onShuffle, Modifier.weight(1f))
    }
}

/**
 * A song row. [showArtwork] shows the cover (song lists); otherwise the track
 * number is shown (album track lists). The playing song gets live bars instead.
 */
@Composable
fun TrackRow(
    track: Track,
    nowPlaying: NowPlayingInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArtwork: Boolean = true,
    subtitle: String = "${track.artist} · ${track.albumTitle}",
) {
    val isCurrent = nowPlaying.trackId == track.id
    Row(
        modifier
            .fillMaxWidth()
            .clip(Radius.medium)
            .pressable(onClick, pressedScale = 0.985f, highlight = Maestro.colors.hairline, shape = Radius.medium)
            .padding(horizontal = 8.dp, vertical = if (showArtwork) 6.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showArtwork) {
            Box(contentAlignment = Alignment.Center) {
                ArtworkImage(track.artwork, Modifier.size(44.dp), shape = Radius.small)
                if (isCurrent) {
                    Box(
                        Modifier.size(44.dp).clip(Radius.small).background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center,
                    ) { PlayingBars(nowPlaying.isPlaying, color = Color.White) }
                }
            }
        } else {
            Box(Modifier.width(28.dp), contentAlignment = Alignment.CenterStart) {
                if (isCurrent) {
                    PlayingBars(nowPlaying.isPlaying, color = Maestro.colors.accent)
                } else {
                    Text("${track.trackNumber}", style = Maestro.type.numeric, color = Maestro.colors.textTertiary)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = Maestro.type.body,
                color = if (isCurrent) Maestro.colors.accent else Maestro.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = Maestro.type.caption,
                    color = Maestro.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(formatDuration(track.durationMs), style = Maestro.type.numeric, color = Maestro.colors.textTertiary)
    }
}

/** Three bars that bounce while playing and rest, low, when paused. */
@Composable
fun PlayingBars(isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val phases = listOf(420, 340, 520).map { period ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(period), RepeatMode.Reverse),
        )
    }
    val animate = isPlaying && !Maestro.reduceMotion
    // Paused holds an uneven pose at half strength, so it never reads as an ellipsis.
    val barColor = if (isPlaying) color else color.copy(alpha = 0.55f)
    Canvas(modifier.size(width = 14.dp, height = 12.dp)) {
        val barWidth = size.width / 5
        phases.forEachIndexed { i, phase ->
            val level = if (animate) phase.value else listOf(0.55f, 1f, 0.75f)[i]
            val h = size.height * level
            drawRoundRect(
                barColor,
                topLeft = Offset(i * barWidth * 2, size.height - h),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2),
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = Maestro.type.title,
        color = Maestro.colors.textPrimary,
        modifier = modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

internal val CircleArt = RoundedCornerShape(percent = 50)
