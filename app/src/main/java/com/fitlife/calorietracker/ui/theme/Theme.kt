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
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = PrimaryOrangeLight,
    secondary = AccentGreen,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = PrimaryOrange.copy(alpha = 0.18f),
    onSecondaryContainer = PrimaryOrange,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrangeDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = PrimaryOrangeDark,
    secondary = AccentGreenDark,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = LightPrimaryContainer,
    onSecondaryContainer = PrimaryOrangeDark,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightCardBorder
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
            window.navigationBarColor = colorScheme.surface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
