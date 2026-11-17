
package com.trinaldi.shoppinglist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF176B70),
    onPrimary = Color(0xFFFFFFFF),

    primaryContainer = Color(0xFFD0E8E7),
    onPrimaryContainer = Color(0xFF103F42),

    secondary = Color(0xFF4F806B),
    onSecondary = Color(0xFFFFFFFF),

    secondaryContainer = Color(0xFFD9EADF),
    onSecondaryContainer = Color(0xFF244C3A),

    tertiary = Color(0xFF536F91),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF263746),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF263746),

    surfaceVariant = Color(0xFFE5EDF0),
    onSurfaceVariant = Color(0xFF526575),

    error = Color(0xFFBA3038),
    onError = Color(0xFFFFFFFF),

    outline = Color(0xFF82949D),
    outlineVariant = Color(0xFFC5D1D7)
)

// Dark theme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF65B8B0),
    onPrimary = Color(0xFF14272B),

    primaryContainer = Color(0xFF24565A),
    onPrimaryContainer = Color(0xFFD0E8E7),

    secondary = Color(0xFF91BDA5),
    onSecondary = Color(0xFF1B3028),

    secondaryContainer = Color(0xFF345747),
    onSecondaryContainer = Color(0xFFD9EADF),

    tertiary = Color(0xFF9DB8D8),
    onTertiary = Color(0xFF1D2B3B),

    background = Color(0xFF18252D),
    onBackground = Color(0xFFE7EEF2),

    surface = Color(0xFF24343E),
    onSurface = Color(0xFFE7EEF2),

    surfaceVariant = Color(0xFF344852),
    onSurfaceVariant = Color(0xFFB1C1CA),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),

    outline = Color(0xFF8299A4),
    outlineVariant = Color(0xFF50636D)
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
