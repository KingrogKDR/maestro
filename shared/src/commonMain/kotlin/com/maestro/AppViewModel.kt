package com.maestro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.data.Library
import com.maestro.data.LibraryRepository
import com.maestro.data.SampleLibraryRepository
import com.maestro.model.Track
import com.maestro.player.Player
import com.maestro.player.SimulatedPlayer
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

/** Owns the long-lived pieces so they survive rotation and window changes. */
class AppViewModel(
    repository: LibraryRepository = SampleLibraryRepository(),
) : ViewModel() {
    val library: StateFlow<Library> = repository.library
    val player: Player = SimulatedPlayer(viewModelScope)

    fun play(queue: List<Track>, startIndex: Int) = player.play(queue, startIndex)

    fun playShuffled(queue: List<Track>) {
        if (queue.isEmpty()) return
        if (!player.state.value.shuffle) player.toggleShuffle()
        player.play(queue, Random.nextInt(queue.size))
    }
}
