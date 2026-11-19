package com.maestro.ui.player

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.maestro.ui.theme.Motion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Position of the now-playing sheet, in px from the top of the screen.
 *
 * There is a single presentation value, [offset]. Animations always start from
 * it and carry the current velocity, so the sheet can be grabbed mid-flight,
 * tapped again while moving, or thrown, and the motion never jumps.
 */
@Stable
class PlayerSheetState internal constructor(private val scope: CoroutineScope) {

    /** Offset when fully expanded is 0; collapsed is [collapsedOffset]. */
    var offset by mutableFloatStateOf(0f)
        private set

    var collapsedOffset by mutableFloatStateOf(0f)
        private set

    /** Where the sheet is heading; drives back handling and accessibility. */
    var isExpanded by mutableStateOf(false)
        private set

    private var travel = 1f
    private var velocity = 0f
    private var rawDrag = 0f
    private var animation: Job? = null
    private var laidOut = false

    /** True while the sheet is moving on its own, so a touch can catch it mid-flight. */
    var isAnimating by mutableStateOf(false)
        private set

    /** 0 when collapsed, 1 when expanded. */
    val progress: Float
        get() = if (collapsedOffset <= 0f) 0f else (1f - offset / collapsedOffset).coerceIn(0f, 1f)

    internal fun updateBounds(collapsed: Float, height: Float) {
        travel = height
        if (collapsedOffset == collapsed && laidOut) return
        collapsedOffset = collapsed
        if (!laidOut || animation?.isActive != true) offset = if (isExpanded) 0f else collapsed
        laidOut = true
    }

    fun expand() = animateTo(expanded = true, Motion.standard())

    fun collapse() = animateTo(expanded = false, Motion.standard())

    fun toggle() = if (isExpanded) collapse() else expand()

    internal fun startDrag() {
        animation?.cancel()
        rawDrag = offset
    }

    internal fun dragBy(delta: Float) {
        rawDrag += delta
        offset = when {
            rawDrag < 0f -> -Motion.rubberband(-rawDrag, travel)
            rawDrag > collapsedOffset -> collapsedOffset + Motion.rubberband(rawDrag - collapsedOffset, travel)
            else -> rawDrag
        }
    }

    /** Picks the resting point nearest to where the flick would carry the sheet. */
    internal fun release(releaseVelocity: Float) {
        velocity = releaseVelocity
        val projected = offset + Motion.project(releaseVelocity)
        val expand = abs(projected) < abs(projected - collapsedOffset)
        animateTo(expand, Motion.sheet())
    }

    private fun animateTo(expanded: Boolean, spec: AnimationSpec<Float>) {
        isExpanded = expanded
        val target = if (expanded) 0f else collapsedOffset
        animation?.cancel()
        animation = scope.launch {
            isAnimating = true
            try {
                animate(offset, target, initialVelocity = velocity, animationSpec = spec) { value, v ->
                    offset = value
                    velocity = v
                }
                velocity = 0f
            } finally {
                isAnimating = false
            }
        }
    }
}

@Composable
fun rememberPlayerSheetState(): PlayerSheetState {
    val scope = rememberCoroutineScope()
    return remember { PlayerSheetState(scope) }
}
