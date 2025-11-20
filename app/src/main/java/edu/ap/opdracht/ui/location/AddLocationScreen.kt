package edu.ap.opdracht.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
} else {
    Manifest.permission.READ_EXTERNAL_STORAGE
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    onLocationAdded: () -> Unit,
    viewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Horeca") }
    var currentGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    // State van de ViewModel
    val state by viewModel.state.collectAsState()
    val addressText by viewModel.addressText.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationError = null
            getCurrentLocation(context) { geoPoint ->
                currentGeoPoint = geoPoint
                viewModel.onLocationFetched(context, geoPoint)
            }
        } else {
            locationError = "Locatie permissie is geweigerd."
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Galerij permissie geweigerd", Toast.LENGTH_SHORT).show()
        }
    }

    fun openGallery() {
        when (ContextCompat.checkSelfPermission(context, galleryPermission)) {
            PackageManager.PERMISSION_GRANTED -> {
                galleryLauncher.launch("image/*")
            }
            else -> {
                galleryPermissionLauncher.launch(galleryPermission)
            }
        }
    }

    fun fetchLocation() {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                locationError = null
                getCurrentLocation(context) { geoPoint ->
                    currentGeoPoint = geoPoint
                    viewModel.onLocationFetched(context, geoPoint)
                }
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                onLocationAdded()
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Nieuwe Locatie", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Button(onClick = { openGallery() }, modifier = Modifier.fillMaxWidth()) {
            Text("Kies Foto")
        }
        if (imageUri != null) {
            Spacer(Modifier.height(16.dp))
            AsyncImage(
                model = imageUri,
                contentDescription = "Gekozen foto",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(16.dp))
        Text("Beoordeling", style = MaterialTheme.typography.titleMedium)
        StarRatingInput(
            rating = rating,
            onRatingChange = { rating = it }
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Beschrijving / Eerste commentaar") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(Modifier.height(24.dp))
        if (currentGeoPoint != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = addressText, // Dit is nu bv. "Atomiumsquare 1, 1020 Brussel"
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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

        Button(
            onClick = {
                viewModel.saveLocation(
                    name = name,
                    category = category,
                    geoPoint = currentGeoPoint,
                    imageUri = imageUri,
                    ratingValue = rating.toDouble(),
                    commentText = comment
                )
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

@Composable
fun StarRatingInput(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    maxRating: Int = 5
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Ster $i",
                tint = Color(0xFFFFC107), // Goudkleur
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        onRatingChange(i.toFloat())
                    }
                    .padding(4.dp)
            )
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