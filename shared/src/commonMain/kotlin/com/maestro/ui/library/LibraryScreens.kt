package com.maestro.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maestro.data.Library
import com.maestro.model.Album
import com.maestro.model.Track
import com.maestro.ui.components.ArtworkImage
import com.maestro.ui.components.artworkPalette
import com.maestro.ui.components.formatLongDuration
import com.maestro.ui.components.pressable
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Radius

/** Everything a library screen can ask the app to do. */
class LibraryActions(
    val openAlbum: (albumId: String) -> Unit,
    val openArtist: (name: String) -> Unit,
    val play: (queue: List<Track>, startIndex: Int) -> Unit,
    val shuffle: (queue: List<Track>) -> Unit,
    val back: () -> Unit,
)

private val GutterNarrow = 16.dp
private val GutterWide = 28.dp

/** Lists and detail pages stop growing here so rows stay scannable on wide windows. */
private val ReadableWidth = 880.dp

/** Side padding for a list: gutter on the start edge, and on wide windows enough end padding to cap the width. */
private fun listPadding(available: Dp, gutter: Dp, padding: PaddingValues): PaddingValues {
    val start = gutter - 8.dp
    val end = maxOf(gutter - 8.dp, available - start - ReadableWidth)
    return PaddingValues(
        start = start,
        end = end,
        top = padding.calculateTopPadding(),
        bottom = padding.calculateBottomPadding() + 16.dp,
    )
}

@Composable
fun AlbumsScreen(library: Library, actions: LibraryActions, padding: PaddingValues, wide: Boolean) {
    AlbumGrid(
        albums = library.albums.sortedBy { it.title.lowercase() },
        actions = actions,
        padding = padding,
        wide = wide,
        header = { ScreenTitle("Albums") },
    )
}

@Composable
private fun AlbumGrid(
    albums: List<Album>,
    actions: LibraryActions,
    padding: PaddingValues,
    wide: Boolean,
    showArtist: Boolean = true,
    header: @Composable () -> Unit,
) {
    val gutter = if (wide) GutterWide else GutterNarrow
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = if (wide) 180.dp else 150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = gutter,
            end = gutter,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 16.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { header() }
        items(albums, key = { it.id }) { album ->
            AlbumCard(album, showArtist, onClick = { actions.openAlbum(album.id) })
        }
    }
}

@Composable
private fun AlbumCard(album: Album, showArtist: Boolean, onClick: () -> Unit) {
    Column(Modifier.pressable(onClick, pressedScale = 0.96f)) {
        ArtworkImage(album.artwork, Modifier.fillMaxWidth().aspectRatio(1f))
        Spacer(Modifier.height(8.dp))
        Text(
            album.title,
            style = Maestro.type.callout,
            color = Maestro.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            if (showArtist) album.artist else album.year?.toString().orEmpty(),
            style = Maestro.type.caption,
            color = Maestro.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ArtistsScreen(library: Library, actions: LibraryActions, padding: PaddingValues, wide: Boolean) {
    val gutter = if (wide) GutterWide else GutterNarrow
    BoxWithConstraints(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = listPadding(maxWidth, gutter, padding),
        ) {
            item { ScreenTitle("Artists", Modifier.padding(start = 8.dp)) }
            items(library.artists, key = { it.name }) { artist ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.medium)
                        .pressable({ actions.openArtist(artist.name) }, pressedScale = 0.985f, highlight = Maestro.colors.hairline, shape = Radius.medium)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ArtworkImage(artist.albums.first().artwork, Modifier.size(52.dp), shape = CircleArt)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(artist.name, style = Maestro.type.headline, color = Maestro.colors.textPrimary)
                        val albums = artist.albums.size
                        Text(
                            "$albums ${if (albums == 1) "album" else "albums"} · ${artist.trackCount} songs",
                            style = Maestro.type.caption,
                            color = Maestro.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongsScreen(
    library: Library,
    nowPlaying: NowPlayingInfo,
    actions: LibraryActions,
    padding: PaddingValues,
    wide: Boolean,
) {
    val gutter = if (wide) GutterWide else GutterNarrow
    val tracks = library.tracks
    BoxWithConstraints(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = listPadding(maxWidth, gutter, padding),
        ) {
            item {
                Column(Modifier.padding(horizontal = 8.dp)) {
                    ScreenTitle("Songs")
                    PlayShuffleRow(
                        onPlay = { actions.play(tracks, 0) },
                        onShuffle = { actions.shuffle(tracks) },
                        modifier = Modifier.widthIn(max = 420.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
                TrackRow(track, nowPlaying, onClick = { actions.play(tracks, index) })
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    nowPlaying: NowPlayingInfo,
    actions: LibraryActions,
    padding: PaddingValues,
    wide: Boolean,
) {
    val gutter = if (wide) GutterWide else GutterNarrow
    val listState = rememberLazyListState()
    val palette = artworkPalette(album.artwork)
    val wash = palette.primary.copy(alpha = if (Maestro.colors.isDark) 0.30f else 0.20f)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sideBySide = maxWidth >= 640.dp
        val washHeight = padding.calculateTopPadding() + if (sideBySide) 340.dp else 560.dp
        // The cover's color washes down behind the header and scrolls away with it,
        // the same language as Now Playing, so the album and its player feel like one object.
        Box(
            Modifier
                .fillMaxWidth()
                .height(washHeight)
                .graphicsLayer {
                    translationY = if (listState.firstVisibleItemIndex == 0) {
                        -listState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        -size.height
                    }
                }
                .background(Brush.verticalGradient(0f to wash, 0.55f to wash.copy(alpha = wash.alpha * 0.5f), 1f to wash.copy(alpha = 0f))),
        )
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = listPadding(maxWidth, gutter, padding),
        ) {
            item {
                Column(Modifier.padding(horizontal = 8.dp)) {
                    BackButton(actions.back)
                    if (sideBySide) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            ArtworkImage(album.artwork, Modifier.size(240.dp), shape = Radius.large)
                            Spacer(Modifier.width(24.dp))
                            AlbumHeaderText(album, actions, Modifier.weight(1f), centered = false)
                        }
                    } else {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ArtworkImage(
                                album.artwork,
                                Modifier.fillMaxWidth(0.72f).widthIn(max = 300.dp).aspectRatio(1f),
                                shape = Radius.large,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        AlbumHeaderText(album, actions, Modifier.fillMaxWidth(), centered = true)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            items(album.tracks, key = { it.id }) { track ->
                TrackRow(
                    track,
                    nowPlaying,
                    onClick = { actions.play(album.tracks, track.trackNumber - 1) },
                    showArtwork = false,
                    subtitle = if (track.artist != album.artist) track.artist else "",
                )
            }
            item {
                Text(
                    "${album.tracks.size} songs, ${formatLongDuration(album.durationMs)}",
                    style = Maestro.type.caption,
                    color = Maestro.colors.textTertiary,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun AlbumHeaderText(album: Album, actions: LibraryActions, modifier: Modifier, centered: Boolean) {
    val align = if (centered) Alignment.CenterHorizontally else Alignment.Start
    Column(modifier, horizontalAlignment = align) {
        Text(album.title, style = Maestro.type.title, color = Maestro.colors.textPrimary)
        Text(
            album.artist,
            style = Maestro.type.headline,
            color = Maestro.colors.accent,
            modifier = Modifier.pressable({ actions.openArtist(album.artist) }, pressedScale = 0.97f),
        )
        album.year?.let {
            Text("$it", style = Maestro.type.caption, color = Maestro.colors.textSecondary)
        }
        Spacer(Modifier.height(16.dp))
        // Play is the album's own color (filled); Shuffle stays tonal.
        PlayShuffleRow(
            onPlay = { actions.play(album.tracks, 0) },
            onShuffle = { actions.shuffle(album.tracks) },
            modifier = Modifier.widthIn(max = 420.dp),
            playContainer = lerp(artworkPalette(album.artwork).primary, Color.Black, 0.28f),
            playContent = Color.White,
        )
    }
}

@Composable
fun ArtistDetailScreen(
    library: Library,
    name: String,
    actions: LibraryActions,
    padding: PaddingValues,
    wide: Boolean,
) {
    val artist = library.artist(name) ?: return
    val tracks = artist.albums.flatMap { it.tracks }
    AlbumGrid(
        albums = artist.albums,
        actions = actions,
        padding = padding,
        wide = wide,
        showArtist = false,
        header = {
            Column {
                BackButton(actions.back)
                ScreenTitle(artist.name)
                PlayShuffleRow(
                    onPlay = { actions.play(tracks, 0) },
                    onShuffle = { actions.shuffle(tracks) },
                    modifier = Modifier.widthIn(max = 420.dp),
                )
                SectionLabel("Albums")
            }
        },
    )
}
