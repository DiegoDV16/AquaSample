package com.example.aquasample.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.unit.dp

/** Escala de espaciado (múltiplos de 4 dp). */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
}

/** Medidas compartidas por pantallas y componentes. */
object Dimens {
    val screenPadding = 20.dp
    val buttonHeight = 52.dp
    val minTouchTarget = 48.dp
    val avatarSmall = 36.dp
    val avatarMedium = 48.dp
    val avatarLarge = 84.dp
    val iconBadge = 44.dp
    val photoTileHeight = 148.dp
    val kpiHeight = 112.dp
}

/** Duraciones y curvas de animación. */
object Motion {
    const val Instant = 80
    const val Short = 150
    const val Medium = 250
    const val Long = 400
    const val Emphasized = 500

    val Standard: Easing = FastOutSlowInEasing
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Decelerate: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
}
