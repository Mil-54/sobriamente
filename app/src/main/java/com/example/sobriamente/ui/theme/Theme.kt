package com.example.sobriamente.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Usamos un tema oscuro con colores calmantes (Soft Blues, Muted Greens, Lavanda)
private val CalmingDarkColorScheme = darkColorScheme(
    primary = CalmPrimary,
    secondary = CalmSecondary,
    background = CalmBackground,
    surface = CalmSurface,
    surfaceVariant = CalmSurface,
    onPrimary = CalmBackground,
    onSecondary = CalmBackground,
    onBackground = CalmTextWhite,
    onSurface = CalmTextWhite,
    onSurfaceVariant = CalmTextWhite,
    error = CalmError
)

@Composable
fun SobriamenteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Lo ponemos en false para usar NUESTROS colores, no los del sistema
    content: @Composable () -> Unit
) {
    val colorScheme = CalmingDarkColorScheme // Usamos siempre el oscuro calmante

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}