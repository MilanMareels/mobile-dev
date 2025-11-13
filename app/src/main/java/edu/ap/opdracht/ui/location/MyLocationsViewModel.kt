package edu.ap.opdracht.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class MyLocationsViewModel(
    private val locationRepository: LocationRepository = LocationRepository()
) : ViewModel() {
    private val _selectedCategory = MutableStateFlow("Alles")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
    val myLocations: StateFlow<List<Location>> = _selectedCategory.flatMapLatest { category ->
        locationRepository.getMyLocations(category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )
}