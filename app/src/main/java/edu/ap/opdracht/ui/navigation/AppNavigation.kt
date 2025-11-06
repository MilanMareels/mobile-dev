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
import edu.ap.opdracht.Greeting
import edu.ap.opdracht.ui.auth.AuthViewModel
import edu.ap.opdracht.ui.auth.LoginScreen
import edu.ap.opdracht.ui.auth.RegisterScreen
import edu.ap.opdracht.ui.profile.ProfileScreen
import edu.ap.opdracht.ui.settings.SettingsScreen

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
    data object Settings : Screen("settings", "Instellingen", Icons.Filled.Settings)
    data object Profile : Screen("profile", "Profiel", Icons.Filled.Person)
}

@Composable
fun AppScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navItems = listOf(Screen.Home, Screen.Settings, Screen.Profile)

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
                Greeting(
                    name = "Ingelogde Gebruiker",
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel
                )
            }
        }
    }
}