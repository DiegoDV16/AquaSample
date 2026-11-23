package com.example.aquasample.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta de marca AquaSample · ALDEMAR SpA.
 *
 * Evolución de la identidad original (azul océano + verde menta) hacia una escala
 * más profunda y con contraste suficiente para uso en terreno y bajo luz solar.
 */
internal val Ocean950 = Color(0xFF04121A)
internal val Ocean900 = Color(0xFF06293A)
internal val Ocean850 = Color(0xFF073247)
internal val Ocean800 = Color(0xFF083D57)
internal val Ocean700 = Color(0xFF0A577A)
internal val Ocean600 = Color(0xFF0A7EA4)
internal val Ocean500 = Color(0xFF2A9CC4)
internal val Ocean200 = Color(0xFFBFE4F2)
internal val Ocean100 = Color(0xFFDDF1F8)
internal val Ocean50 = Color(0xFFF1F9FC)

internal val Kelp900 = Color(0xFF06301F)
internal val Kelp700 = Color(0xFF1F7A57)
internal val Kelp500 = Color(0xFF3E9C75)
internal val Kelp200 = Color(0xFFBCE8D4)
internal val Kelp100 = Color(0xFFD3F0E2)

internal val Sand800 = Color(0xFF553900)
internal val Sand700 = Color(0xFF7A5200)
internal val Sand500 = Color(0xFFB07A12)
internal val Sand200 = Color(0xFFFFDFA6)
internal val Sand100 = Color(0xFFFFEFCF)

internal val Slate950 = Color(0xFF07141B)
internal val Slate900 = Color(0xFF0B1B23)
internal val Slate800 = Color(0xFF12262F)
internal val Slate700 = Color(0xFF3C5562)
internal val Slate500 = Color(0xFF6E8794)
internal val Slate300 = Color(0xFFA9BEC9)
internal val Slate200 = Color(0xFFC9D9E2)
internal val Slate100 = Color(0xFFE6EFF4)
internal val Slate50 = Color(0xFFF6FAFC)
internal val PureWhite = Color(0xFFFFFFFF)

internal val Red700 = Color(0xFFB3261E)
internal val Red500 = Color(0xFFDC4C42)
internal val Red100 = Color(0xFFF9DEDC)
internal val Red900 = Color(0xFF410E0B)
internal val Red200 = Color(0xFFFFB4AB)

/**
 * Colores semánticos que Material 3 no define (éxito, advertencia, información).
 * Se exponen vía [LocalAquaStatusColors] para que los componentes los consuman
 * sin duplicar lógica de tema.
 */
@Immutable
data class AquaStatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color
)

internal val LightStatusColors = AquaStatusColors(
    success = Kelp700,
    onSuccess = PureWhite,
    successContainer = Kelp100,
    onSuccessContainer = Kelp900,
    warning = Sand700,
    onWarning = PureWhite,
    warningContainer = Sand100,
    onWarningContainer = Sand800,
    info = Ocean700,
    onInfo = PureWhite,
    infoContainer = Ocean100,
    onInfoContainer = Ocean900
)

internal val DarkStatusColors = AquaStatusColors(
    success = Color(0xFF7FD3AC),
    onSuccess = Color(0xFF00382A),
    successContainer = Color(0xFF0B4A34),
    onSuccessContainer = Color(0xFFC2F0DA),
    warning = Color(0xFFFFD48A),
    onWarning = Sand800,
    warningContainer = Color(0xFF5A3D00),
    onWarningContainer = Sand200,
    info = Color(0xFF8FD6F2),
    onInfo = Ocean900,
    infoContainer = Ocean800,
    onInfoContainer = Ocean100
)

val LocalAquaStatusColors = staticCompositionLocalOf { LightStatusColors }
