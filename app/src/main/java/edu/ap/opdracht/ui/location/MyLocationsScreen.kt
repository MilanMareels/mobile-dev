package edu.ap.opdracht.ui.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.opdracht.ui.home.LocationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLocationsScreen(
    myLocationsViewModel: MyLocationsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLocationClick: (locationId: String) -> Unit
) {
    val locations by myLocationsViewModel.myLocations.collectAsState()

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
            contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(locations) { location ->
                LocationItem(
                    location = location,
                    onClick = {
                        if (!location.id.isNullOrBlank()) {
                            onLocationClick(location.id)
                        }
                    }
                )
            }
        }
    }
}