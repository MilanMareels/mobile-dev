package edu.ap.opdracht.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle // Je icoon was correct
import androidx.compose.material.icons.filled.Home
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.ap.opdracht.Greeting
import edu.ap.opdracht.ui.auth.RegisterScreen // Importeren
import edu.ap.opdracht.ui.settings.SettingsScreen

// Definieer de schermen en hun routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Settings : Screen("settings", "Instellingen", Icons.Filled.Settings)
    data object Register : Screen("register", "Registreren", Icons.Filled.AddCircle)
    // Login is hier terecht verwijderd
}

@Composable
fun AppScreen() {
    val navController = rememberNavController()

    // Deze items zijn voor de BottomAppBar
    val navItems = listOf(Screen.Home, Screen.Settings)

    // Huidige route bepalen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Bepaal of de BottomAppBar getoond moet worden
    val shouldShowBottomBar = navItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            // Alleen tonen als we op Home of Settings zijn
            if (shouldShowBottomBar) {
                BottomAppBar {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            // AANGEPAST: Start op het Registratie-scherm
            startDestination = Screen.Register.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Scherm
            composable(Screen.Home.route) {
                Greeting(
                    name = "Ingelogde Gebruiker",
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Instellingen Scherm
            composable(Screen.Settings.route) {
                SettingsScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Registratie Scherm (NIEUW)
            composable(Screen.Register.route) {
                RegisterScreen(
                    // AANGEPAST: De 'onLoginClicked' parameter is verwijderd
                    onRegisterSuccess = {
                        // Na registratie, ga naar Home en wis de auth backstack
                        navController.navigate(Screen.Home.route) {
                            // Pop tot aan het begin van de graph, niet specifiek 'Register'
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}