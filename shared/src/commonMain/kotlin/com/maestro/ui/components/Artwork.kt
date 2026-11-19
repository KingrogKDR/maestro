package com.maestro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.maestro.model.Artwork
import com.maestro.ui.theme.Maestro
import com.maestro.ui.theme.Radius
import kotlin.random.Random

@Immutable
data class ArtPalette(
    /** The dominant color, used to tint surfaces behind the artwork. */
    val primary: Color,
    val secondary: Color,
    val highlight: Color,
)

private class GeneratedArt(val palette: ArtPalette, val motif: Int, val random: Long)

private fun generate(seed: String): GeneratedArt {
    val random = Random(fnv1a(seed))
    val hue = random.nextFloat() * 360f
    val shift = 25f + random.nextFloat() * 55f
    val saturation = 0.45f + random.nextFloat() * 0.3f
    val palette = ArtPalette(
        primary = Color.hsv(hue, saturation, 0.42f + random.nextFloat() * 0.18f),
        secondary = Color.hsv((hue + shift) % 360f, saturation + 0.1f, 0.7f + random.nextFloat() * 0.2f),
        highlight = Color.hsv((hue + 180f + shift) % 360f, 0.25f, 0.98f),
    )
    return GeneratedArt(palette, motif = random.nextInt(4), random = random.nextLong())
}

fun artworkPalette(artwork: Artwork): ArtPalette = when (artwork) {
    is Artwork.Generated -> generate(artwork.seed).palette
}

@Composable
fun ArtworkImage(
    artwork: Artwork,
    modifier: Modifier = Modifier,
    shape: Shape = Radius.medium,
) {
    val art = remember(artwork) {
        when (artwork) {
            is Artwork.Generated -> generate(artwork.seed)
        }
    }
    Box(
        modifier
            .clip(shape)
            .border(0.5.dp, Maestro.colors.hairline, shape),
    ) {
        Canvas(Modifier.fillMaxSize()) { drawGenerated(art) }
    }
}

private fun DrawScope.drawGenerated(art: GeneratedArt) {
    val p = art.palette
    val w = size.width
    val h = size.height
    drawRect(Brush.linearGradient(listOf(p.primary, p.secondary), Offset.Zero, Offset(w, h)))
    val random = Random(art.random)
    when (art.motif) {
        0 -> { // Low sun over a horizon band.
            val c = Offset(w * (0.35f + random.nextFloat() * 0.3f), h * 0.42f)
            drawCircle(p.highlight.copy(alpha = 0.85f), radius = w * 0.24f, center = c)
            drawRect(p.primary.copy(alpha = 0.55f), topLeft = Offset(0f, h * 0.62f))
        }
        1 -> { // Concentric rings radiating from a corner.
            val c = Offset(w * random.nextFloat(), h * (0.6f + random.nextFloat() * 0.4f))
            for (i in 1..7) {
                drawCircle(
                    p.highlight.copy(alpha = 0.42f - i * 0.04f),
                    radius = w * 0.13f * i,
                    center = c,
                    style = Stroke(width = w * 0.025f),
                )
            }
        }
        2 -> { // Diagonal bands.
            rotate(-28f) {
                for (i in 0..3) {
                    drawRect(
                        p.highlight.copy(alpha = 0.16f + i * 0.08f),
                        topLeft = Offset(-w * 0.5f, h * (0.05f + i * 0.24f)),
                        size = androidx.compose.ui.geometry.Size(w * 2f, h * 0.09f),
                    )
                }
            }
        }
        else -> { // A field of dots that swell toward one side.
            val n = 6
            val step = w / n
            for (x in 0 until n) for (y in 0 until n) {
                val t = (x + y) / (2f * (n - 1))
                drawCircle(
                    p.highlight.copy(alpha = 0.25f + 0.6f * t),
                    radius = step * (0.08f + 0.28f * t),
                    center = Offset(step * (x + 0.5f), step * (y + 0.5f) * (h / w)),
                )
            }
        }
    }
    // A soft vignette gives the cover some depth.
    drawRect(
        Brush.radialGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
            center = Offset(w / 2, h / 2),
            radius = maxOf(w, h) * 0.75f,
        ),
    )
}

/** FNV-1a: a stable hash so the same seed always draws the same cover on every platform. */
private fun fnv1a(text: String): Long {
    var hash = -0x340d631b7bdddcdbL
    for (ch in text) {
        hash = hash xor ch.code.toLong()
        hash *= 0x100000001b3L
    }
    return hash
}
