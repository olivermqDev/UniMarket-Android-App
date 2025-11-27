package com.atom.unimarket.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import com.atom.unimarket.ui.theme.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeonDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = TextColorLight,
    secondary = NeonPurple,
    tertiary = NeonPurple,
    background = DarkBackground,
    onBackground = TextColorLight,
    surface = DarkBackground,
    onSurface = TextColorLight,
    surfaceVariant = DarkBackground, // Adding surfaceVariant for consistency
    onSurfaceVariant = TextColorLight // Adding onSurfaceVariant for consistency
)

@Composable
fun UnimarketTheme(
    content: @Composable () -> Unit
) {
    // This theme is exclusively Dark Mode Neon/Vaporwave
    val colorScheme = NeonDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Dark status bar content
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
