package edu.ap.opdracht.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class HomeViewModel(
    private val locationRepository: LocationRepository = LocationRepository()
) : ViewModel() {
    val locations: StateFlow<List<Location>> = locationRepository.getMyLocations()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )
}