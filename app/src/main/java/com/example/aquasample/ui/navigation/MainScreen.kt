package com.example.aquasample.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aquasample.data.session.UserRole
import com.example.aquasample.data.session.UserSession
import com.example.aquasample.ui.admin.AdminCatalogosScreen
import com.example.aquasample.ui.admin.AdminUsuariosScreen
import com.example.aquasample.ui.detail.SampleDetailScreen
import com.example.aquasample.ui.history.HistorialMuestrasScreen
import com.example.aquasample.ui.home.HomeScreen
import com.example.aquasample.ui.profile.ProfileScreen
import com.example.aquasample.ui.sample.NuevaMuestraScreen
import com.example.aquasample.ui.theme.Motion

/**
 * Grafo de navegación principal.
 *
 * La barra inferior solo se muestra en los destinos raíz; los flujos profundos
 * (nueva muestra, detalle) la ocultan para ganar espacio vertical.
 */
@Composable
fun MainScreen(
    userSession: UserSession,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomItems = remember(userSession.role) { bottomBarDestinations(userSession.role) }
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(animationSpec = tween(Motion.Medium)) { height -> height } +
                    fadeIn(animationSpec = tween(Motion.Short)),
                exit = slideOutVertically(animationSpec = tween(Motion.Short)) { height -> height } +
                    fadeOut(animationSpec = tween(Motion.Instant))
            ) {
                AquaBottomBar(
                    items = bottomItems,
                    currentRoute = currentRoute,
                    onSelect = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = {
                fadeIn(animationSpec = tween(Motion.Medium)) +
                    slideInHorizontally(animationSpec = tween(Motion.Medium)) { width -> width / 16 }
            },
            exitTransition = { fadeOut(animationSpec = tween(Motion.Short)) },
            popEnterTransition = {
                fadeIn(animationSpec = tween(Motion.Medium)) +
                    slideInHorizontally(animationSpec = tween(Motion.Medium)) { width -> -width / 16 }
            },
            popExitTransition = { fadeOut(animationSpec = tween(Motion.Short)) }
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    userSession = userSession,
                    onNewSample = { navController.navigate(AppDestination.NewSample.route) },
                    onSeeAllSamples = { navController.navigate(AppDestination.Samples.route) },
                    onOpenSample = { sampleId ->
                        navController.navigate(AppDestination.SampleDetail.createRoute(sampleId))
                    },
                    onLogout = onLogout
                )
            }

            composable(AppDestination.Samples.route) {
                HistorialMuestrasScreen(
                    canCreateSample = userSession.role == UserRole.OPERADOR,
                    onNavigateToNewSample = { navController.navigate(AppDestination.NewSample.route) },
                    onOpenSample = { sampleId ->
                        navController.navigate(AppDestination.SampleDetail.createRoute(sampleId))
                    },
                    onLogout = onLogout
                )
            }

            composable(
                route = AppDestination.SampleDetail.route,
                arguments = listOf(
                    navArgument(AppDestination.SampleDetail.ARG_SAMPLE_ID) {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val sampleId = entry.arguments
                    ?.getString(AppDestination.SampleDetail.ARG_SAMPLE_ID)
                    .orEmpty()

                SampleDetailScreen(
                    sampleId = sampleId,
                    canValidate = userSession.role != UserRole.OPERADOR,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.NewSample.route) {
                NuevaMuestraScreen(
                    onSampleSaved = { navController.popBackStack() },
                    onClose = { navController.popBackStack() }
                )
            }

            composable(AppDestination.Team.route) {
                AdminUsuariosScreen(
                    currentUserRole = userSession.role,
                    onLogout = onLogout,
                    onOpenCatalogs = {
                        navController.navigate(AppDestination.AdminCatalogs.route)
                    }
                )
            }

            composable(AppDestination.AdminCatalogs.route) {
                AdminCatalogosScreen(onBack = { navController.popBackStack() })
            }

            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun AquaBottomBar(
    items: List<AppDestination>,
    currentRoute: String?,
    onSelect: (AppDestination) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            items.forEach { destination ->
                val selected = currentRoute == destination.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { if (!selected) onSelect(destination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
