package edu.ap.opdracht

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.ap.opdracht.ui.navigation.AppScreen
import edu.ap.opdracht.ui.theme.MobieldevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobieldevTheme {
                // Laad simpelweg het AppScreen dat alle navigatie en UI beheert
                AppScreen()
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