package com.maestro.data

import com.maestro.model.Album
import com.maestro.model.Artist
import com.maestro.model.Track
import kotlinx.coroutines.flow.StateFlow

/** Read access to the music library, independent of where the music comes from. */
interface LibraryRepository {
    val library: StateFlow<Library>
}

data class Library(
    val albums: List<Album> = emptyList(),
) {
    val tracks: List<Track> by lazy {
        albums.flatMap { it.tracks }.sortedBy { it.title.lowercase() }
    }

    val artists: List<Artist> by lazy {
        albums.groupBy { it.artist }
            .map { (name, albums) -> Artist(name, albums.sortedByDescending { it.year ?: 0 }) }
            .sortedBy { it.name.lowercase() }
    }

    fun album(id: String): Album? = albums.firstOrNull { it.id == id }

    fun artist(name: String): Artist? = artists.firstOrNull { it.name == name }
}
