package edu.ap.opdracht.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location as AndroidLocation // Alias om verwarring te voorkomen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import edu.ap.opdracht.data.model.Comment
import edu.ap.opdracht.data.model.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    detailViewModel: DetailViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val comments by detailViewModel.comments.collectAsStateWithLifecycle()

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val context = LocalContext.current

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = GeoPoint(location.latitude, location.longitude)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.location?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Fout: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val location = uiState.location!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    LocationDetails(location, userLocation)
                }

                item {
                    AddReviewSection(
                        onSubmit = { rating, text ->
                            detailViewModel.submitRating(rating, text)
                        }
                    )
                }

                item {
                    Text(
                        text = "Recente Reacties",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "Nog geen reacties.",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.Gray
                        )
                    }
                } else {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationDetails(location: Location, userLocation: GeoPoint?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = location.photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(250.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        val rating = location.averageRating.toInt()
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "${String.format("%.1f", location.averageRating)} (${location.totalRatings})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = location.category,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            location.location?.let { geoPoint ->
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Locatie op kaart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (userLocation != null) {
                        val distance = calculateDistance(userLocation, geoPoint)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = distance,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Locatie bepalen...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OsmMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    geoPoint = geoPoint,
                    title = location.name,
                    subtitle = location.category
                )
            }
        }
    }
}


@Composable
fun AddReviewSection(onSubmit: (Double, String) -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Geef een rating",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "$i Sterren",
                    tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { rating = i }
                        .padding(4.dp)
                )
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Schrijf een reactie...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (rating > 0) {
                    onSubmit(rating.toDouble(), comment)
                    rating = 0
                    comment = ""
                }
            },
            enabled = rating > 0,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Plaats Review")
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (comment.userDisplayName.isNotBlank()) comment.userDisplayName else "Anoniem",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun calculateDistance(start: GeoPoint, end: GeoPoint): String {
    val results = FloatArray(1)
    AndroidLocation.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    val distanceInMeters = results[0]

    return if (distanceInMeters >= 1000) {
        String.format("%.1f km", distanceInMeters / 1000)
    } else {
        String.format("%.0f meter", distanceInMeters)
    }
}