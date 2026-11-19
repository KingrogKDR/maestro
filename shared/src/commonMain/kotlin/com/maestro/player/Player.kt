package com.maestro.player

import com.maestro.model.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Playback contract the UI talks to. Platform engines (Media3 on Android,
 * a native engine on desktop) will implement this in a later chunk.
 */
interface Player {
    val state: StateFlow<PlayerState>

    fun play(queue: List<Track>, startIndex: Int = 0)
    fun togglePlayPause()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
    fun toggleShuffle()
    fun cycleRepeatMode()
    fun setRepeatMode(mode: RepeatMode)
}

enum class RepeatMode { Off, All, One }

data class PlayerState(
    val queue: List<Track> = emptyList(),
    val index: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.Off,
) {
    val current: Track? get() = queue.getOrNull(index)
    val durationMs: Long get() = current?.durationMs ?: 0
}
