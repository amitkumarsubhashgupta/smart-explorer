package com.example.data.repository

import com.example.data.local.UserPreferencesManager
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String,
    val isGuest: Boolean = false,
    val loginProvider: String = "email",
    val emailVerified: Boolean = false
)

// Extension function to await Firebase tasks reliably
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Firebase operation failed without exception."))
        }
    }
}

class AuthRepository(private val preferencesManager: UserPreferencesManager) {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _currentUser = MutableStateFlow<UserProfile?>(preferencesManager.getSavedUserSession())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Map exceptions to helpful and friendly user messages
    private fun mapFirebaseException(e: Exception): Exception {
        val friendlyMessage = when (e) {
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please register first."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect password or email formatting. Please try again."
            is FirebaseAuthUserCollisionException -> "This email address is already in use by another account."
            is FirebaseAuthWeakPasswordException -> "The password must be at least 6 characters long."
            is FirebaseNetworkException -> "A network error occurred. Please check your internet connection."
            else -> {
                val msg = e.message ?: ""
                when {
                    msg.contains("too-many-requests", ignoreCase = true) -> 
                        "Too many unsuccessful attempts. Please try again later."
                    msg.contains("wrong-password", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true) ->
                        "Incorrect password. Please try again."
                    else -> e.localizedMessage ?: "An unexpected authentication error occurred."
                }
            }
        }
        return Exception(friendlyMessage)
    }

    // Helper to store user details in Firestore
    private suspend fun syncUserProfileWithFirestore(user: UserProfile, provider: String) {
        try {
            val userRef = firestore.collection("users").document(user.uid)
            val userSnapshot = try {
                userRef.get().awaitTask()
            } catch (e: Exception) {
                null
            }

            val data = hashMapOf<String, Any>(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "loginProvider" to provider,
                "lastLogin" to Timestamp.now(),
                "emailVerified" to user.emailVerified
            )

            if (userSnapshot == null || !userSnapshot.exists()) {
                data["createdAt"] = Timestamp.now()
            }

            userRef.set(data, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 1. Google Sign-In Integration
    suspend fun signInWithGoogle(uid: String, name: String, email: String, photoUrl: String): Result<UserProfile> {
        _isLoading.value = true
        return try {
            val user = UserProfile(
                uid = uid,
                name = name,
                email = email,
                photoUrl = photoUrl.ifBlank { "https://picsum.photos/seed/${name.lowercase().replace(" ", "")}/200/200" },
                isGuest = false,
                loginProvider = "google",
                emailVerified = true
            )
            syncUserProfileWithFirestore(user, "google")
            preferencesManager.saveUserSession(user)
            _currentUser.value = user
            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(mapFirebaseException(e))
        }
    }

    // 2. Email Sign-Up (Registration)
    suspend fun registerWithEmail(name: String, email: String, password: String): Result<UserProfile> {
        _isLoading.value = true
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).awaitTask()
            val firebaseUser = authResult.user ?: throw Exception("Failed to retrieve user registration credentials.")

            // Send verification email immediately
            firebaseUser.sendEmailVerification().awaitTask()

            val user = UserProfile(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                photoUrl = "https://picsum.photos/seed/${name.lowercase().trim().replace(" ", "")}/200/200",
                isGuest = false,
                loginProvider = "email",
                emailVerified = false
            )

            syncUserProfileWithFirestore(user, "email")
            preferencesManager.saveUserSession(user)
            _currentUser.value = user
            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(mapFirebaseException(e))
        }
    }

    // 3. Email Sign-In
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        _isLoading.value = true
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).awaitTask()
            val firebaseUser = authResult.user ?: throw Exception("Failed to sign in. User not found.")

            val emailVerified = firebaseUser.isEmailVerified
            val name = firebaseUser.displayName ?: email.substringBefore("@")
            val photoUrl = firebaseUser.photoUrl?.toString() ?: "https://picsum.photos/seed/${name.lowercase().trim().replace(" ", "")}/200/200"

            val user = UserProfile(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                photoUrl = photoUrl,
                isGuest = false,
                loginProvider = "email",
                emailVerified = emailVerified
            )

            syncUserProfileWithFirestore(user, "email")
            preferencesManager.saveUserSession(user)
            _currentUser.value = user
            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(mapFirebaseException(e))
        }
    }

    // 4. Send Email Verification
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: throw Exception("No authenticated user session found.")
            firebaseUser.sendEmailVerification().awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    // 5. Refresh Verification Status
    suspend fun refreshVerificationStatus(): Result<UserProfile> {
        _isLoading.value = true
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: throw Exception("No authenticated user session found.")
            firebaseUser.reload().awaitTask()
            
            val isVerified = firebaseUser.isEmailVerified
            val current = _currentUser.value ?: throw Exception("No current session found.")
            val updated = current.copy(emailVerified = isVerified)
            
            if (isVerified) {
                syncUserProfileWithFirestore(updated, current.loginProvider)
            }
            preferencesManager.saveUserSession(updated)
            _currentUser.value = updated
            _isLoading.value = false
            Result.success(updated)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(mapFirebaseException(e))
        }
    }

    // 6. Forgot Password (Send Reset Email)
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        _isLoading.value = true
        return try {
            firebaseAuth.sendPasswordResetEmail(email).awaitTask()
            _isLoading.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(mapFirebaseException(e))
        }
    }

    // 7. Guest Sign-In (Firebase Anonymous Auth)
    suspend fun signInAsGuest(): Result<UserProfile> {
        _isLoading.value = true
        return try {
            val authResult = firebaseAuth.signInAnonymously().awaitTask()
            val firebaseUser = authResult.user ?: throw Exception("Anonymous sign in failed.")

            val user = UserProfile(
                uid = firebaseUser.uid,
                name = "Guest Explorer",
                email = "guest@smartexplorer.ai",
                photoUrl = "https://picsum.photos/seed/guest/200/200",
                isGuest = true,
                loginProvider = "guest",
                emailVerified = true
            )

            syncUserProfileWithFirestore(user, "guest")
            preferencesManager.saveUserSession(user)
            _currentUser.value = user
            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            // Fallback locally if firebase anonymous auth is completely blocked or fails
            val localGuest = UserProfile(
                uid = "guest_user_${System.currentTimeMillis()}",
                name = "Guest Explorer",
                email = "guest@smartexplorer.ai",
                photoUrl = "https://picsum.photos/seed/guest/200/200",
                isGuest = true,
                loginProvider = "guest",
                emailVerified = true
            )
            preferencesManager.saveUserSession(localGuest)
            _currentUser.value = localGuest
            _isLoading.value = false
            Result.success(localGuest)
        }
    }

    // 8. Sign Out
    suspend fun signOut() {
        _isLoading.value = true
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        preferencesManager.clearUserSession()
        _currentUser.value = null
        _isLoading.value = false
    }

    // 9. Delete Account (requires re-authentication)
    suspend fun deleteAccount(password: String? = null): Result<Unit> {
        _isLoading.value = true
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: throw Exception("No authenticated user session found.")
            val current = _currentUser.value ?: throw Exception("No active session found.")

            // Re-authenticate if it is email provider and password is provided
            if (current.loginProvider == "email") {
                if (password.isNullOrBlank()) {
                    throw Exception("Password is required to confirm deletion.")
                }
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(current.email, password)
                firebaseUser.reauthenticate(credential).awaitTask()
            }

            // Delete Firestore user profile document
            try {
                firestore.collection("users").document(current.uid).delete().awaitTask()
            } catch (e: Exception) {
                // If firestore deletion fails, log it and proceed with auth deletion
                e.printStackTrace()
            }

            // Delete Firebase Auth User record
            firebaseUser.delete().awaitTask()

            // Clear local preferences and state
            preferencesManager.clearUserSession()
            _currentUser.value = null
            _isLoading.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(mapFirebaseException(e))
        }
    }

    fun updateProfile(name: String, email: String, photoUrl: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(name = name, email = email, photoUrl = photoUrl)
        preferencesManager.saveUserSession(updated)
        _currentUser.value = updated
    }

    fun restoreSession(user: UserProfile) {
        preferencesManager.saveUserSession(user)
        _currentUser.value = user
    }
}
