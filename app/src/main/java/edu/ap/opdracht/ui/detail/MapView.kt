package edu.ap.opdracht.ui.detail

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.GeoPoint
import edu.ap.opdracht.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    geoPoint: GeoPoint,
    title: String,
    subtitle: String
) {
    val context = LocalContext.current
    val osmGeoPoint = org.osmdroid.util.GeoPoint(geoPoint.latitude, geoPoint.longitude)

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
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(16.0)
                controller.setCenter(osmGeoPoint)

                val marker = Marker(this)
                marker.position = osmGeoPoint
                marker.title = title
                marker.snippet = subtitle

                if (customMarkerIcon != null) {
                    marker.icon = customMarkerIcon
                }

                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                marker.setOnMarkerClickListener { m, _ ->
                    if (m.isInfoWindowShown) {
                        m.closeInfoWindow()
                    } else {
                        m.showInfoWindow()
                    }
                    true
                }

                this.overlays.add(marker)
                this.invalidate()
            }
        },
        update = { mapView ->
            val newOsmGeoPoint = org.osmdroid.util.GeoPoint(geoPoint.latitude, geoPoint.longitude)
            mapView.controller.setCenter(newOsmGeoPoint)

            (mapView.overlays.firstOrNull() as? Marker)?.let { marker ->
                marker.position = newOsmGeoPoint
                marker.title = title
                marker.snippet = subtitle

                if (customMarkerIcon != null) {
                    marker.icon = customMarkerIcon
                }
            }
            mapView.invalidate()
        }
    )
}