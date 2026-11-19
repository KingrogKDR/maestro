package com.maestro.ui.theme

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MotionTest {
    @Test
    fun projectionMatchesScrollDeceleration() {
        // 1000 px/s at 0.998 travels ~499 px.
        assertTrue(abs(Motion.project(1000f) - 499f) < 0.5f)
        assertEquals(0f, Motion.project(0f))
        assertTrue(Motion.project(-1000f) < 0f)
    }

    @Test
    fun rubberbandResistsMoreTheFurtherYouPull() {
        val small = Motion.rubberband(50f, 800f)
        val large = Motion.rubberband(500f, 800f)
        assertTrue(small < 50f)
        assertTrue(large < 500f)
        assertTrue(large / 500f < small / 50f)
    }

    @Test
    fun responseMapsToStiffness() {
        // A 0.4 s response is roughly Compose's "medium low" stiffness range.
        assertTrue(abs(Motion.stiffnessFor(0.4f) - 246.7f) < 0.5f)
    }
}
