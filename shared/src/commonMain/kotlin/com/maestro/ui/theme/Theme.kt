package com.maestro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
data class MaestroColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val hairline: Color,
    val accent: Color,
    val onAccent: Color,
    val scrim: Color,
)

/*
 * Neutral surfaces so cover art carries the color; one brass accent (concert-hall
 * brass, for a music app called Maestro) marks what's interactive, selected or current.
 * Every text token is at least 4.5:1 on its surface in both appearances.
 */
private val DarkColors = MaestroColors(
    isDark = true,
    background = Color(0xFF0E0E10),
    surface = Color(0xFF18181B),
    surfaceRaised = Color(0xFF232327),
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFFF5F5F7).copy(alpha = 0.68f), // 8.4:1 on background
    textTertiary = Color(0xFFF5F5F7).copy(alpha = 0.52f), // 5.3:1 on background
    hairline = Color.White.copy(alpha = 0.08f),
    accent = Color(0xFFD9A441), // 8.6:1 on background
    onAccent = Color(0xFF1A1204),
    scrim = Color.Black.copy(alpha = 0.55f),
)

private val LightColors = MaestroColors(
    isDark = false,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFF0F0F2),
    textPrimary = Color(0xFF111113),
    textSecondary = Color(0xFF111113).copy(alpha = 0.72f), // 7.6:1 on background
    textTertiary = Color(0xFF111113).copy(alpha = 0.60f), // 4.9:1 on background
    hairline = Color.Black.copy(alpha = 0.08f),
    accent = Color(0xFF8A5F0E), // 5.4:1 on background, 4.9:1 on raised surfaces
    onAccent = Color.White,
    scrim = Color.Black.copy(alpha = 0.35f),
)

/**
 * Type scale. Tracking tightens as size grows and loosens slightly for small
 * labels; leading is tight on large type and roomier on body copy.
 */
@Immutable
data class MaestroType(
    val largeTitle: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.022).em),
    val title: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.016).em),
    val headline: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
    val body: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.em),
    val callout: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.em),
    val caption: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.01.em),
    val label: TextStyle = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.02.em),
    /** Tabular figures keep time readouts from jittering as digits change. */
    val numeric: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
)

object Radius {
    val small = RoundedCornerShape(6.dp)
    val medium = RoundedCornerShape(10.dp)
    val large = RoundedCornerShape(16.dp)
}

private val LocalColors = staticCompositionLocalOf { DarkColors }
private val LocalReduceMotion = staticCompositionLocalOf { false }
private val LocalType = staticCompositionLocalOf { MaestroType() }

object Maestro {
    val colors: MaestroColors @Composable get() = LocalColors.current
    val type: MaestroType @Composable get() = LocalType.current

    /** The system asked for less motion: swap slides and scaling for fades. */
    val reduceMotion: Boolean @Composable get() = LocalReduceMotion.current
}

@Composable
fun MaestroTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    val type = MaestroType()
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent, onPrimary = colors.onAccent,
            background = colors.background, onBackground = colors.textPrimary,
            surface = colors.surface, onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary, outlineVariant = colors.hairline,
        )
    } else {
        lightColorScheme(
            primary = colors.accent, onPrimary = colors.onAccent,
            background = colors.background, onBackground = colors.textPrimary,
            surface = colors.surface, onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary, outlineVariant = colors.hairline,
        )
    }
    val reduceMotion = rememberReduceMotion()
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalType provides type,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography(bodyLarge = type.body, bodyMedium = type.body),
            content = content,
        )
    }
}
