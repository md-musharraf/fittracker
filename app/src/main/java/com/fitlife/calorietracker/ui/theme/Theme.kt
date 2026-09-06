package com.fitlife.calorietracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceBorder,
    onPrimaryContainer = PrimaryOrangeLight,
    secondary = AccentGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceBorder,
    onSecondaryContainer = AccentGreenLight,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrangeDark,
    onPrimary = LightSurface,
    primaryContainer = PrimaryOrangeLight.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryOrangeDark,
    secondary = AccentGreenDark,
    onSecondary = LightSurface,
    secondaryContainer = AccentGreenLight.copy(alpha = 0.2f),
    onSecondaryContainer = AccentGreenDark,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightSurfaceBorder
)

@Composable
fun CalorieTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
