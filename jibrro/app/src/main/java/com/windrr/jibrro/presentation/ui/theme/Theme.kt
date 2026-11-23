package com.windrr.jibrro.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF17654F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F3E7),
    onPrimaryContainer = Color(0xFF123D32),
    secondary = Color(0xFF4B665B),
    secondaryContainer = Color(0xFFE7F1DC),
    onSecondaryContainer = Color(0xFF253822),
    tertiary = Color(0xFF795221),
    tertiaryContainer = Color(0xFFFFE9C5),
    onTertiaryContainer = Color(0xFF522F0D),
    background = Color(0xFFF6F7F1),
    onBackground = Color(0xFF192F28),
    surface = Color(0xFFFFFFFB),
    onSurface = Color(0xFF192F28),
    surfaceVariant = Color(0xFFE8EDE4),
    onSurfaceVariant = Color(0xFF495D52),
    outline = Color(0xFF698074),
    outlineVariant = Color(0xFFD2DDD2)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9CDABF),
    onPrimary = Color(0xFF14392C),
    primaryContainer = Color(0xFF21463A),
    onPrimaryContainer = Color(0xFFC8F4DF),
    secondary = Color(0xFFB1CCBD),
    secondaryContainer = Color(0xFF304338),
    onSecondaryContainer = Color(0xFFE2F1E4),
    tertiary = Color(0xFFFFD399),
    tertiaryContainer = Color(0xFF563D22),
    onTertiaryContainer = Color(0xFFFFEBD1),
    background = Color(0xFF12211C),
    onBackground = Color(0xFFE6F1E9),
    surface = Color(0xFF1B2B24),
    onSurface = Color(0xFFE6F1E9),
    surfaceVariant = Color(0xFF25392F),
    onSurfaceVariant = Color(0xFFBED3C5),
    outline = Color(0xFF8AA597),
    outlineVariant = Color(0xFF3C5146)
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
