package com.maestro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Motion

/**
 * Press feedback that starts on pointer-down: the element shrinks slightly and,
 * optionally, picks up a tint. The action still commits on release. With a
 * pointer, hovering shows half the tint so targets read as clickable.
 */
@Composable
fun Modifier.pressable(
    onClick: () -> Unit,
    pressedScale: Float = 0.97f,
    highlight: Color = Color.Transparent,
    enabled: Boolean = true,
    role: Role = Role.Button,
    onClickLabel: String? = null,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val hovered by source.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, Motion.snappy())
    val tintAlpha = when {
        pressed -> highlight.alpha
        hovered -> highlight.alpha * 0.5f
        else -> 0f
    }
    val tint by animateColorAsState(highlight.copy(alpha = tintAlpha), Motion.snappy())
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .background(tint)
        .clickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
}

/** A round icon button for transport and toolbar controls. */
@Composable
fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    tint: Color = Maestro.colors.textPrimary,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .pressable(onClick, pressedScale = 0.86f, highlight = Maestro.colors.hairline),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = tint)
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val ss = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:$ss" else "$minutes:$ss"
}

/** "1 hr 12 min" style summary for album headers. */
fun formatLongDuration(ms: Long): String {
    val minutes = ms / 60_000
    return if (minutes >= 60) "${minutes / 60} hr ${minutes % 60} min" else "$minutes min"
}
