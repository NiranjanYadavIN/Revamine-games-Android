package com.revamine.games.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = RevaIndigoPrimary,
    secondary = RevaRoseAccent,
    tertiary = RevaEmeraldAccent,
    background = RevaBgDark,
    surface = RevaSurfaceDark,
    surfaceVariant = RevaCardDark,
    outline = RevaBorderDark,
    onBackground = RevaTextDark,
    onSurface = RevaTextDark,
    onSurfaceVariant = RevaTextMutedDark
)

private val LightColors = lightColorScheme(
    primary = RevaIndigoPrimary,
    secondary = RevaRoseAccent,
    tertiary = RevaEmeraldAccent,
    background = RevaBgLight,
    surface = RevaSurfaceLight,
    surfaceVariant = RevaCardLight,
    outline = RevaBorderLight,
    onBackground = RevaTextLight,
    onSurface = RevaTextLight,
    onSurfaceVariant = RevaTextMutedLight
)

@Composable
fun RevaMineGamesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = RevaTypography,
        content = content
    )
}
