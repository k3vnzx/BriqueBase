package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BriqueiLime,
    onPrimary = Color(0xFF022C11),
    primaryContainer = Color(0xFF052E16),
    onPrimaryContainer = BriqueiLimeLight,

    secondary = BriqueiCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = Color(0xFF67E8F9),

    tertiary = BriqueiPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B0764),
    onTertiaryContainer = Color(0xFFE9D5FF),

    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = BriqueiRed,
    onError = Color.White,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark graphite theme
    dynamicColor: Boolean = false, // Keep consistent branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
