package edu.ap.opdracht.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MyLocationsViewModel(
    private val locationRepository: LocationRepository = LocationRepository()
) : ViewModel() {
    val myLocations: StateFlow<List<Location>> =
        locationRepository.getMyLocations(category = null)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
}