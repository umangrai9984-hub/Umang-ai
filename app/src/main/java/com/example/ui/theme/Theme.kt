package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val UmangDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    secondary = NeonPurple,
    tertiary = NeonPink,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBackground,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextMuted
)

@Composable
fun UmangAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UmangDarkColorScheme,
        typography = Typography,
        content = content
    )
}
