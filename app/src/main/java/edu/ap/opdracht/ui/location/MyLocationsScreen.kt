package edu.ap.opdracht.ui.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.opdracht.ui.home.LocationItem

import edu.ap.opdracht.ui.home.FilterChipRow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLocationsScreen(
    myLocationsViewModel: MyLocationsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLocationClick: (locationId: String) -> Unit
) {
    val locations by myLocationsViewModel.myLocations.collectAsStateWithLifecycle()
    val selectedCategory by myLocationsViewModel.selectedCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mijn Locaties") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Terug"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                FilterChipRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        myLocationsViewModel.selectCategory(category)
                    }
                )
            }

            items(locations) { location ->
                LocationItem(
                    location = location,
                    onClick = {
                        if (!location.id.isNullOrBlank()) {
                            onLocationClick(location.id)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}