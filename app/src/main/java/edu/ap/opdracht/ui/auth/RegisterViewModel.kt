package edu.ap.opdracht.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.User
import edu.ap.opdracht.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Een simpele state om de UI te vertellen wat er gebeurt
sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val user: User) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class RegisterViewModel : ViewModel() {

    // Maak een instantie van de repository
    // (In een echt project zou je dit via Dependency Injection doen, bijv. Hilt)
    private val authRepository = AuthRepository()

    // _state is privaat en kan gewijzigd worden, state is publiek en read-only
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun onRegisterClicked(email: String, password: String, displayName: String) {
        // Valideer input (simpel voorbeeld)
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _registrationState.value = RegistrationState.Error("Alle velden zijn verplicht.")
            return
        }

        // Start de coroutine op de ViewModel scope
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading

            val result = authRepository.registerUser(email, password, displayName)

            result.onSuccess { user ->
                _registrationState.value = RegistrationState.Success(user)
            }
            result.onFailure { exception ->
                _registrationState.value = RegistrationState.Error(exception.message ?: "Onbekende fout")
            }
        }
    }
}