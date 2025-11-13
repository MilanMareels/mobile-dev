package edu.ap.opdracht.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val locationRepository: LocationRepository = LocationRepository()
) : ViewModel() {

    // 1. State om de geselecteerde categorie bij te houden (start met "Alles")
    private val _selectedCategory = MutableStateFlow("Alles")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 2. Functie die de UI kan aanroepen om de categorie te veranderen
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    val locations: StateFlow<List<Location>> = _selectedCategory.flatMapLatest { category ->
        locationRepository.getMyLocations(category)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )
}