package edu.ap.opdracht

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.opdracht.ui.auth.AuthViewModel
import edu.ap.opdracht.ui.navigation.AppScreen
import edu.ap.opdracht.ui.navigation.AuthNavigation
import edu.ap.opdracht.ui.theme.MobieldevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobieldevTheme {
                val authViewModel: AuthViewModel = viewModel()

                val currentUser by authViewModel.currentUser.collectAsState()

                if (currentUser == null) {
                    AuthNavigation(authViewModel)
                } else {
                    AppScreen(authViewModel)
                }
            }
        }
    }
}

// De Greeting functie blijft hier of verhuist naar HomeScreen.kt
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobieldevTheme {
        // Om de preview van Greeting te laten werken zonder NavController
        Greeting("Android")
    }
}