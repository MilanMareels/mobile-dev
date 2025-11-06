package edu.ap.opdracht.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
// import androidx.compose.material3.TextButton // VERWIJDERD
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    // We halen de viewModel hier direct op
    viewModel: RegisterViewModel = viewModel(),
    // AANGEPAST: onLoginClicked is hier verwijderd
    // Actie die wordt uitgevoerd na succesvolle registratie
    onRegisterSuccess: () -> Unit
) {
    // Lokale states voor de invoervelden
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    // Observeer de state van de ViewModel
    val state by viewModel.registrationState.collectAsStateWithLifecycle()

    // We gebruiken een Column om alles onder elkaar te zetten
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Maak een account", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        // Veld voor Display Naam
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Naam") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        // Veld voor E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mailadres") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        // Veld voor Wachtwoord
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wachtwoord") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        // Reageer op de state (Loading, Error, Success)
        when (val currentState = state) {
            is RegistrationState.Idle -> {
                Button(
                    onClick = {
                        viewModel.onRegisterClicked(email, password, displayName)
                    }
                ) {
                    Text("Registreren")
                }
            }
            is RegistrationState.Loading -> {
                CircularProgressIndicator()
            }
            is RegistrationState.Success -> {
                // Registratie is gelukt! Roep de success-actie aan.
                onRegisterSuccess()
            }
            is RegistrationState.Error -> {
                // Toon foutmelding en de knop opnieuw
                Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.onRegisterClicked(email, password, displayName)
                    }
                ) {
                    Text("Probeer opnieuw")
                }
            }
        }

        // AANGEPAST: De TextButton om naar login te gaan is hier verwijderd
    }
}