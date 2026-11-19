package com.maestro.player

import com.maestro.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * A silent player that advances time on a clock. It implements the full queue,
 * shuffle and repeat behavior, so the UI can be built and tested before a real
 * audio engine exists, and the rules carry over to the real engines.
 */
class SimulatedPlayer(
    scope: CoroutineScope,
    private val tickMs: Long = 250,
    private val random: Random = Random.Default,
) : Player {

    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    /** Queue in the order it was requested, restored when shuffle is turned off. */
    private var originalQueue: List<Track> = emptyList()

    init {
        scope.launch {
            while (isActive) {
                delay(tickMs)
                tick()
            }
        }
    }

    private fun tick() {
        val s = mutableState.value
        if (!s.isPlaying || s.current == null) return
        val position = s.positionMs + tickMs
        if (position >= s.durationMs) onTrackEnded() else mutableState.value = s.copy(positionMs = position)
    }

    private fun onTrackEnded() {
        val s = mutableState.value
        when {
            s.repeat == RepeatMode.One -> mutableState.value = s.copy(positionMs = 0)
            else -> advance()
        }
    }

    override fun play(queue: List<Track>, startIndex: Int) {
        if (queue.isEmpty()) return
        val start = startIndex.coerceIn(queue.indices)
        originalQueue = queue
        mutableState.update { s ->
            if (s.shuffle) {
                s.copy(queue = shuffledAround(queue, queue[start]), index = 0, positionMs = 0, isPlaying = true)
            } else {
                s.copy(queue = queue, index = start, positionMs = 0, isPlaying = true)
            }
        }
    }

    override fun togglePlayPause() {
        mutableState.update { s -> if (s.current == null) s else s.copy(isPlaying = !s.isPlaying) }
    }

    override fun skipToNext() = advance()

    /** Moves to the next track; past the end it wraps if repeating, else rests paused on the first. */
    private fun advance() {
        mutableState.update { s ->
            if (s.current == null) return@update s
            val next = s.index + 1
            when {
                next < s.queue.size -> s.copy(index = next, positionMs = 0)
                s.repeat == RepeatMode.All -> s.copy(index = 0, positionMs = 0)
                else -> s.copy(index = 0, positionMs = 0, isPlaying = false)
            }
        }
    }

    override fun skipToPrevious() {
        mutableState.update { s ->
            if (s.current == null) return@update s
            val previous = s.index - 1
            when {
                // Like most players: a few seconds in, "previous" restarts the song.
                s.positionMs > RESTART_THRESHOLD_MS -> s.copy(positionMs = 0)
                previous >= 0 -> s.copy(index = previous, positionMs = 0)
                s.repeat == RepeatMode.All -> s.copy(index = s.queue.lastIndex, positionMs = 0)
                else -> s.copy(positionMs = 0)
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        mutableState.update { s -> s.copy(positionMs = positionMs.coerceIn(0, s.durationMs)) }
    }

    override fun toggleShuffle() {
        mutableState.update { s ->
            val current = s.current ?: return@update s.copy(shuffle = !s.shuffle)
            if (s.shuffle) {
                s.copy(
                    shuffle = false,
                    queue = originalQueue,
                    index = originalQueue.indexOfFirst { it.id == current.id }.coerceAtLeast(0),
                )
            } else {
                s.copy(shuffle = true, queue = shuffledAround(originalQueue, current), index = 0)
            }
        }
    }

    override fun cycleRepeatMode() {
        mutableState.update { s ->
            val next = when (s.repeat) {
                RepeatMode.Off -> RepeatMode.All
                RepeatMode.All -> RepeatMode.One
                RepeatMode.One -> RepeatMode.Off
            }
            s.copy(repeat = next)
        }
    }

    override fun setRepeatMode(mode: RepeatMode) {
        mutableState.update { it.copy(repeat = mode) }
    }

    /** The current track first, then everything else in random order. */
    private fun shuffledAround(queue: List<Track>, current: Track): List<Track> =
        listOf(current) + queue.filter { it.id != current.id }.shuffled(random)

    private companion object {
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
