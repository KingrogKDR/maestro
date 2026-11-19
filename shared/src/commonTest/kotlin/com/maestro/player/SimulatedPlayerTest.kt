package com.maestro.player

import com.maestro.data.SampleLibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedPlayerTest {
    private val album = SampleLibraryRepository().library.value.albums.first()
    private val tracks = album.tracks

    private fun TestScope.player() = SimulatedPlayer(backgroundScope, tickMs = 100, random = Random(7))

    @Test
    fun playStartsAtRequestedTrack() = runTest {
        val p = player()
        p.play(tracks, 2)
        assertEquals(tracks[2], p.state.value.current)
        assertTrue(p.state.value.isPlaying)
    }

    @Test
    fun clockAdvancesOnlyWhilePlaying() = runTest {
        val p = player()
        p.play(tracks, 0)
        advanceTimeBy(1_050)
        assertEquals(1_000, p.state.value.positionMs)
        p.togglePlayPause()
        advanceTimeBy(1_000)
        assertEquals(1_000, p.state.value.positionMs)
    }

    @Test
    fun trackEndMovesToNext() = runTest {
        val p = player()
        p.play(tracks, 0)
        p.seekTo(tracks[0].durationMs - 100)
        advanceTimeBy(150)
        assertEquals(tracks[1], p.state.value.current)
        assertEquals(0, p.state.value.positionMs)
    }

    @Test
    fun endOfQueueStopsOnFirstTrackWithoutRepeat() = runTest {
        val p = player()
        p.play(tracks, tracks.lastIndex)
        p.skipToNext()
        assertEquals(tracks[0], p.state.value.current)
        assertFalse(p.state.value.isPlaying)
    }

    @Test
    fun repeatAllWrapsAndKeepsPlaying() = runTest {
        val p = player()
        p.cycleRepeatMode()
        p.play(tracks, tracks.lastIndex)
        p.skipToNext()
        assertEquals(tracks[0], p.state.value.current)
        assertTrue(p.state.value.isPlaying)
    }

    @Test
    fun repeatOneReplaysTheSameTrack() = runTest {
        val p = player()
        p.cycleRepeatMode()
        p.cycleRepeatMode()
        assertEquals(RepeatMode.One, p.state.value.repeat)
        p.play(tracks, 1)
        p.seekTo(tracks[1].durationMs - 100)
        advanceTimeBy(150)
        assertEquals(tracks[1], p.state.value.current)
        assertEquals(0, p.state.value.positionMs)
    }

    @Test
    fun previousRestartsSongAfterThreeSeconds() = runTest {
        val p = player()
        p.play(tracks, 2)
        p.seekTo(10_000)
        p.skipToPrevious()
        assertEquals(tracks[2], p.state.value.current)
        assertEquals(0, p.state.value.positionMs)
        p.skipToPrevious()
        assertEquals(tracks[1], p.state.value.current)
    }

    @Test
    fun shuffleKeepsCurrentFirstAndRestoresOrder() = runTest {
        val p = player()
        p.play(tracks, 3)
        p.toggleShuffle()
        val shuffled = p.state.value
        assertEquals(tracks[3], shuffled.current)
        assertEquals(0, shuffled.index)
        assertEquals(tracks.toSet(), shuffled.queue.toSet())

        p.toggleShuffle()
        assertEquals(tracks, p.state.value.queue)
        assertEquals(3, p.state.value.index)
    }
}
