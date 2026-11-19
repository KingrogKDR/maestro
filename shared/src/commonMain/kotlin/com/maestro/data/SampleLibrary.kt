package com.maestro.data

import com.maestro.model.Album
import com.maestro.model.Artwork
import com.maestro.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A small fictional library so every screen has something real to show
 * before a music source is connected.
 */
class SampleLibraryRepository : LibraryRepository {
    private val state = MutableStateFlow(Library(sampleAlbums()))
    override val library: StateFlow<Library> = state.asStateFlow()
}

private class AlbumSpec(
    val title: String,
    val artist: String,
    val year: Int,
    val tracks: List<Pair<String, String>>, // title to "m:ss"
)

private fun sampleAlbums(): List<Album> = listOf(
    AlbumSpec(
        "Night Ferries", "Lumen Coast", 2024,
        listOf(
            "Harbour Lights" to "3:42", "Undertow" to "4:15", "Salt on the Window" to "3:08",
            "Last Crossing" to "5:01", "Lanterns" to "3:33", "Pier Seventeen" to "4:47",
            "The Quiet Deck" to "2:58", "Morning Docks" to "6:12",
        ),
    ),
    AlbumSpec(
        "Paper Satellites", "Mira Okonkwo", 2023,
        listOf(
            "Orbit" to "3:21", "Small Gravity" to "3:55", "Paper Satellites" to "4:30",
            "Signal Fade" to "3:12", "Low Earth" to "4:02", "Reentry" to "5:18",
        ),
    ),
    AlbumSpec(
        "Velvet Arcade", "Halcyon Static", 2022,
        listOf(
            "Insert Coin" to "2:44", "Neon Mile" to "3:37", "High Score" to "3:59",
            "Attract Mode" to "4:21", "Continue?" to "3:05", "Two Player" to "3:48",
            "Game Over Sunrise" to "5:26",
        ),
    ),
    AlbumSpec(
        "North of Juniper", "Saltgrass", 2021,
        listOf(
            "Juniper" to "4:11", "Dry Creek" to "3:29", "Fence Line" to "3:54",
            "Coyote Hour" to "4:40", "Long Road Home" to "5:33",
        ),
    ),
    AlbumSpec(
        "Glasshouse", "Mira Okonkwo", 2020,
        listOf(
            "Condensation" to "3:16", "Fern" to "2:51", "Glasshouse" to "4:24",
            "Humid" to "3:39", "Bloom Cycle" to "4:58", "Open the Vents" to "3:27",
        ),
    ),
    AlbumSpec(
        "Long Shadows", "Kōji & the Late Hours", 2025,
        listOf(
            "Five O'Clock" to "3:50", "Streetlamp Waltz" to "4:36", "Long Shadows" to "5:12",
            "Taxi Rank" to "3:03", "Closing Time Again" to "4:19", "Blue Hour" to "6:40",
        ),
    ),
    AlbumSpec(
        "Field Recordings", "Lumen Coast", 2019,
        listOf(
            "Wind Study" to "2:30", "Rain on Tin" to "3:10", "Gulls" to "1:58",
            "Low Tide" to "4:05", "Fog Horn" to "3:44",
        ),
    ),
    AlbumSpec(
        "Afterimage", "Ostra", 2024,
        listOf(
            "Afterimage" to "3:58", "Persistence" to "4:12", "Retina" to "3:24",
            "Flicker Fusion" to "4:49", "Dark Adaptation" to "5:06", "Phosphene" to "3:31",
            "Blink" to "2:22",
        ),
    ),
).map { spec ->
    val albumId = slug("${spec.artist}-${spec.title}")
    val artwork = Artwork.Generated(albumId)
    Album(
        id = albumId,
        title = spec.title,
        artist = spec.artist,
        year = spec.year,
        artwork = artwork,
        tracks = spec.tracks.mapIndexed { index, (title, length) ->
            Track(
                id = "$albumId-${index + 1}",
                title = title,
                artist = spec.artist,
                albumId = albumId,
                albumTitle = spec.title,
                trackNumber = index + 1,
                durationMs = parseLength(length),
                artwork = artwork,
            )
        },
    )
}

private fun slug(text: String): String =
    text.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

private fun parseLength(text: String): Long {
    val (minutes, seconds) = text.split(":").map { it.toLong() }
    return (minutes * 60 + seconds) * 1000
}
