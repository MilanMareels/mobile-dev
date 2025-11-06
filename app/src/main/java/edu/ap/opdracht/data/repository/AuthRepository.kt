package edu.ap.opdracht.data.repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.opdracht.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    // Haal de instanties van Auth en Firestore op
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Registreert een nieuwe gebruiker met e-mail, wachtwoord en displaynaam.
     * 1. Maakt de gebruiker aan in Firebase Authentication.
     * 2. Slaat de gebruikersgegevens (inclusief displayName) op in Firestore.
     */
    suspend fun registerUser(email: String, password: String, displayName: String): Result<User> {
        return try {
            // Stap 1: Maak gebruiker aan in Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Controleer of het gelukt is en we een user hebben
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Kon geen Firebase gebruiker aanmaken."))

            // Stap 2: Maak ons eigen User-object
            val user = User(
                uid = firebaseUser.uid,
                displayName = displayName,
                email = email
            )

            // Stap 3: Sla het User-object op in Firestore
            // We gebruiken de UID als de "key" (document ID) in een 'users' collectie
            firestore.collection("users").document(firebaseUser.uid)
                .set(user).await()

            // Als alles goed gaat, geef de gemaakte gebruiker terug
            Result.success(user)

        } catch (e: Exception) {
            // Vang alle fouten af (bijv. "email already in use")
            Result.failure(e)
        }
    }
}