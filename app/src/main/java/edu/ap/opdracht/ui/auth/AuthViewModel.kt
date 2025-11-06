package edu.ap.opdracht.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed class voor de UI state (Idle, Loading, Error, Success)
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- State voor AC 4 (Session Persistence) ---
    // Deze flow houdt de HUIDIGE ingelogde gebruiker bij.
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // --- State voor AC 1, 2, 3 (Login UI) ---
    // Deze flow houdt de *actie* (login/registratie) bij.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Luister naar veranderingen in de Firebase auth state (AC 4)
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    // --- Jouw functie voor Ticket 3 ---
    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("E-mail en wachtwoord mogen niet leeg zijn.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading // Start het laden

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Succes! (AC 2)
                    // _currentUser wordt automatisch geüpdatet door de listener.
                    _authState.value = AuthState.Success
                }
                .addOnFailureListener { exception ->
                    // Foutmeldingen (AC 3)
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidUserException -> "Er bestaat geen account met dit e-mailadres."
                        is FirebaseAuthInvalidCredentialsException -> "Wachtwoord of e-mail ongeldig."
                        else -> "Inloggen mislukt: ${exception.message}"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
        }
    }

    // Functie voor je teamgenoot (Ticket 2)
    // fun registerUser(email: String, password: String) { ... }

    // Functie om de state te resetten (bv. na navigatie)
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}