package edu.ap.opdracht.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class Location(
    @DocumentId
    val id: String? = null,
    val name: String = "",
    val category: String = "",
    val photoUrl: String = "",

    val location: GeoPoint? = null,
    val address: String? = null,

    val addedByUid: String = "",
    val averageRating: Double = 0.0,
    val comments: String = "",

    @ServerTimestamp
    val timestamp: Timestamp? = null
)