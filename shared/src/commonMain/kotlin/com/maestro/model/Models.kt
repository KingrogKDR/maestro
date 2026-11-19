package com.maestro.model

/**
 * Core library models. These are deliberately source-agnostic: a track can come
 * from local storage, a self-hosted server, or the built-in sample library.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val albumId: String,
    val albumTitle: String,
    val trackNumber: Int,
    val durationMs: Long,
    val artwork: Artwork,
    /** Where the audio lives. Null for sample tracks that have no audio yet. */
    val mediaUri: String? = null,
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    val artwork: Artwork,
    val tracks: List<Track>,
) {
    val durationMs: Long get() = tracks.sumOf { it.durationMs }
}

data class Artist(
    val name: String,
    val albums: List<Album>,
) {
    val trackCount: Int get() = albums.sumOf { it.tracks.size }
}

/** How a cover is drawn. Real cover images will be added alongside [Generated]. */
sealed interface Artwork {
    /** A deterministic abstract cover drawn from [seed], used until real art exists. */
    data class Generated(val seed: String) : Artwork
}
