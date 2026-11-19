package com.maestro.ui.shell

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.maestro.ui.icons.MaestroIcons

enum class Section(val label: String, val icon: ImageVector) {
    Albums("Albums", MaestroIcons.Albums),
    Artists("Artists", MaestroIcons.Artists),
    Songs("Songs", MaestroIcons.Songs),
}

sealed interface Destination {
    val key: String

    data class Root(val section: Section) : Destination {
        override val key get() = "root:${section.name}"
    }

    data class AlbumDetail(val albumId: String) : Destination {
        override val key get() = "album:$albumId"
    }

    data class ArtistDetail(val name: String) : Destination {
        override val key get() = "artist:$name"
    }
}

/** A tab plus a stack of detail screens on top of it. */
@Stable
class Navigator {
    var section by mutableStateOf(Section.Albums)
        private set

    private val stack = mutableStateListOf<Destination>()

    val current: Destination get() = stack.lastOrNull() ?: Destination.Root(section)
    val depth: Int get() = stack.size
    val canGoBack: Boolean get() = stack.isNotEmpty()

    fun open(destination: Destination) {
        if (current != destination) stack.add(destination)
    }

    fun back(): Boolean = stack.removeLastOrNull() != null

    /** Selecting the current tab again returns to its top level. */
    fun select(target: Section) {
        stack.clear()
        section = target
    }
}
