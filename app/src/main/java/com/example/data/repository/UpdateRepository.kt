package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AppUpdateConfig(
    val latestVersion: String,
    val minimumSupportedVersion: String,
    val updateUrl: String,
    val forceUpdate: Boolean,
    val releaseNotes: String,
    val releaseDate: String
)

sealed class UpdateCheckResult {
    object Loading : UpdateCheckResult()
    data class Success(
        val config: AppUpdateConfig,
        val isUpdateAvailable: Boolean,
        val isForceUpdate: Boolean,
        val currentVersion: String,
        val lastCheckedTime: Long
    ) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

class UpdateRepository(private val context: Context) {
    private val TAG = "UpdateRepository"
    private val prefs: SharedPreferences = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)

    // Simulation settings
    var isSimulationEnabled: Boolean
        get() = prefs.getBoolean("simulation_enabled", false)
        set(value) = prefs.edit().putBoolean("simulation_enabled", value).apply()

    var simulatedLatestVersion: String
        get() = prefs.getString("simulated_latest_version", "1.6.0") ?: "1.6.0"
        set(value) = prefs.edit().putString("simulated_latest_version", value).apply()

    var simulatedMinVersion: String
        get() = prefs.getString("simulated_min_version", "1.5.0") ?: "1.5.0"
        set(value) = prefs.edit().putString("simulated_min_version", value).apply()

    var simulatedForceUpdate: Boolean
        get() = prefs.getBoolean("simulated_force_update", false)
        set(value) = prefs.edit().putBoolean("simulated_force_update", value).apply()

    var simulatedReleaseNotes: String
        get() = prefs.getString("simulated_release_notes", "• Added Offline Translate\n• Fixed Referral Links\n• Added Interactive Map Updates") ?: "• Added Offline Translate\n• Fixed Referral Links\n• Added Interactive Map Updates"
        set(value) = prefs.edit().putString("simulated_release_notes", value).apply()

    var lastCheckedTime: Long
        get() = prefs.getLong("last_checked_time", 0L)
        set(value) = prefs.edit().putLong("last_checked_time", value).apply()

    fun getCurrentVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.5.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.5.0"
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Task failed"))
            }
        }
    }

    suspend fun checkForUpdates(): AppUpdateConfig = withContext(Dispatchers.IO) {
        if (isSimulationEnabled) {
            lastCheckedTime = System.currentTimeMillis()
            return@withContext AppUpdateConfig(
                latestVersion = simulatedLatestVersion,
                minimumSupportedVersion = simulatedMinVersion,
                updateUrl = "https://play.google.com/store/apps/details?id=${context.packageName}",
                forceUpdate = simulatedForceUpdate,
                releaseNotes = simulatedReleaseNotes,
                releaseDate = "2026-07-20"
            )
        }

        // Try Firebase Remote Config first
        try {
            val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings).await()
            
            // Set defaults
            val defaults = mapOf(
                "latestVersion" to "1.5.0",
                "minimumSupportedVersion" to "1.4.0",
                "updateUrl" to "https://play.google.com/store/apps/details?id=${context.packageName}",
                "forceUpdate" to false,
                "releaseNotes" to "Bug fixes and performance improvements",
                "releaseDate" to "2026-07-20"
            )
            mFirebaseRemoteConfig.setDefaultsAsync(defaults).await()
            
            // Fetch and activate
            mFirebaseRemoteConfig.fetchAndActivate().await()
            
            val latestVersion = mFirebaseRemoteConfig.getString("latestVersion")
            val minimumSupportedVersion = mFirebaseRemoteConfig.getString("minimumSupportedVersion")
            val updateUrl = mFirebaseRemoteConfig.getString("updateUrl")
            val forceUpdate = mFirebaseRemoteConfig.getBoolean("forceUpdate")
            val releaseNotes = mFirebaseRemoteConfig.getString("releaseNotes")
            val releaseDate = mFirebaseRemoteConfig.getString("releaseDate")

            lastCheckedTime = System.currentTimeMillis()

            if (latestVersion.isNotEmpty()) {
                return@withContext AppUpdateConfig(
                    latestVersion = latestVersion,
                    minimumSupportedVersion = minimumSupportedVersion,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate,
                    releaseNotes = releaseNotes,
                    releaseDate = releaseDate
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote Config fetch failed, trying Firestore: ${e.message}")
        }

        // Fallback to Firestore
        try {
            val firestore = FirebaseFirestore.getInstance()
            val doc = firestore.collection("config").document("app_update").get().await()
            if (doc.exists()) {
                val latestVersion = doc.getString("latestVersion") ?: "1.5.0"
                val minimumSupportedVersion = doc.getString("minimumSupportedVersion") ?: "1.4.0"
                val updateUrl = doc.getString("updateUrl") ?: "https://play.google.com/store/apps/details?id=${context.packageName}"
                val forceUpdate = doc.getBoolean("forceUpdate") ?: false
                val releaseNotes = doc.getString("releaseNotes") ?: "Bug fixes and performance improvements"
                val releaseDate = doc.getString("releaseDate") ?: "2026-07-20"

                lastCheckedTime = System.currentTimeMillis()

                return@withContext AppUpdateConfig(
                    latestVersion = latestVersion,
                    minimumSupportedVersion = minimumSupportedVersion,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate,
                    releaseNotes = releaseNotes,
                    releaseDate = releaseDate
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore fetch failed, using default offline config: ${e.message}")
        }

        // Return offline defaults
        lastCheckedTime = System.currentTimeMillis()
        AppUpdateConfig(
            latestVersion = "1.5.0",
            minimumSupportedVersion = "1.4.0",
            updateUrl = "https://play.google.com/store/apps/details?id=${context.packageName}",
            forceUpdate = false,
            releaseNotes = "No update config found on server.",
            releaseDate = "2026-07-20"
        )
    }

    fun isVersionNewer(current: String, latest: String): Boolean {
        val currentClean = current.split("-").firstOrNull() ?: current
        val latestClean = latest.split("-").firstOrNull() ?: latest
        
        val currentParts = currentClean.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latestClean.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val currVal = currentParts.getOrNull(i) ?: 0
            val latVal = latestParts.getOrNull(i) ?: 0
            if (latVal > currVal) return true
            if (currVal > latVal) return false
        }
        return false
    }

    fun isUpdateRequired(current: String, minimum: String): Boolean {
        val currentClean = current.split("-").firstOrNull() ?: current
        val minClean = minimum.split("-").firstOrNull() ?: minimum
        
        val currentParts = currentClean.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = minClean.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(currentParts.size, minParts.size)
        for (i in 0 until maxLength) {
            val currVal = currentParts.getOrNull(i) ?: 0
            val minVal = minParts.getOrNull(i) ?: 0
            if (minVal > currVal) return true
            if (currVal > minVal) return false
        }
        return false
    }
}
