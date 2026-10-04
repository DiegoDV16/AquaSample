package com.example.aquasample.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.aquasample.data.session.UserRole

sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : NavItem("home", "Inicio", Icons.Default.Home)
    data object NewSample : NavItem("new_sample", "Nueva Muestra", Icons.Default.AddCircle)
    data object History : NavItem("history", "Historial", Icons.Default.History)
    data object Admin : NavItem("admin", "Personal / Admin", Icons.Default.SupervisorAccount)

    companion object {
        fun itemsFor(role: UserRole): List<NavItem> {
            return when (role) {
                UserRole.OPERADOR -> listOf(Home, NewSample, History)
                UserRole.ANALISTA -> listOf(Home, History)
                UserRole.ADMINISTRADOR -> listOf(Home, Admin, History)
            }
        }
    }
}
