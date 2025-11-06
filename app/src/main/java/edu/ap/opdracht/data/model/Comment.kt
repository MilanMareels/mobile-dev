package edu.ap.opdracht.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    val text: String = "",
    val commentByUid: String = "",
    val userDisplayName: String = "",

    @ServerTimestamp
    val timestamp: Timestamp? = null
)
