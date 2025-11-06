package edu.ap.opdracht.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    onLocationAdded: () -> Unit,
    viewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Horeca") } // AC 4
    var currentGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // State van de ViewModel
    val state by viewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationError = null
            getCurrentLocation(context) { geoPoint ->
                currentGeoPoint = geoPoint
            }
        } else {
            locationError = "Locatie permissie is geweigerd."
        }
    }

    fun fetchLocation() {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                // Permissie is al verleend
                locationError = null
                getCurrentLocation(context) { geoPoint ->
                    currentGeoPoint = geoPoint
                }
            }
            else -> {
                // Vraag permissie aan
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchLocation()
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is AddLocationState.Success -> {
                Toast.makeText(context, "Locatie opgeslagen!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onLocationAdded() // Navigeer terug
            }
            is AddLocationState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Nieuwe Locatie", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Naam
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Naam van de locatie") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        CategoryDropdown(
            selectedCategory = category,
            onCategorySelected = { category = it }
        )
        Spacer(Modifier.height(24.dp))

        // Locatie info
        if (currentGeoPoint != null) {
            Text(
                "Locatie gevonden: ${currentGeoPoint!!.latitude}, ${currentGeoPoint!!.longitude}",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                locationError ?: "Locatie wordt opgehaald...",
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = { fetchLocation() }) {
                Text("Haal locatie opnieuw op")
            }
        }

        Spacer(Modifier.height(32.dp))

        // Opslaan knop
        Button(
            onClick = {
                viewModel.saveLocation(name, category, currentGeoPoint)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is AddLocationState.Loading && currentGeoPoint != null
        ) {
            if (state is AddLocationState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Opslaan")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Horeca", "Hotel", "Bezienswaardigheid", "Overig")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categorie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(context: Context, onLocationFetched: (GeoPoint) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                onLocationFetched(geoPoint)
            } else {
                // Soms is 'lastLocation' null, probeer een 'currentLocation'
                // (Dit kun je later verfijnen als het nodig is)
                // Voor nu is dit voldoende.
            }
        }
}