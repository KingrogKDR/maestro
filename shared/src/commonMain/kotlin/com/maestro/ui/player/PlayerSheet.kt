package com.maestro.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.maestro.player.Player
import com.maestro.player.PlayerState
import com.maestro.ui.components.ArtworkImage
import com.maestro.ui.components.ControlButton
import com.maestro.ui.components.artworkPalette
import com.maestro.ui.components.pressable
import com.maestro.ui.icons.MaestroIcons
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Motion
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/** Mini player height; grows with the system text size so two lines never clip. */
@Composable
fun miniPlayerHeight(): Dp {
    val fontScale = LocalDensity.current.fontScale
    return maxOf(64.dp, (37 * fontScale + 27).dp)
}

private val MiniArtSize = 48.dp
private val MiniArtPadding = 8.dp
private val MiniRadius = 16.dp
private val MiniArtRadius = 8.dp
private val FullArtRadius = 14.dp

/**
 * The player as one continuous surface: a mini player docked above the bottom
 * chrome that expands into the full now-playing screen. It follows the finger
 * 1:1, can be thrown either way, and the cover art morphs between the two.
 *
 * @param bottomInset distance from the bottom of the screen to the docked mini player.
 * @param startInset left margin of the docked mini player.
 * @param endInset right margin of the docked mini player.
 */
@Composable
fun PlayerSheet(
    sheet: PlayerSheetState,
    playerState: PlayerState,
    player: Player,
    bottomInset: Dp,
    miniHeight: Dp,
    startInset: Dp,
    endInset: Dp,
    modifier: Modifier = Modifier,
) {
    val track = playerState.current ?: return
    val density = LocalDensity.current
    val palette = artworkPalette(track.artwork)
    // Darkened slightly so white controls at the top keep at least 3:1 on light covers.
    val tintTop by animateColorAsState(lerp(palette.primary, Color.Black, 0.18f), Motion.standard())
    val tintBottom by animateColorAsState(lerp(palette.primary, Color.Black, 0.7f), Motion.standard())
    val miniBackground = Maestro.colors.surfaceRaised
    val shrinkWhenPaused = !playerState.isPlaying && !Maestro.reduceMotion
    val artScale by animateFloatAsState(if (shrinkWhenPaused) 0.8f else 1f, Motion.standard())

    BoxWithConstraints(modifier.fillMaxSize()) {
        val heightPx = constraints.maxHeight.toFloat()
        val miniPx = with(density) { miniHeight.toPx() }
        val startPx = with(density) { startInset.toPx() }
        val endPx = with(density) { endInset.toPx() }
        val collapsed = heightPx - with(density) { bottomInset.toPx() } - miniPx
        SideEffect { sheet.updateBounds(collapsed, heightPx) }

        val showMini by remember { derivedStateOf { sheet.progress < 0.6f } }
        // Screen readers only see whichever player is actually presented.
        val fullPresented by remember { derivedStateOf { sheet.progress > 0.5f } }
        val sheetCoords = remember { CoordinatesHolder() }
        var fullArtRect by remember { mutableStateOf<Rect?>(null) }
        val miniArtRect = with(density) {
            Rect(
                Offset(startPx + MiniArtPadding.toPx(), (miniPx - MiniArtSize.toPx()) / 2),
                Size(MiniArtSize.toPx(), MiniArtSize.toPx()),
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                // Extra height below the screen so an upward rubber-band never reveals a gap.
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .requiredHeight(maxHeight * 1.5f)
                .offset { IntOffset(0, sheet.offset.roundToInt()) }
                .graphicsLayer {
                    val p = sheet.progress
                    val visible = lerp(miniPx, heightPx, p) + max(0f, -sheet.offset)
                    shape = SheetShape(
                        start = lerp(startPx, 0f, p),
                        end = lerp(endPx, 0f, p),
                        visibleHeight = visible,
                        radius = MiniRadius.toPx() * (1f - p.pow(3)),
                    )
                    clip = true
                    shadowElevation = (1f - p) * 10.dp.toPx()
                }
                .draggable(
                    state = rememberDraggableState { sheet.dragBy(it) },
                    orientation = Orientation.Vertical,
                    startDragImmediately = sheet.isAnimating,
                    onDragStarted = { sheet.startDrag() },
                    onDragStopped = { velocity -> sheet.release(velocity) },
                )
                .onPlaced { sheetCoords.value = it }
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(tintTop, tintBottom), endY = heightPx))
                    val miniAlpha = (1f - sheet.progress / 0.5f).coerceIn(0f, 1f)
                    if (miniAlpha > 0f) drawRect(miniBackground.copy(alpha = miniAlpha))
                },
        ) {
            NowPlaying(
                playerState = playerState,
                player = player,
                onCollapse = sheet::collapse,
                onArtPlaced = { coords ->
                    val root = sheetCoords.value ?: return@NowPlaying
                    val r = root.localBoundingBoxOf(coords)
                    // Rounded so sheet movement never produces a "new" rect from float noise.
                    fullArtRect = Rect(
                        r.left.roundToInt().toFloat(), r.top.roundToInt().toFloat(),
                        r.right.roundToInt().toFloat(), r.bottom.roundToInt().toFloat(),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(this@BoxWithConstraints.maxHeight)
                    .then(if (fullPresented) Modifier else Modifier.clearAndSetSemantics { })
                    .graphicsLayer { alpha = ((sheet.progress - 0.15f) / 0.6f).coerceIn(0f, 1f) },
            )
            if (showMini) {
                MiniPlayer(
                    playerState = playerState,
                    player = player,
                    height = miniHeight,
                    onExpand = sheet::expand,
                    modifier = Modifier
                        .padding(start = startInset, end = endInset)
                        .graphicsLayer { alpha = (1f - sheet.progress / 0.35f).coerceIn(0f, 1f) },
                )
            }
            fullArtRect?.let { full ->
                MorphingArtwork(
                    track = track,
                    mini = miniArtRect,
                    full = full,
                    progress = { sheet.progress },
                    pausedScale = { artScale },
                )
            }
        }
    }
}

/** One cover that travels between the mini player and the full player. */
@Composable
private fun MorphingArtwork(
    track: com.maestro.model.Track,
    mini: Rect,
    full: Rect,
    progress: () -> Float,
    pausedScale: () -> Float,
) {
    val density = LocalDensity.current
    with(density) {
        Box(
            Modifier
                .offset { IntOffset(full.left.roundToInt(), full.top.roundToInt()) }
                .size(full.width.toDp(), full.height.toDp())
                .graphicsLayer {
                    val p = progress()
                    // The paused shrink only applies to the big cover.
                    val s = lerp(1f, pausedScale(), p)
                    val fullW = full.width * s
                    val fullH = full.height * s
                    val targetLeft = lerp(mini.left, full.center.x - fullW / 2, p)
                    val targetTop = lerp(mini.top, full.center.y - fullH / 2, p)
                    val targetW = lerp(mini.width, fullW, p)
                    val targetH = lerp(mini.height, fullH, p)
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = targetW / full.width
                    scaleY = targetH / full.height
                    translationX = targetLeft - full.left
                    translationY = targetTop - full.top
                    val radius = lerp(MiniArtRadius.toPx(), FullArtRadius.toPx(), p)
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(radius / scaleX)
                    clip = true
                    shadowElevation = p * 24.dp.toPx() * s
                },
        ) {
            ArtworkImage(track.artwork, Modifier.fillMaxSize(), shape = RectangleShape)
        }
    }
}

@Composable
private fun MiniPlayer(
    playerState: PlayerState,
    player: Player,
    height: Dp,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playerState.current ?: return
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .pressable(onExpand, pressedScale = 1f, onClickLabel = "Open player"),
    ) {
        Row(
            Modifier.fillMaxSize().padding(start = MiniArtPadding, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(MiniArtSize)) // The morphing cover sits here.
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    style = Maestro.type.body.copy(fontWeight = FontWeight.SemiBold),
                    color = Maestro.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    track.artist,
                    style = Maestro.type.caption,
                    color = Maestro.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ControlButton(
                if (playerState.isPlaying) MaestroIcons.Pause else MaestroIcons.Play,
                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                onClick = player::togglePlayPause,
            )
            ControlButton(MaestroIcons.Next, contentDescription = "Next", onClick = player::skipToNext)
        }
        val accent = Maestro.colors.accent
        val fraction = if (playerState.durationMs > 0) playerState.positionMs.toFloat() / playerState.durationMs else 0f
        Canvas(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(2.dp),
        ) {
            drawRoundRect(accent, size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(size.height))
        }
    }
}

private class CoordinatesHolder {
    var value: LayoutCoordinates? = null
}

/** The visible part of the sheet; also limits where it receives touches. */
private class SheetShape(
    private val start: Float,
    private val end: Float,
    private val visibleHeight: Float,
    private val radius: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rounded(
            RoundRect(
                left = start,
                top = 0f,
                right = size.width - end,
                bottom = visibleHeight.coerceAtMost(size.height),
                cornerRadius = CornerRadius(radius),
            ),
        )
}
