package com.example.aquasample.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.aquasample.data.session.UserRole
import com.example.aquasample.data.session.UserSession
import com.example.aquasample.ui.admin.AdminUsuariosScreen
import com.example.aquasample.ui.history.HistorialMuestrasScreen
import com.example.aquasample.ui.home.HomeScreen
import com.example.aquasample.ui.sample.NuevaMuestraScreen
import com.example.aquasample.ui.sample.PerfilScreen
import com.example.aquasample.ui.theme.AquaSampleTheme

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userSession: UserSession = UserSession("1", "operador", "Juan Pérez", "operador@aquasample.cl", UserRole.OPERADOR),
    onLogout: () -> Unit = {}
) {
    val allowedItems = remember(userSession.role) {
        NavItem.itemsFor(userSession.role)
    }

    var selectedItem by remember(allowedItems) {
        mutableStateOf<NavItem>(allowedItems.firstOrNull() ?: NavItem.Home)
    }
    var showProfile by remember { mutableStateOf(false) }

    if (showProfile && userSession.role == UserRole.OPERADOR) {
        PerfilScreen(
            modifier = modifier,
            onBack = { showProfile = false }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                allowedItems.forEach { item ->
                    val isSelected = selectedItem == item
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItem = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (selectedItem) {
            is NavItem.Home -> HomeScreen(
                modifier = screenModifier,
                userSession = userSession,
                onNavigateToNewSample = { selectedItem = NavItem.NewSample },
                onLogout = onLogout,
                onNavigateToProfile = { showProfile = true }
            )
            is NavItem.NewSample -> NuevaMuestraScreen(
                modifier = screenModifier,
                onSavedComplete = { selectedItem = NavItem.History },
                onLogout = onLogout
            )
            is NavItem.History -> HistorialMuestrasScreen(
                modifier = screenModifier,
                canCreateSample = userSession.role == UserRole.OPERADOR,
                onNavigateToNewSample = { selectedItem = NavItem.NewSample },
                onLogout = onLogout
            )
            is NavItem.Admin -> AdminUsuariosScreen(
                modifier = screenModifier,
                currentUserRole = userSession.role,
                onLogout = onLogout
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AquaSampleTheme {
        MainScreen()
    }
}
