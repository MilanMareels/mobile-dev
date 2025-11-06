package edu.ap.opdracht.data.repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.opdracht.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(email: String, password: String, displayName: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Kon geen Firebase gebruiker aanmaken."))

            val user = User(
                uid = firebaseUser.uid,
                displayName = displayName,
                email = email
            )

            firestore.collection("users").document(firebaseUser.uid)
                .set(user).await()

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Gebruikersprofiel niet gevonden in Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}