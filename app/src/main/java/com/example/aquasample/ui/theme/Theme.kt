package com.example.aquasample.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Ocean700,
    onPrimary = PureWhite,
    primaryContainer = Ocean100,
    onPrimaryContainer = Ocean900,
    inversePrimary = Ocean200,
    secondary = Kelp700,
    onSecondary = PureWhite,
    secondaryContainer = Kelp100,
    onSecondaryContainer = Kelp900,
    tertiary = Sand700,
    onTertiary = PureWhite,
    tertiaryContainer = Sand100,
    onTertiaryContainer = Sand800,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    surfaceTint = Ocean700,
    inverseSurface = Slate800,
    inverseOnSurface = Slate50,
    error = Red700,
    onError = PureWhite,
    errorContainer = Red100,
    onErrorContainer = Red900,
    outline = Slate500,
    outlineVariant = Slate200,
    scrim = Color(0xFF000000),
    surfaceBright = PureWhite,
    surfaceDim = Slate100,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = Slate50,
    surfaceContainer = Slate50,
    surfaceContainerHigh = Slate100,
    surfaceContainerHighest = Slate200
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6FC9EE),
    onPrimary = Ocean950,
    primaryContainer = Ocean800,
    onPrimaryContainer = Ocean100,
    inversePrimary = Ocean700,
    secondary = Color(0xFF7FD3AC),
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFF0B4A34),
    onSecondaryContainer = Kelp100,
    tertiary = Color(0xFFFFD48A),
    onTertiary = Sand800,
    tertiaryContainer = Color(0xFF5A3D00),
    onTertiaryContainer = Sand200,
    background = Slate950,
    onBackground = Color(0xFFE7F1F6),
    surface = Slate900,
    onSurface = Color(0xFFE7F1F6),
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    surfaceTint = Color(0xFF6FC9EE),
    inverseSurface = Color(0xFFE7F1F6),
    inverseOnSurface = Slate900,
    error = Red200,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF7C96A3),
    outlineVariant = Color(0xFF2E4652),
    scrim = Color(0xFF000000),
    surfaceBright = Slate800,
    surfaceDim = Slate950,
    surfaceContainerLowest = Color(0xFF040D12),
    surfaceContainerLow = Slate950,
    surfaceContainer = Slate900,
    surfaceContainerHigh = Slate800,
    surfaceContainerHighest = Color(0xFF1B303B)
)

/**
 * Tema raíz de AquaSample.
 *
 * Responde a [ThemeManager] (preferencia del usuario) y, si no hay preferencia,
 * al modo del sistema. Con edge-to-edge activo, además ajusta el contraste de
 * los iconos de la barra de estado.
 */
@Composable
fun AquaSampleTheme(
    darkTheme: Boolean = ThemeManager.isDarkTheme.collectAsState().value ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAquaStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AquaTypography,
            shapes = AquaShapes,
            content = content
        )
    }
}

/** Acceso a los colores semánticos (éxito, advertencia, información). */
val MaterialTheme.statusColors: AquaStatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAquaStatusColors.current
