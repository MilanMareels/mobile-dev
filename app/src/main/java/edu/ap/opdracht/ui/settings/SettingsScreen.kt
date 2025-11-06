package edu.ap.opdracht.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import edu.ap.opdracht.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Instellingen Scherm")
        Text(text = "Hier komen alle opties voor de gebruiker.")
        Button(
            onClick = {
                authViewModel.logout()
            }
        ) {
            Text("Uitloggen")
        }
    }
}
