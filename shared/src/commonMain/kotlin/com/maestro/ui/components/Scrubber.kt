package com.maestro.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Motion

/**
 * Song position scrubber. Touch drags are relative (grabbing the bar never makes
 * the position jump); a mouse click seeks to the point clicked, as desktop users
 * expect. The bar thickens while held so it's clear it's being manipulated.
 */
@Composable
fun Scrubber(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Maestro.colors.textPrimary,
    trackColor: Color = Maestro.colors.textPrimary.copy(alpha = 0.2f),
    labelColor: Color = Maestro.colors.textSecondary,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val fraction = dragFraction
        ?: if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val held = dragFraction != null
    val thickness by animateDpAsState(if (held) 10.dp else 5.dp, Motion.snappy())
    val currentDuration by rememberUpdatedState(durationMs)
    val currentFraction by rememberUpdatedState(fraction)
    val seek by rememberUpdatedState(onSeek)

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                // The bar is thin but the touch target is a full 44dp tall.
                .height(44.dp)
                .semantics {
                    contentDescription = "Song position"
                    stateDescription = "${formatDuration(positionMs)} of ${formatDuration(durationMs)}"
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f, steps = 0)
                    setProgress { target ->
                        seek((target.coerceIn(0f, 1f) * currentDuration).toLong())
                        true
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val absolute = down.type == PointerType.Mouse
                        val start = if (absolute) down.position.x / width else currentFraction
                        dragFraction = start.coerceIn(0f, 1f)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val delta = (change.position.x - down.position.x) / width
                            dragFraction = (start + delta).coerceIn(0f, 1f)
                        }
                        dragFraction?.let { seek((it * currentDuration).toLong()) }
                        dragFraction = null
                    }
                },
        ) {
            val barHeight = thickness.toPx()
            val top = (size.height - barHeight) / 2
            val radius = CornerRadius(barHeight / 2)
            drawRoundRect(trackColor, Offset(0f, top), Size(size.width, barHeight), radius)
            drawRoundRect(activeColor, Offset(0f, top), Size(size.width * fraction, barHeight), radius)
        }
        val shownMs = (fraction * durationMs).toLong()
        Row(
            Modifier.fillMaxWidth().padding(top = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(shownMs), style = Maestro.type.numeric, color = labelColor)
            Text("-" + formatDuration(durationMs - shownMs), style = Maestro.type.numeric, color = labelColor)
        }
    }
}
