package edu.ap.opdracht.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onGoToRegister: () -> Unit // Lambda om naar Registratie te navigeren
) {
    // Luister naar de state van de ViewModel
    val authState by authViewModel.authState.collectAsState()

    // Lokale state voor de invoervelden
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Titel ---
        Text(text = "Inloggen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- E-mail invoerveld ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mailadres") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Wachtwoord invoerveld ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wachtwoord") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // --- Login Knop ---
        Button(
            onClick = { authViewModel.loginUser(email, password) },
            modifier = Modifier.fillMaxWidth(),
            // Toon de knop als "disabled" tijdens het laden
            enabled = authState != AuthState.Loading
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Link naar Registratie ---
        TextButton(onClick = onGoToRegister) {
            Text("Nog geen account? Registreer hier.")
        }

        // --- Toon Laad-indicator of Foutmelding (AC 3) ---
        Spacer(modifier = Modifier.height(16.dp))
        when (val state = authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {} // Idle of Success
        }
    }
}