package edu.ap.opdracht.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onGoToLogin: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }

    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val state by authViewModel.authState.collectAsStateWithLifecycle()
    val isLoading = state is AuthState.Loading

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welkom bij CityExplorer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Log in of maak een account aan om te beginnen",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTabRow(
                onGoToLogin = onGoToLogin
            )

            Spacer(modifier = Modifier.height(32.dp))

            LabeledTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Naam"
            )
            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                value = password,
                onValueChange = { password = it },
                label = "Wachtwoord",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Bevestig wachtwoord",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        localError = "Wachtwoorden komen niet overeen."
                    } else {
                        localError = null
                        authViewModel.registerUser(email, password, displayName)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4)
                )
            ) {
                Text("Account aanmaken", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (isLoading) {
                CircularProgressIndicator()
            }

            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            LaunchedEffect(state) {
                if (state is AuthState.Error) {
                    delay(3000)
                    authViewModel.resetAuthState()
                }
                if (state is AuthState.Success) {
                    authViewModel.resetAuthState()
                }
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun AuthTabRow(
    onGoToLogin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onGoToLogin),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Inloggen",
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Registreren",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}