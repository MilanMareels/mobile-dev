package edu.ap.opdracht.ui.location

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import edu.ap.opdracht.data.model.Comment
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.model.Rating
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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

    fun saveLocation(
        name: String,
        category: String,
        geoPoint: GeoPoint?,
        imageUri: Uri?,
        ratingValue: Double,
        commentText: String
    ) {
        // Validatie
        if (name.isBlank() || category.isBlank()) {
            _state.value = AddLocationState.Error("Naam en categorie zijn verplicht.")
            return
        }
        if (geoPoint == null) {
            _state.value = AddLocationState.Error("Locatie (GPS) is niet gevonden.")
            return
        }
        if (imageUri == null) {
            _state.value = AddLocationState.Error("Kies een foto.")
            return
        }
        if (commentText.isBlank()) {
            _state.value = AddLocationState.Error("Voeg een korte beschrijving toe.")
            return
        }
        if (ratingValue == 0.0) {
            _state.value = AddLocationState.Error("Geef een rating (1-5 sterren).")
            return
        }

        val uid = repository.getCurrentUserId()
        val displayName = repository.getCurrentUserDisplayName()

        if (uid == null) {
            _state.value = AddLocationState.Error("Kon gebruiker niet verifiëren. Log opnieuw in.")
            return
        }

        viewModelScope.launch {
            _state.value = AddLocationState.Loading

            val photoResult = repository.uploadPhoto(imageUri)

            photoResult.onSuccess { downloadUrl ->

                val location = Location(
                    name = name,
                    category = category,
                    location = geoPoint,
                    photoUrl = downloadUrl,
                    addedByUid = uid
                )

                val rating = Rating(
                    value = ratingValue,
                    ratedByUid = uid
                )

                val comment = Comment(
                    text = commentText,
                    commentByUid = uid,
                    userDisplayName = displayName
                )

                // 3. Roep de nieuwe repository-functie aan
                val addResult = repository.addLocationWithDetails(location, rating, comment)

                addResult.onSuccess {
                    _state.value = AddLocationState.Success
                }
                addResult.onFailure {
                    _state.value = AddLocationState.Error(it.message ?: "Opslaan locatie mislukt")
                }

            }.onFailure {
                _state.value = AddLocationState.Error(it.message ?: "Foto upload mislukt")
            }
        }
    }

    fun resetState() {
        _state.value = AddLocationState.Idle
    }

    private val _selectedCategory = MutableStateFlow("Alles")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    val locations: StateFlow<List<Location>> = _selectedCategory.flatMapLatest { category ->
        repository.getAllLocations(category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )
}