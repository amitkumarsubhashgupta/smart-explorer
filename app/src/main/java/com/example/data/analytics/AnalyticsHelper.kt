package com.example.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.data.local.UserPreferencesManager
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper private constructor(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
    private val userPreferencesManager = UserPreferencesManager(context.applicationContext)

    companion object {
        private const val TAG = "AnalyticsHelper"
        
        @Volatile
        private var INSTANCE: AnalyticsHelper? = null

        fun getInstance(context: Context): AnalyticsHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsHelper(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Log a custom event if analytics is enabled in preferences.
     */
    fun logEvent(name: String, params: Bundle? = null) {
        val enabled = userPreferencesManager.getAnalytics()
        if (enabled) {
            try {
                firebaseAnalytics.logEvent(name, params)
                Log.d(TAG, "Logged event: $name with params: ${params?.toString() ?: "empty"}")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging event to Firebase Analytics: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "Analytics is disabled by user. Skipped logging: $name")
        }
    }

    /**
     * Track user profile update.
     */
    fun logProfileUpdate(userId: String, name: String, email: String) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putString("name", name)
            putString("email", email)
        }
        logEvent("profile_updated", bundle)
    }

    /**
     * Track onboarding completion.
     */
    fun logOnboardingComplete() {
        logEvent("onboarding_complete")
    }

    /**
     * Track screen views.
     */
    fun logScreenView(screenName: String, screenClass: String = "MainActivity") {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Track authentication events (e.g. login, registration, guest entry).
     */
    fun logAuthEvent(method: String, isSuccess: Boolean, errorMsg: String? = null) {
        val bundle = Bundle().apply {
            putString("auth_method", method)
            putString("status", if (isSuccess) "success" else "failure")
            if (errorMsg != null) {
                putString("error_message", errorMsg)
            }
        }
        logEvent("auth_event", bundle)
    }

    /**
     * Track usage of specific tools like Translator, Currency, Calculator, etc.
     */
    fun logToolUsage(toolName: String, action: String, extraInfo: Bundle? = null) {
        val bundle = Bundle().apply {
            putString("tool_name", toolName)
            putString("action", action)
            extraInfo?.let { putAll(it) }
        }
        logEvent("tool_usage", bundle)
    }

    /**
     * Track when a tool/feature card is clicked/opened.
     */
    fun logFeatureCardUsage(featureName: String) {
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
        }
        logEvent("feature_card_usage", bundle)
    }

    /**
     * Track search query events in Translator/Dictionary.
     */
    fun logSearchQuery(query: String, searchType: String) {
        val bundle = Bundle().apply {
            putString("search_query", query)
            putString("search_type", searchType) // e.g. "dictionary" or "translator"
        }
        logEvent("search_performed", bundle)
    }
}
