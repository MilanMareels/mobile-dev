package edu.ap.opdracht.utils

import android.content.Context
import android.location.Geocoder
import java.util.Locale

data class AddressResult(
    val fullAddress: String,
    val cityName: String,
    val postalCode: String
)

fun getAddressFromCoordinates(context: Context, lat: Double, lon: Double): AddressResult? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lon, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]

            val street = address.thoroughfare ?: "Onbekende straat"
            val number = address.subThoroughfare ?: ""
            val postal = address.postalCode ?: ""
            val city = address.locality ?: "Onbekend"

            val fullText = "$street $number, $postal $city"

            AddressResult(fullText, city, postal)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}