package edu.ap.opdracht.ui.auth // Of 'package com.jouwprojectnaam.ui.auth'

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import edu.ap.opdracht.data.model.User // Importeer het User model
import edu.ap.opdracht.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// AuthState blijft hetzelfde
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user

            if (user != null) {
                loadUserProfile(user.uid)
            } else {
                _userProfile.value = null
            }
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val result = authRepository.getUserProfile(uid)
            result.onSuccess { user ->
                _userProfile.value = user
            }
            result.onFailure { exception ->
                _authState.value = AuthState.Error("Profiel laden mislukt: ${exception.message}")
                _userProfile.value = null
            }
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("E-mail en wachtwoord mogen niet leeg zijn.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _authState.value = AuthState.Success
                }
                .addOnFailureListener { exception ->
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidUserException -> "Geen account gevonden."
                        is FirebaseAuthInvalidCredentialsException -> "Wachtwoord of e-mail ongeldig."
                        else -> "Inloggen mislukt: ${exception.message}"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
        }
    }

    fun registerUser(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _authState.value = AuthState.Error("Alle velden zijn verplicht.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.registerUser(email, password, displayName)

            result.onSuccess {
                _authState.value = AuthState.Success
            }
            result.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Onbekende fout")
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}