package com.maestro.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The app's own icon set on a 24-unit grid: 1.8 strokes with round caps for line
 * icons, and filled glyphs with softened corners for transport controls.
 */
object MaestroIcons {

    val Play: ImageVector by lazy {
        icon("Play") { filled { moveTo(7.5f, 4.8f); lineTo(19.2f, 12f); lineTo(7.5f, 19.2f); close() } }
    }

    val Pause: ImageVector by lazy {
        icon("Pause") {
            filled { moveTo(6.5f, 5f); lineTo(9.5f, 5f); lineTo(9.5f, 19f); lineTo(6.5f, 19f); close() }
            filled { moveTo(14.5f, 5f); lineTo(17.5f, 5f); lineTo(17.5f, 19f); lineTo(14.5f, 19f); close() }
        }
    }

    val Next: ImageVector by lazy {
        icon("Next") {
            filled { moveTo(3.5f, 6.5f); lineTo(11.5f, 12f); lineTo(3.5f, 17.5f); close() }
            filled { moveTo(12.5f, 6.5f); lineTo(20.5f, 12f); lineTo(12.5f, 17.5f); close() }
        }
    }

    val Previous: ImageVector by lazy {
        icon("Previous") {
            filled { moveTo(20.5f, 6.5f); lineTo(12.5f, 12f); lineTo(20.5f, 17.5f); close() }
            filled { moveTo(11.5f, 6.5f); lineTo(3.5f, 12f); lineTo(11.5f, 17.5f); close() }
        }
    }

    val Shuffle: ImageVector by lazy {
        icon("Shuffle") {
            stroked {
                moveTo(3f, 7f); lineTo(6f, 7f)
                curveTo(10.5f, 7f, 11.5f, 17f, 16f, 17f); lineTo(20.5f, 17f)
            }
            stroked {
                moveTo(3f, 17f); lineTo(6f, 17f)
                curveTo(10.5f, 17f, 11.5f, 7f, 16f, 7f); lineTo(20.5f, 7f)
            }
            stroked { moveTo(18f, 4.5f); lineTo(20.5f, 7f); lineTo(18f, 9.5f) }
            stroked { moveTo(18f, 14.5f); lineTo(20.5f, 17f); lineTo(18f, 19.5f) }
        }
    }

    val Repeat: ImageVector by lazy { icon("Repeat") { repeatArrows() } }

    val RepeatOne: ImageVector by lazy {
        icon("RepeatOne") {
            repeatArrows()
            stroked(width = 1.6f) { moveTo(11f, 10.6f); lineTo(12.4f, 9.6f); lineTo(12.4f, 14.4f) }
        }
    }

    val ChevronDown: ImageVector by lazy {
        icon("ChevronDown") { stroked(width = 2.2f) { moveTo(6f, 9.5f); lineTo(12f, 15f); lineTo(18f, 9.5f) } }
    }

    /** Material-style back arrow: Android users read a bare chevron as "previous", not "back". */
    val ArrowBack: ImageVector by lazy {
        icon("ArrowBack") {
            stroked(width = 2f) { moveTo(19.5f, 12f); lineTo(4.5f, 12f) }
            stroked(width = 2f) { moveTo(11f, 5.5f); lineTo(4.5f, 12f); lineTo(11f, 18.5f) }
        }
    }

    val ChevronRight: ImageVector by lazy {
        icon("ChevronRight") { stroked(width = 2.2f) { moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f) } }
    }

    val ChevronLeft: ImageVector by lazy {
        icon("ChevronLeft") { stroked(width = 2.2f) { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) } }
    }

    val Albums: ImageVector by lazy {
        icon("Albums") {
            stroked { roundedRect(3.5f, 7.5f, 13f, 13f, 2f) }
            stroked { moveTo(7.5f, 4f); lineTo(18.5f, 4f); quadTo(20.5f, 4f, 20.5f, 6f); lineTo(20.5f, 17f) }
        }
    }

    val Artists: ImageVector by lazy {
        icon("Artists") {
            stroked { circle(12f, 8f, 3.8f) }
            stroked { moveTo(4.5f, 20f); curveTo(4.5f, 15.8f, 8f, 14f, 12f, 14f); curveTo(16f, 14f, 19.5f, 15.8f, 19.5f, 20f) }
        }
    }

    val Songs: ImageVector by lazy {
        icon("Songs") {
            stroked { moveTo(9f, 17.5f); lineTo(9f, 5.5f); lineTo(19f, 3.8f); lineTo(19f, 15.5f) }
            stroked { moveTo(9f, 9f); lineTo(19f, 7.3f) }
            filled { circle(6.5f, 17.5f, 2.5f) }
            filled { circle(16.5f, 15.5f, 2.5f) }
        }
    }

    private fun ImageVector.Builder.repeatArrows() {
        stroked {
            moveTo(4f, 11.5f); lineTo(4f, 9.5f); quadTo(4f, 7f, 6.5f, 7f); lineTo(19.5f, 7f)
        }
        stroked { moveTo(17f, 4.5f); lineTo(19.5f, 7f); lineTo(17f, 9.5f) }
        stroked {
            moveTo(20f, 12.5f); lineTo(20f, 14.5f); quadTo(20f, 17f, 17.5f, 17f); lineTo(4.5f, 17f)
        }
        stroked { moveTo(7f, 14.5f); lineTo(4.5f, 17f); lineTo(7f, 19.5f) }
    }
}

private inline fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(block).build()

private fun ImageVector.Builder.stroked(width: Float = 1.8f, pathBuilder: PathBuilder.() -> Unit) {
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder,
    )
}

/** A fill plus a round-joined stroke of the same color softens the corners. */
private fun ImageVector.Builder.filled(pathBuilder: PathBuilder.() -> Unit) {
    path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.6f,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder,
    )
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 2 * r, dy1 = 0f)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2 * r, dy1 = 0f)
    close()
}

private fun PathBuilder.roundedRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
    moveTo(x + r, y)
    lineTo(x + w - r, y); quadTo(x + w, y, x + w, y + r)
    lineTo(x + w, y + h - r); quadTo(x + w, y + h, x + w - r, y + h)
    lineTo(x + r, y + h); quadTo(x, y + h, x, y + h - r)
    lineTo(x, y + r); quadTo(x, y, x + r, y)
    close()
}
