package edu.ap.opdracht.data.repository

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.storage
import edu.ap.opdracht.data.model.City
import edu.ap.opdracht.data.model.Comment
import edu.ap.opdracht.data.model.Location
import edu.ap.opdracht.data.model.Rating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LocationRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserDisplayName(): String {
        val user = auth.currentUser

        if (!user?.displayName.isNullOrBlank()) {
            return user.displayName!!
        }

        return user?.email
            ?.substringBefore("@")
            ?.substringBefore(".")
            ?.replaceFirstChar { it.uppercase() }
            ?: "Anoniem"
    }

    suspend fun uploadPhoto(imageUri: Uri): Result<String> {
        return try {
            val fileName = "locations/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            storageRef.putFile(imageUri).await()

            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCommentsForLocation(locationId: String): Flow<List<Comment>> {
        return db.collection("locations").document(locationId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Comment::class.java)
            }
    }

    suspend fun addReview(locationId: String, ratingValue: Double, commentText: String): Result<Unit> {
        return try {
            val uid = getCurrentUserId() ?: return Result.failure(Exception("Niet ingelogd"))
            val userName = getCurrentUserDisplayName()

            val locationRef = db.collection("locations").document(locationId)
            val ratingRef = locationRef.collection("ratings").document(uid) // 1 rating per user per locatie
            val commentRef = locationRef.collection("comments").document()

            val newRating = Rating(value = ratingValue, ratedByUid = uid)

            val newComment = Comment(
                text = commentText,
                commentByUid = uid,
                userDisplayName = userName
            )

            db.runTransaction { transaction ->
                val snapshot = transaction.get(locationRef)


                val currentAvg = snapshot.getDouble("averageRating") ?: 0.0
                val currentCount = snapshot.getLong("totalRatings") ?: 0L

                val newTotalRatings = currentCount + 1
                val newAverage = ((currentAvg * currentCount) + ratingValue) / newTotalRatings

                transaction.set(ratingRef, newRating)
                transaction.set(commentRef, newComment)

                transaction.update(locationRef, "averageRating", newAverage)
                transaction.update(locationRef, "totalRatings", newTotalRatings)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun addLocationWithDetails(
        location: Location,
        rating: Rating,
        comment: Comment,
        cityName: String,
        postalCode: String
    ): Result<Unit> {
        return try {
            val uid = getCurrentUserId()
            if (uid == null) {
                return Result.failure(Exception("Gebruiker is niet ingelogd."))
            }

            val finalCityId = getOrCreateCityId(cityName, postalCode)

            if (finalCityId.isBlank()) {
                return Result.failure(Exception("Kon geen geldig City ID genereren"))
            }

            val finalLocation = location.copy(
                cityId = finalCityId,
                averageRating = rating.value,
                totalRatings = 1,
                originalRating = rating.value
            )

            val batch = db.batch()

            val locationRef = db.collection("locations").document()

            batch.set(locationRef, finalLocation)

            val ratingRef = locationRef.collection("ratings").document(uid)
            batch.set(ratingRef, rating)

            val commentRef = locationRef.collection("comments").document()
            batch.set(commentRef, comment)

            batch.commit().await()
            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    fun getCities(): Flow<List<City>> {
        return db.collection("cities")
            .orderBy("name", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(City::class.java)
            }
    }

    fun getAllLocations(category: String?, cityId: String?): Flow<List<Location>> {
        var query: Query = db.collection("locations")

        if (category != null && category != "Alles") {
            query = query.whereEqualTo("category", category)
        }

        if (cityId != null && cityId != "Alles") {
            query = query.whereEqualTo("cityId", cityId)
        }

        return query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Location::class.java)
            }
    }

    fun getMyLocations(category: String?): Flow<List<Location>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }

        var query: Query = db.collection("locations")
            .whereEqualTo("addedByUid", uid)

        if (category != null && category != "Alles") {
            query = query.whereEqualTo("category", category)
        }

        return query.snapshots()
            .map { snapshot ->
                if (snapshot.metadata.hasPendingWrites()) {
                    return@map emptyList<Location>()
                }
                snapshot.toObjects(Location::class.java)
            }
    }

    suspend fun getLocationById(locationId: String): Result<Location> {
        return try {
            val document = db.collection("locations").document(locationId).get().await()
            val location = document.toObject(Location::class.java)
            if (location != null) {
                Result.success(location)
            } else {
                Result.failure(Exception("Locatie niet gevonden"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    private suspend fun getOrCreateCityId(cityName: String, postalCode: String): String {
        val safeName = if (cityName.isBlank()) "Onbekend" else cityName
        val safeZip = if (postalCode.isBlank()) "0000" else postalCode

        val querySnapshot = db.collection("cities")
            .whereEqualTo("name", safeName)
            .whereEqualTo("postalCode", safeZip)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            val existingId = querySnapshot.documents[0].id
            return existingId
        } else {
            val newCityRef = db.collection("cities").document()

            val newCity = City(
                id = newCityRef.id,
                name = safeName,
                postalCode = safeZip
            )

            newCityRef.set(newCity).await()
            return newCityRef.id
        }
    }

}