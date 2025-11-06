package edu.ap.opdracht.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import edu.ap.opdracht.data.model.Location
import kotlinx.coroutines.tasks.await

class LocationRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun addLocation(location: Location): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                return Result.failure(Exception("Gebruiker is niet ingelogd."))
            }

            val locationWithUser = location.copy(addedByUid = uid)

            db.collection("locations").add(locationWithUser).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}