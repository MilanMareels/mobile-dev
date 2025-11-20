package edu.ap.opdracht.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.City
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val locationRepository: LocationRepository = LocationRepository()
) : ViewModel() {

    // --- Filters ---
    private val _selectedCategory = MutableStateFlow("Alles")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCityId = MutableStateFlow<String?>("Alles")
    val selectedCityId: StateFlow<String?> = _selectedCityId.asStateFlow()

    // --- View Mode (Lijst of Kaart) ---
    private val _isMapView = MutableStateFlow(false)
    val isMapView: StateFlow<Boolean> = _isMapView.asStateFlow()

    // --- Data: Steden (voor chips) ---
    val cities: StateFlow<List<City>> = locationRepository.getCities().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    // --- Data: Locaties (Gefilterd) ---
    // Dit luistert naar BEIDE filters.
    val locations: StateFlow<List<Location>> = combine(
        _selectedCategory,
        _selectedCityId
    ) { category, cityId ->
        Pair(category, cityId)
    }.flatMapLatest { (category, cityId) ->
        locationRepository.getAllLocations(category, cityId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    // --- Actions ---
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectCity(cityId: String) {
        _selectedCityId.value = cityId
    }

    fun toggleViewMode(showMap: Boolean) {
        _isMapView.value = showMap
    }
}