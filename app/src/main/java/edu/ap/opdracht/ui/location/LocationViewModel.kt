package edu.ap.opdracht.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddLocationState {
    data object Idle : AddLocationState()
    data object Loading : AddLocationState()
    data object Success : AddLocationState()
    data class Error(val message: String) : AddLocationState()
}

class LocationViewModel : ViewModel() {

    private val repository = LocationRepository()

    private val _state = MutableStateFlow<AddLocationState>(AddLocationState.Idle)
    val state: StateFlow<AddLocationState> = _state

    fun saveLocation(name: String, category: String, geoPoint: GeoPoint?) {
        if (name.isBlank() || category.isBlank()) {
            _state.value = AddLocationState.Error("Naam en categorie zijn verplicht.")
            return
        }

        if (geoPoint == null) {
            _state.value = AddLocationState.Error("Locatie (GPS) is niet gevonden.")
            return
        }

        viewModelScope.launch {
            _state.value = AddLocationState.Loading

            val location = Location(
                name = name,
                category = category,
                location = geoPoint
            )

            val result = repository.addLocation(location)
            result.onSuccess {
                _state.value = AddLocationState.Success
            }
            result.onFailure {
                _state.value = AddLocationState.Error(it.message ?: "Opslaan mislukt")
            }
        }
    }

    fun resetState() {
        _state.value = AddLocationState.Idle
    }
}