package edu.ap.opdracht.ui.home // Zorg dat dit je package naam is

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.ap.opdracht.data.model.Location

/**
 * Het hoofdscherm, opgebouwd met een Scaffold om de FAB te tonen
 * en een LazyColumn voor alle scrollbare content.
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onLocationClick: (locationId: String) -> Unit,
    onAddLocationClick: () -> Unit = {} // Callback voor de FAB
) {
    val locations by homeViewModel.locations.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddLocationClick() }, // TODO: Navigeer naar "add location" scherm
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Voeg locatie toe"
                )
            }
        }
    ) { paddingValues ->

        // LazyColumn bevat alle scherm-elementen, zodat alles
        // als één geheel kan scrollen.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding van de Scaffold
        ) {
            // Item 1: Welkomstitel
            item {
                Header()
            }

            // Item 2: Horizontale steden-lijst
            item {
                CityChips()
            }

            // Item 3: Categorieën sectie
            item {
                CategorySection()
            }

            // Item 4: "Populaire locaties" header
            item {
                PopularLocationsHeader()
            }

            // Items 5...N: De dynamische lijst van locaties
            items(locations) { location ->
                LocationItem(
                    location = location,
                    onClick = {
                        // Correctie: location.id is al een String
                        onLocationClick(location.id.toString())
                    }
                )
            }

            // Voeg extra ruimte toe aan de onderkant zodat de
            // lijst niet onder de FAB verborgen zit
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
            text = "Welkome in Antwerpen",
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
fun CityChips() {
    val cities = listOf("Brussel", "Gent", "Amsterdam", "Rotterdam")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cities) { city ->
            Button(onClick = { /* TODO: Filter op stad */ }) {
                Text(city)
            }
        }
    }
}

@Composable
fun CategorySection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            text = "Categorieën",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CategoryItem(icon = Icons.Default.Home, label = "Restaurant")
            CategoryItem(icon = Icons.Default.Home, label = "Hotel")
            CategoryItem(icon = Icons.Default.Star, label = "Attractie")
            CategoryItem(icon = Icons.Default.ShoppingCart, label = "Winkel")
        }
    }
}

@Composable
fun CategoryItem(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { /* TODO: Filter op categorie */ }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PopularLocationsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Populaire locaties",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row {
            // TODO: Maak deze knoppen functioneel (toggle)
            Button(
                onClick = { /* Geselecteerd: Lijst */ },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Lijst", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { /* Geselecteerd: Kaart */ },
                modifier = Modifier.height(40.dp)
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

                    // TODO: Vervang "4.3" door location.rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.3", // TODO: location.rating
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // TODO: Vervang "Hotel in het centrum" door location.description
                Text(
                    text = "Hotel in het centrum", // TODO: location.description
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
        "restaurant" -> Color(0xFFE65100)
        "hotel" -> Color(0xFF0D47A1)
        "attractie" -> Color(0xFF1B5E20)
        "winkel" -> Color(0xFF4A148C)
        else -> MaterialTheme.colorScheme.primary
    }
}