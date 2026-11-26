package com.windrr.jibrro.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Station-signage palette: navy sign boards, platform tile grays, safety yellow accents.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1D3557),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE7F3),
    onPrimaryContainer = Color(0xFF0F2138),
    secondary = Color(0xFF4A5A6E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E9EF),
    onSecondaryContainer = Color(0xFF1E2A38),
    tertiary = Color(0xFF8A6500),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE7A3),
    onTertiaryContainer = Color(0xFF3D2C00),
    background = Color(0xFFEDF0F4),
    onBackground = Color(0xFF131B26),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF131B26),
    surfaceVariant = Color(0xFFE3E8EE),
    onSurfaceVariant = Color(0xFF4A5666),
    outline = Color(0xFF6E7A8A),
    outlineVariant = Color(0xFFCDD5DF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAFC9EC),
    onPrimary = Color(0xFF0D223D),
    primaryContainer = Color(0xFF253954),
    onPrimaryContainer = Color(0xFFD8E6F8),
    secondary = Color(0xFFB7C3D3),
    onSecondary = Color(0xFF212C3A),
    secondaryContainer = Color(0xFF2D3846),
    onSecondaryContainer = Color(0xFFDCE4EE),
    tertiary = Color(0xFFFFCD3C),
    onTertiary = Color(0xFF2E2200),
    tertiaryContainer = Color(0xFF4A3A0A),
    onTertiaryContainer = Color(0xFFFFE7A3),
    background = Color(0xFF0E131A),
    onBackground = Color(0xFFE3E9F1),
    surface = Color(0xFF171E28),
    onSurface = Color(0xFFE3E9F1),
    surfaceVariant = Color(0xFF222B37),
    onSurfaceVariant = Color(0xFFB6C1CF),
    outline = Color(0xFF8592A3),
    outlineVariant = Color(0xFF333E4C)
)

@Composable
fun JibrroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
