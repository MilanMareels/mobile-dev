package edu.ap.opdracht.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.Comment
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val location: Location? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val locationRepository = LocationRepository()
    private val locationId: String? = savedStateHandle.get("locationId")

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val comments: StateFlow<List<Comment>> = if (locationId != null) {
        locationRepository.getCommentsForLocation(locationId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    } else {
        MutableStateFlow(emptyList())
    }

    init {
        if (locationId.isNullOrBlank()) {
            _uiState.value = DetailUiState(isLoading = false, error = "Locatie ID niet gevonden.")
        } else {
            loadLocationDetails()
        }
    }

    private fun loadLocationDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            locationRepository.getLocationById(locationId!!).fold(
                onSuccess = { location ->
                    _uiState.value = DetailUiState(location = location, isLoading = false)
                },
                onFailure = { exception ->
                    _uiState.value = DetailUiState(error = exception.message, isLoading = false)
                }
            )
        }
    }

    fun submitRating(rating: Double, comment: String) {
        if (locationId == null) return
        viewModelScope.launch {
            // Verstuur naar Firebase
            locationRepository.addReview(locationId, rating, comment)
            // Herlaad de details om het nieuwe gemiddelde direct te zien
            loadLocationDetails()
        }
    }
}