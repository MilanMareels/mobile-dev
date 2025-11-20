package edu.ap.opdracht.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.ap.opdracht.data.model.City
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.ui.map.HomeMapView

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onLocationClick: (locationId: String) -> Unit,
    onAddLocationClick: () -> Unit = {}
) {
    // Haal alle states op van de HomeViewModel
    val locations by homeViewModel.locations.collectAsStateWithLifecycle()
    val selectedCategory by homeViewModel.selectedCategory.collectAsStateWithLifecycle()
    val cities by homeViewModel.cities.collectAsStateWithLifecycle()
    val selectedCityId by homeViewModel.selectedCityId.collectAsStateWithLifecycle()
    val isMapView by homeViewModel.isMapView.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header en Filters (Vast bovenaan)
            Header()

            CityChips(
                cities = cities,
                selectedCityId = selectedCityId,
                onCitySelected = { cityId ->
                    homeViewModel.selectCity(cityId)
                }
            )

            FilterChipRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    homeViewModel.selectCategory(category)
                }
            )

            PopularLocationsHeader(
                isMapView = isMapView,
                onToggle = { showMap -> homeViewModel.toggleViewMode(showMap) }
            )

            // Content (Kaart of Lijst)
            Box(modifier = Modifier.fillMaxSize()) {
                if (isMapView) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Laat de kaart de rest van de ruimte vullen
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            HomeMapView(
                                locations = locations,
                                onLocationClick = onLocationClick
                            )
                        }

                        Text(
                            text = "${locations.size} locaties gevonden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (locations.isEmpty()) {
                            item {
                                Text(
                                    "Geen locaties gevonden voor deze filters.",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        }

                        items(locations) { location ->
                            LocationItem(
                                location = location,
                                onClick = {
                                    if (!location.id.isNullOrEmpty()) {
                                        onLocationClick(location.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Welkom in Antwerpen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ontdek de beste plekken in de stad",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CityChips(
    cities: List<City>,
    selectedCityId: String?,
    onCitySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val isSelected = selectedCityId == "Alles" || selectedCityId == null
            FilterChip(
                selected = isSelected,
                onClick = { onCitySelected("Alles") },
                label = { Text("Alle Steden") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        items(cities) { city ->
            val isSelected = city.id == selectedCityId

            FilterChip(
                selected = isSelected,
                onClick = { onCitySelected(city.id) },
                label = { Text(city.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Alles", "Horeca", "Hotel", "Bezienswaardigheid", "Overig")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Categorieën",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = (category == selectedCategory),
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

@Composable
fun PopularLocationsHeader(
    isMapView: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Populaire locaties",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row {
            // Lijst knop
            Button(
                onClick = { onToggle(false) },
                modifier = Modifier.height(40.dp),
                colors = if (!isMapView) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Lijst", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Kaart knop
            Button(
                onClick = { onToggle(true) },
                modifier = Modifier.height(40.dp),
                colors = if (isMapView) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Kaart", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LocationItem(
    location: Location,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            AsyncImage(
                model = location.photoUrl,
                contentDescription = "Foto van ${location.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", location.averageRating),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getCategoryColor(location.category).copy(alpha = 0.15f),
                    contentColor = getCategoryColor(location.category)
                ) {
                    Text(
                        text = location.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "horeca" -> Color(0xFFE65100)
        "hotel" -> Color(0xFF0D47A1)
        "bezienswaardigheid" -> Color(0xFF1B5E20)
        "attractie" -> Color(0xFF1B5E20)
        "winkel" -> Color(0xFF4A148C)
        "overig" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
}