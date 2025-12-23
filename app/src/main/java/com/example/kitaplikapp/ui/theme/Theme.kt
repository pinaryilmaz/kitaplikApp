package com.example.kitaplikapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = OnTertiary,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline,

    error = Error,
    onError = OnError
)

private val DarkColorScheme = darkColorScheme(
    // Dark modda da moru koruyup yüzeyleri koyulaştırıyoruz
    primary = Color(0xFFBDB4FF),
    onPrimary = Color(0xFF160B2E),
    primaryContainer = Color(0xFF2B1E70),
    onPrimaryContainer = Color(0xFFE6E3FF),

    secondary = Color(0xFFC9C3FF),
    onSecondary = Color(0xFF160B2E),
    secondaryContainer = Color(0xFF2A2560),
    onSecondaryContainer = Color(0xFFEDEBFF),

    tertiary = Color(0xFF7BE7CF),
    onTertiary = Color(0xFF062019),

    background = Color(0xFF0F0F14),
    onBackground = Color(0xFFEAEAF0),

    surface = Color(0xFF14141B),
    onSurface = Color(0xFFEAEAF0),

    surfaceVariant = Color(0xFF1D1D27),
    onSurfaceVariant = Color(0xFFB9B9C6),

    outline = Color(0xFF3A3A4B),

    error = Color(0xFFFF6B6F),
    onError = Color(0xFF1A0B0C)
)

@Composable
fun KitaplikAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // Projede Typography.kt varsa aynen kalsın
        content = content
    )
}
