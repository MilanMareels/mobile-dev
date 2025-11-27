package edu.ap.opdracht.ui.detail

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
// Importeer de Firebase GeoPoint
import com.google.firebase.firestore.GeoPoint

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    geoPoint: GeoPoint
) {
    val osmGeoPoint = org.osmdroid.util.GeoPoint(geoPoint.latitude, geoPoint.longitude)

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(16.0)
                controller.setCenter(osmGeoPoint)

                val marker = Marker(this)
                marker.position = osmGeoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                this.overlays.add(marker)

                this.invalidate()
            }
        },
        update = { mapView ->
            val newOsmGeoPoint = org.osmdroid.util.GeoPoint(geoPoint.latitude, geoPoint.longitude)
            mapView.controller.setCenter(newOsmGeoPoint)
            (mapView.overlays.firstOrNull() as? Marker)?.let {
                it.position = newOsmGeoPoint
            }
            mapView.invalidate()
        }
    )
}