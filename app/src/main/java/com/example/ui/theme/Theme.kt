package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color(0xFF261900),
    primaryContainer = AmberContainerDark,
    onPrimaryContainer = OnAmberContainerDark,
    secondary = SteelBlue,
    onSecondary = Color(0xFF002F49),
    secondaryContainer = Color(0xFF084364),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = ProfitGreen,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = ProfitGreenContainer,
    onTertiaryContainer = OnProfitGreenContainer,
    error = ExpenseRed,
    errorContainer = ExpenseRedContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFFFFD1D8),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E2A3A)
)

val LightColorScheme = lightColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = SteelBlueDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = ExpenseRed,
    errorContainer = Color(0xFFFFE4E6),
    onError = Color.White,
    onErrorContainer = Color(0xFF9F1239),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun TBJayaAbadiTheme(
    darkTheme: Boolean = true, // Default Dark Mode as requested by user
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for backwards-compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TBJayaAbadiTheme(darkTheme = darkTheme, content = content)
}
