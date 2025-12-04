package edu.ap.opdracht.ui.map

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import edu.ap.opdracht.R
import edu.ap.opdracht.data.model.Location
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.ArrayList

@Composable
fun HomeMapView(
    locations: List<Location>,
    onLocationClick: (String) -> Unit
) {
    val context = LocalContext.current
    val startPoint = GeoPoint(51.2194, 4.4025)

    val customMarkerIcon: Drawable? = remember(context) {
        val originalDrawable = ContextCompat.getDrawable(context, R.drawable.custom_pin)

        if (originalDrawable != null) {
            val bitmap = (originalDrawable as BitmapDrawable).bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, true)
            BitmapDrawable(context.resources, scaledBitmap)
        } else {
            null
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                maxZoomLevel = 19.5
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

                    if (customMarkerIcon != null) {
                        marker.icon = customMarkerIcon
                    }

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

            if (geoPoints.size == 1) {
                mapView.controller.setCenter(geoPoints[0])
                mapView.controller.setZoom(15.0)
            } else if (geoPoints.isNotEmpty()) {
                mapView.post {
                    val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPoints)
                    mapView.zoomToBoundingBox(boundingBox, true, 100)
                    if (mapView.zoomLevelDouble > 15.0) {
                        mapView.controller.setZoom(15.0)
                    }
                }
            } else {
                mapView.controller.setCenter(startPoint)
                mapView.controller.setZoom(13.0)
            }

            mapView.invalidate()
        }
    )
}