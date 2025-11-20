package edu.ap.opdracht.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.ap.opdracht.ui.auth.AuthViewModel
import edu.ap.opdracht.ui.auth.LoginScreen
import edu.ap.opdracht.ui.auth.RegisterScreen
import edu.ap.opdracht.ui.profile.ProfileScreen
import androidx.compose.material.icons.filled.Add
import androidx.navigation.NavType
import androidx.navigation.navArgument
import edu.ap.opdracht.ui.detail.DetailScreen
import edu.ap.opdracht.ui.home.HomeScreen
import edu.ap.opdracht.ui.location.AddLocationScreen
import edu.ap.opdracht.ui.location.MyLocationsScreen

@Composable
fun AuthNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onGoToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onGoToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Add : Screen("add", "Toevoegen", Icons.Filled.Add)
    data object Profile : Screen("profile", "Profiel", Icons.Filled.Person)
}

@Composable
fun AppScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navItems = listOf(Screen.Home, Screen.Add, Screen.Profile)
    Scaffold(
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen (
                    onLocationClick = { locationId ->
                        navController.navigate("detail/$locationId")
                    },
                    onAddLocationClick = {
                        navController.navigate(Screen.Add.route)
                    }
                )

            }
            composable(
                route = "detail/{locationId}",
                arguments = listOf(navArgument("locationId") {
                    type = NavType.StringType
                })
            ) {
                DetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Add.route) {
                AddLocationScreen(
                    onLocationAdded = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                    onMyLocationsClick = {
                        navController.navigate("my_locations")
                    }
                )
            }
            composable("my_locations") {
                MyLocationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLocationClick = { locationId ->
                        navController.navigate("detail/$locationId")
                    }
                )
            }
        }
    }
}