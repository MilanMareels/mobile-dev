package edu.ap.opdracht.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import edu.ap.opdracht.data.model.Location
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.ArrayList
import kotlin.collections.forEach

@Composable
fun HomeMapView(
    locations: List<Location>,
    onLocationClick: (String) -> Unit
) {
    val startPoint = GeoPoint(51.2194, 4.4025)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // OPTIE 1: Beperk hoe ver je überhaupt mag inzoomen in de hele app
                maxZoomLevel = 18.0
                minZoomLevel = 4.0

                controller.setCenter(startPoint)
                controller.setZoom(13.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val geoPoints = ArrayList<GeoPoint>()

            locations.forEach { location ->
                location.location?.let { firebaseGeo ->
                    val marker = Marker(mapView)
                    val osmPoint = GeoPoint(firebaseGeo.latitude, firebaseGeo.longitude)

                    marker.position = osmPoint
                    geoPoints.add(osmPoint)

                    marker.title = location.name
                    marker.snippet = location.category
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    marker.setOnMarkerClickListener { m, _ ->
                        if (m.isInfoWindowShown) m.closeInfoWindow() else m.showInfoWindow()
                        true
                    }

                    mapView.overlays.add(marker)
                }
            }

            // --- DE FIX ZIT HIER ---
            if (geoPoints.size == 1) {
                // SCENARIO 1: Er is precies één locatie.
                // Zoom niet naar bounding box, maar centreer gewoon en zet een vaste zoom.
                mapView.controller.setCenter(geoPoints[0])
                mapView.controller.setZoom(15.0) // 15 is een mooi straat-niveau
            }
            else if (geoPoints.isNotEmpty()) {
                // SCENARIO 2: Meerdere locaties.
                mapView.post {
                    val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPoints)
                    // zoomToBoundingBox(box, animated, border, maxZoom, speed)
                    // We geven hier 15.0 mee als 'maxZoom' parameter (als je library versie dit ondersteunt)
                    // Als jouw versie die parameters niet heeft, gebruik: mapView.zoomToBoundingBox(boundingBox, true, 100)

                    // Probeer de veilige manier (standaard):
                    mapView.zoomToBoundingBox(boundingBox, true, 100)

                    // Check achteraf: als hij té ver is ingezoomd (omdat punten dicht bij elkaar liggen), zoom uit.
                    if (mapView.zoomLevelDouble > 15.0) {
                        mapView.controller.setZoom(15.0)
                    }
                }
            }
            else {
                // SCENARIO 3: Geen locaties.
                mapView.controller.setCenter(startPoint)
                mapView.controller.setZoom(13.0)
            }

            mapView.invalidate()
        }
    )
}