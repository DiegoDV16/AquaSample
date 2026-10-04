package com.example.aquasample.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
    primaryContainer = OnPrimaryContainerAqua,
    onPrimaryContainer = PrimaryContainerAqua,
    secondary = SecondaryMint,
    onSecondary = OnSecondaryMint,
    background = OnBackgroundLight,
    onBackground = BackgroundLight,
    surface = OnSurfaceLight,
    onSurface = SurfaceLight,
    surfaceVariant = OnSurfaceVariantLight,
    onSurfaceVariant = SurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorRed,
    onError = OnErrorRed
)

@Composable
fun AquaSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
