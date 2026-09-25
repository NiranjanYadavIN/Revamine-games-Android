package com.revamine.games.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = RevaVioletPrimary,
    secondary = RevaAmberAccent,
    tertiary = RevaEmeraldAccent,
    background = RevaBgDark,
    surface = RevaSurfaceDark,
    onBackground = RevaTextDark,
    onSurface = RevaTextDark
)

private val LightColors = lightColorScheme(
    primary = RevaVioletPrimary,
    secondary = RevaAmberAccent,
    tertiary = RevaEmeraldAccent,
    background = RevaBgLight,
    surface = RevaSurfaceLight,
    onBackground = RevaTextLight,
    onSurface = RevaTextLight
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
