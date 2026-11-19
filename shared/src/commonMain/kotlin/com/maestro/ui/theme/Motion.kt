package com.maestro.ui.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow

/**
 * Motion vocabulary for the app. Springs are described the designer-friendly way:
 * a damping ratio (1 = no overshoot) and a response in seconds (lower = snappier).
 */
object Motion {

    /** Converts damping + response into a Compose spring (unit mass). */
    fun <T> spring(
        damping: Float = 1f,
        response: Float = 0.4f,
        visibilityThreshold: T? = null,
    ): SpringSpec<T> = spring(
        dampingRatio = damping,
        stiffness = stiffnessFor(response),
        visibilityThreshold = visibilityThreshold,
    )

    fun stiffnessFor(response: Float): Float = (2 * PI / response).pow(2).toFloat()

    /** Default for anything that wasn't thrown: settles with no overshoot. */
    fun <T> standard(): SpringSpec<T> = spring(damping = 1f, response = 0.4f)

    /** Quick feedback such as press states. */
    fun <T> snappy(): SpringSpec<T> = spring(damping = 1f, response = 0.25f)

    /** Sheets and drawers released with momentum get a little life. */
    fun <T> sheet(): SpringSpec<T> = spring(damping = 0.8f, response = 0.3f)

    /**
     * Where a flick would come to rest, using scroll-style exponential deceleration.
     * [velocity] is in px/s; the result is a distance in px.
     */
    fun project(velocity: Float, decelerationRate: Float = 0.998f): Float =
        (velocity / 1000f) * decelerationRate / (1f - decelerationRate)

    /** Progressive resistance past a boundary instead of a hard stop. */
    fun rubberband(overshoot: Float, dimension: Float, constant: Float = 0.55f): Float {
        if (dimension <= 0f) return 0f
        return (overshoot * dimension * constant) / (dimension + constant * abs(overshoot))
    }
}
