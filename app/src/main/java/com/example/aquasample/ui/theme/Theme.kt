package com.example.aquasample.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAqua,
    onPrimary = OnPrimaryAqua,
    primaryContainer = PrimaryContainerAqua,
    onPrimaryContainer = OnPrimaryContainerAqua,
    secondary = SecondaryMint,
    onSecondary = OnSecondaryMint,
    secondaryContainer = SecondaryContainerMint,
    onSecondaryContainer = OnSecondaryContainerMint,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorRed,
    onError = OnErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAqua,
    onPrimary = OnPrimaryAqua,
    primaryContainer = PrimaryContainerAqua,
    onPrimaryContainer = OnPrimaryContainerAqua,
    secondary = SecondaryMint,
    onSecondary = OnSecondaryMint,
    secondaryContainer = SecondaryContainerMint,
    onSecondaryContainer = OnSecondaryContainerMint,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = ErrorRed,
    onError = OnErrorRed
)

@Composable
fun AquaSampleTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val managedDarkTheme = ThemeManager.isDarkMode.collectAsState().value
    val useDarkTheme = darkTheme ?: managedDarkTheme

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
