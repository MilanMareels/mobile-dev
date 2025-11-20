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
                controller.setZoom(13.0)
                controller.setCenter(startPoint)
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
                        if (m.isInfoWindowShown) {
                            m.closeInfoWindow()
                        } else {
                            m.showInfoWindow()
                        }
                        true
                    }

                    mapView.overlays.add(marker)
                }
            }

            if (geoPoints.isNotEmpty()) {
                mapView.post {
                    val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPoints)
                    mapView.zoomToBoundingBox(boundingBox, true, 100)
                }
            } else {
                mapView.controller.setCenter(startPoint)
                mapView.controller.setZoom(13.0)
            }

            mapView.invalidate()
        }
    )
}