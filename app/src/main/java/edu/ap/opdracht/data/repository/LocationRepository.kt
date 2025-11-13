package edu.ap.opdracht.data.repository

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import edu.ap.opdracht.data.model.Location
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.storage
import java.util.UUID

class LocationRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    suspend fun uploadPhoto(imageUri: Uri): Result<String> {
        return try {
            // Maak een unieke bestandsnaam
            val fileName = "locations/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            // Upload het bestand
            storageRef.putFile(imageUri).await()

            // Haal de download-URL op
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
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