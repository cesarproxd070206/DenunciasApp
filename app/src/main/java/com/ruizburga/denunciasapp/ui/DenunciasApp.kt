package com.ruizburga.denunciasapp.ui

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ruizburga.denunciasapp.ui.components.AppIcon
import com.ruizburga.denunciasapp.ui.components.AppIconName
import com.ruizburga.denunciasapp.ui.components.motionDurationMillis
import com.ruizburga.denunciasapp.ui.screens.*
import com.ruizburga.denunciasapp.ui.theme.*
import com.ruizburga.denunciasapp.ui.viewmodel.DenunciasViewModel

private enum class Destination(val route: String, val label: String, val icon: AppIconName) {
    Home("inicio", "Inicio", AppIconName.Home),
    List("lista", "Denuncias", AppIconName.List),
    Create("registrar", "Registrar", AppIconName.Add),
    Detail("detalle", "Detalle", AppIconName.Document),
    Search("buscar", "Buscar DNI", AppIconName.Search)
}

private const val DetailWithIdRoute = "detalle/{id}"

@Composable
fun DenunciasApp() {
    val denunciasViewModel: DenunciasViewModel = viewModel()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val selected = when (currentRoute) {
        Destination.Detail.route, DetailWithIdRoute -> Destination.Detail
        else -> Destination.entries.firstOrNull { it.route == currentRoute } ?: Destination.Home
    }

    AppScaffold(
        selectedRoute = selected.route,
        onNavigate = { route ->
            val returningFromDetail = route == Destination.List.route &&
                (currentRoute == Destination.Detail.route || currentRoute == DetailWithIdRoute)
            if (returningFromDetail && navController.popBackStack(Destination.List.route, inclusive = false)) {
                // Reuse the existing list back-stack entry and its ViewModel data/pagination state.
            } else {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            enterTransition = {
                val duration = motionDurationMillis(220)
                fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { it / 18 }
            },
            exitTransition = {
                val duration = motionDurationMillis(170)
                fadeOut(tween(duration)) + slideOutHorizontally(tween(duration)) { -it / 24 }
            },
            popEnterTransition = {
                val duration = motionDurationMillis(220)
                fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { -it / 24 }
            },
            popExitTransition = {
                val duration = motionDurationMillis(170)
                fadeOut(tween(duration)) + slideOutHorizontally(tween(duration)) { it / 18 }
            }
        ) {
            composable(Destination.Home.route) {
                DashboardScreen(
                    viewModel = denunciasViewModel,
                    onOpenList = { navController.navigate(Destination.List.route) },
                    onRegister = { navController.navigate(Destination.Create.route) },
                    onSearch = { navController.navigate(Destination.Search.route) }
                )
            }
            composable(Destination.List.route) {
                ListaScreen(
                    viewModel = denunciasViewModel,
                    onOpenDetail = { id ->
                        navController.navigate("${Destination.Detail.route}/${Uri.encode(id)}")
                    },
                    onOpenHome = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Destination.Create.route) { RegistrarScreen(denunciasViewModel) }
            composable(Destination.Detail.route) { DetalleScreen(denunciasViewModel, selectedId = null) }
            composable(
                route = DetailWithIdRoute,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                DetalleScreen(
                    denunciasViewModel,
                    selectedId = backStackEntry.arguments?.getString("id")
                )
            }
            composable(Destination.Search.route) { BuscarScreen(denunciasViewModel) }
        }
    }
}

@Composable
private fun AppScaffold(selectedRoute: String, onNavigate: (String) -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        containerColor = Canvas,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceBase,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                Destination.entries.forEach { destination ->
                    key(destination.route) {
                        val iconTint by animateColorAsState(
                            targetValue = if (selectedRoute == destination.route) Teal else Muted,
                            animationSpec = tween(motionDurationMillis(180)),
                            label = "navigation-icon-tint"
                        )
                        NavigationBarItem(
                            selected = selectedRoute == destination.route,
                            onClick = { onNavigate(destination.route) },
                            icon = { AppIcon(destination.icon, Modifier.size(AppDimens.iconMedium), iconTint) },
                            label = { Text(destination.label, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Teal,
                                selectedTextColor = Teal,
                                indicatorColor = TealLight,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        }
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            content()
            Box(
                Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border.copy(alpha = .7f))
            )
        }
    }
}
