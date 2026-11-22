package com.example.aquasample.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.aquasample.data.session.UserRole

/**
 * Rutas de la aplicación.
 *
 * Cada destino raíz tiene su par de iconos (relleno para seleccionado, contorno
 * para no seleccionado) y una etiqueta corta para la barra inferior.
 */
sealed class AppDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : AppDestination(
        route = "home",
        label = "Inicio",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Samples : AppDestination(
        route = "samples",
        label = "Muestras",
        selectedIcon = Icons.Rounded.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object NewSample : AppDestination(
        route = "samples/new",
        label = "Nueva",
        selectedIcon = Icons.Rounded.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object Team : AppDestination(
        route = "team",
        label = "Personal",
        selectedIcon = Icons.Rounded.Groups,
        unselectedIcon = Icons.Outlined.Groups
    )

    data object Profile : AppDestination(
        route = "profile",
        label = "Perfil",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    /** Detalle de una muestra; no aparece en la barra inferior. */
    data object SampleDetail : AppDestination(
        route = "muestra/{sampleId}",
        label = "Detalle",
        selectedIcon = Icons.Rounded.Description,
        unselectedIcon = Icons.Outlined.Description
    ) {
        const val ARG_SAMPLE_ID = "sampleId"

        fun createRoute(sampleId: String): String = "muestra/${Uri.encode(sampleId)}"
    }
}

/**
 * Destinos visibles en la barra inferior según el rol. El operador registra,
 * el analista revisa y el administrador además gestiona al personal.
 */
fun bottomBarDestinations(role: UserRole): List<AppDestination> = when (role) {
    UserRole.OPERADOR -> listOf(AppDestination.Home, AppDestination.Samples, AppDestination.Profile)
    UserRole.ANALISTA -> listOf(AppDestination.Home, AppDestination.Samples, AppDestination.Profile)
    UserRole.ADMINISTRADOR -> listOf(
        AppDestination.Home,
        AppDestination.Samples,
        AppDestination.Team,
        AppDestination.Profile
    )
}
