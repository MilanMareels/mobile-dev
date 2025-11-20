package edu.ap.opdracht.data.model

import com.google.firebase.firestore.DocumentId

data class City(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val postalCode: String = ""
)