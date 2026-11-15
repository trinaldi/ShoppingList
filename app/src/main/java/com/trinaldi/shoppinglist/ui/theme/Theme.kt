package com.trinaldi.shoppinglist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Gruvbox Dark
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFABD2F),
    onPrimary = Color(0xFF282828),

    primaryContainer = Color(0xFF50421F),
    onPrimaryContainer = Color(0xFFFABD2F),

    secondary = Color(0xFF8EC07C),
    onSecondary = Color(0xFF282828),

    secondaryContainer = Color(0xFF3B4934),
    onSecondaryContainer = Color(0xFFB8BB26),

    tertiary = Color(0xFF83A598),
    onTertiary = Color(0xFF282828),

    background = Color(0xFF282828),
    onBackground = Color(0xFFEBDBB2),

    surface = Color(0xFF32302F),
    onSurface = Color(0xFFEBDBB2),

    surfaceVariant = Color(0xFF504945),
    onSurfaceVariant = Color(0xFFD5C4A1),

    error = Color(0xFFFB4934),
    onError = Color(0xFF282828),

    outline = Color(0xFF928374),
    outlineVariant = Color(0xFF665C54)
)

// Gruvbox Light
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB57614),
    onPrimary = Color(0xFFFFF4D5),

    primaryContainer = Color(0xFFF2E5BC),
    onPrimaryContainer = Color(0xFF5B3F00),

    secondary = Color(0xFF79740E),
    onSecondary = Color(0xFFFFF4D5),

    secondaryContainer = Color(0xFFE5E5A5),
    onSecondaryContainer = Color(0xFF3C3836),

    tertiary = Color(0xFF427B58),
    onTertiary = Color(0xFFFFF4D5),

    background = Color(0xFFFBF1C7),
    onBackground = Color(0xFF3C3836),

    surface = Color(0xFFF2E5BC),
    onSurface = Color(0xFF3C3836),

    surfaceVariant = Color(0xFFD5C4A1),
    onSurfaceVariant = Color(0xFF504945),

    error = Color(0xFFCC241D),
    onError = Color(0xFFFFF4D5),

    outline = Color(0xFF928374),
    outlineVariant = Color(0xFFBDAE93)
)

@Composable
fun ShoppingListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
