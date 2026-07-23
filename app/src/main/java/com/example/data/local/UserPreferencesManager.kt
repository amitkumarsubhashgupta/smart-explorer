package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_explorer_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_APP_LANGUAGE = "pref_app_language"
        const val KEY_TRANSLATOR_LANG = "pref_translator_lang"
        const val KEY_WEATHER_UNIT = "pref_weather_unit"
        const val KEY_PUSH_NOTIFICATIONS = "pref_push_notifications"
        const val KEY_NOTIF_NEWS = "pref_notif_news"
        const val KEY_NOTIF_WEATHER = "pref_notif_weather"
        const val KEY_NOTIF_FEATURES = "pref_notif_features"
        const val KEY_PRIVACY_MODE = "pref_privacy_mode"
        const val KEY_CACHE_SETTINGS = "pref_cache_settings"
        const val KEY_OFFLINE_MODE = "pref_offline_mode"
        const val KEY_AI_SUGGESTIONS = "pref_ai_suggestions"
        const val KEY_ANALYTICS = "pref_analytics"
        const val KEY_BIOMETRIC_LOCK = "pref_biometric_lock"
        const val KEY_NEWS_FILTER = "pref_news_filter"

        // Session keys
        const val KEY_SESSION_ACTIVE = "session_active"
        const val KEY_SESSION_UID = "session_uid"
        const val KEY_SESSION_NAME = "session_name"
        const val KEY_SESSION_EMAIL = "session_email"
        const val KEY_SESSION_PHOTO = "session_photo"
        const val KEY_SESSION_GUEST = "session_guest"
    }

    // Settings flows
    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<String> = _themeFlow.asStateFlow()

    private val _appLanguageFlow = MutableStateFlow(getAppLanguage())
    val appLanguageFlow: StateFlow<String> = _appLanguageFlow.asStateFlow()

    private val _translatorLangFlow = MutableStateFlow(getTranslatorLang())
    val translatorLangFlow: StateFlow<String> = _translatorLangFlow.asStateFlow()

    private val _weatherUnitFlow = MutableStateFlow(getWeatherUnit())
    val weatherUnitFlow: StateFlow<String> = _weatherUnitFlow.asStateFlow()

    private val _pushNotificationsFlow = MutableStateFlow(getPushNotifications())
    val pushNotificationsFlow: StateFlow<Boolean> = _pushNotificationsFlow.asStateFlow()

    private val _notifNewsFlow = MutableStateFlow(getNotifNews())
    val notifNewsFlow: StateFlow<Boolean> = _notifNewsFlow.asStateFlow()

    private val _notifWeatherFlow = MutableStateFlow(getNotifWeather())
    val notifWeatherFlow: StateFlow<Boolean> = _notifWeatherFlow.asStateFlow()

    private val _notifFeaturesFlow = MutableStateFlow(getNotifFeatures())
    val notifFeaturesFlow: StateFlow<Boolean> = _notifFeaturesFlow.asStateFlow()

    private val _privacyModeFlow = MutableStateFlow(getPrivacyMode())
    val privacyModeFlow: StateFlow<Boolean> = _privacyModeFlow.asStateFlow()

    private val _cacheSettingsFlow = MutableStateFlow(getCacheSettings())
    val cacheSettingsFlow: StateFlow<String> = _cacheSettingsFlow.asStateFlow()

    private val _offlineModeFlow = MutableStateFlow(getOfflineMode())
    val offlineModeFlow: StateFlow<Boolean> = _offlineModeFlow.asStateFlow()

    private val _aiSuggestionsFlow = MutableStateFlow(getAiSuggestions())
    val aiSuggestionsFlow: StateFlow<Boolean> = _aiSuggestionsFlow.asStateFlow()

    private val _analyticsFlow = MutableStateFlow(getAnalytics())
    val analyticsFlow: StateFlow<Boolean> = _analyticsFlow.asStateFlow()

    private val _biometricLockFlow = MutableStateFlow(getBiometricLock())
    val biometricLockFlow: StateFlow<Boolean> = _biometricLockFlow.asStateFlow()

    private val _newsFilterFlow = MutableStateFlow(getNewsFilter())
    val newsFilterFlow: StateFlow<String> = _newsFilterFlow.asStateFlow()

    private val _accentColorFlow = MutableStateFlow(getAccentColor())
    val accentColorFlow: StateFlow<String> = _accentColorFlow.asStateFlow()

    // Getters and Setters
    fun getTheme(): String = prefs.getString(KEY_THEME, "system") ?: "system"
    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _themeFlow.value = theme
    }

    fun getAccentColor(): String = prefs.getString("pref_accent_color", "default") ?: "default"
    fun setAccentColor(color: String) {
        prefs.edit().putString("pref_accent_color", color).apply()
        _accentColorFlow.value = color
    }

    fun getCountrySearchHistory(): List<String> {
        val historyStr = prefs.getString("country_search_history", "") ?: ""
        if (historyStr.isEmpty()) return emptyList()
        return historyStr.split("|:|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun addCountrySearchQuery(query: String) {
        if (query.isBlank()) return
        val current = getCountrySearchHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 8) current.removeAt(current.lastIndex)
        prefs.edit().putString("country_search_history", current.joinToString("|:|")).apply()
    }

    fun clearCountrySearchHistory() {
        prefs.edit().remove("country_search_history").apply()
    }

    fun getTranslationSearchHistory(): List<String> {
        val historyStr = prefs.getString("translation_search_history", "") ?: ""
        if (historyStr.isEmpty()) return emptyList()
        return historyStr.split("|:|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun addTranslationSearchQuery(query: String) {
        if (query.isBlank()) return
        val current = getTranslationSearchHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 8) current.removeAt(current.lastIndex)
        prefs.edit().putString("translation_search_history", current.joinToString("|:|")).apply()
    }

    fun clearTranslationSearchHistory() {
        prefs.edit().remove("translation_search_history").apply()
    }

    fun getAppLanguage(): String = prefs.getString(KEY_APP_LANGUAGE, "English") ?: "English"
    fun setAppLanguage(lang: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, lang).apply()
        _appLanguageFlow.value = lang
    }

    fun getTranslatorLang(): String = prefs.getString(KEY_TRANSLATOR_LANG, "Spanish") ?: "Spanish"
    fun setTranslatorLang(lang: String) {
        prefs.edit().putString(KEY_TRANSLATOR_LANG, lang).apply()
        _translatorLangFlow.value = lang
    }

    fun getWeatherUnit(): String = prefs.getString(KEY_WEATHER_UNIT, "Celsius") ?: "Celsius"
    fun setWeatherUnit(unit: String) {
        prefs.edit().putString(KEY_WEATHER_UNIT, unit).apply()
        _weatherUnitFlow.value = unit
    }

    fun getPushNotifications(): Boolean = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
    fun setPushNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
        _pushNotificationsFlow.value = enabled
    }

    fun getNotifNews(): Boolean = prefs.getBoolean(KEY_NOTIF_NEWS, true)
    fun setNotifNews(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_NEWS, enabled).apply()
        _notifNewsFlow.value = enabled
    }

    fun getNotifWeather(): Boolean = prefs.getBoolean(KEY_NOTIF_WEATHER, true)
    fun setNotifWeather(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_WEATHER, enabled).apply()
        _notifWeatherFlow.value = enabled
    }

    fun getNotifFeatures(): Boolean = prefs.getBoolean(KEY_NOTIF_FEATURES, true)
    fun setNotifFeatures(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_FEATURES, enabled).apply()
        _notifFeaturesFlow.value = enabled
    }

    fun getPrivacyMode(): Boolean = prefs.getBoolean(KEY_PRIVACY_MODE, false)
    fun setPrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_MODE, enabled).apply()
        _privacyModeFlow.value = enabled
    }

    fun getCacheSettings(): String = prefs.getString(KEY_CACHE_SETTINGS, "Automatic") ?: "Automatic"
    fun setCacheSettings(mode: String) {
        prefs.edit().putString(KEY_CACHE_SETTINGS, mode).apply()
        _cacheSettingsFlow.value = mode
    }

    fun getOfflineMode(): Boolean = prefs.getBoolean(KEY_OFFLINE_MODE, false)
    fun setOfflineMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OFFLINE_MODE, enabled).apply()
        _offlineModeFlow.value = enabled
    }

    fun getAiSuggestions(): Boolean = prefs.getBoolean(KEY_AI_SUGGESTIONS, true)
    fun setAiSuggestions(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_SUGGESTIONS, enabled).apply()
        _aiSuggestionsFlow.value = enabled
    }

    fun getAnalytics(): Boolean = prefs.getBoolean(KEY_ANALYTICS, true)
    fun setAnalytics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANALYTICS, enabled).apply()
        _analyticsFlow.value = enabled
    }

    fun getBiometricLock(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
    fun setBiometricLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        _biometricLockFlow.value = enabled
    }

    fun getNewsFilter(): String = prefs.getString(KEY_NEWS_FILTER, "None") ?: "None"
    fun setNewsFilter(filter: String) {
        prefs.edit().putString(KEY_NEWS_FILTER, filter).apply()
        _newsFilterFlow.value = filter
    }

    // --- Session Persistence Management ---
    fun saveUserSession(user: UserProfile) {
        prefs.edit()
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .putString(KEY_SESSION_UID, user.uid)
            .putString(KEY_SESSION_NAME, user.name)
            .putString(KEY_SESSION_EMAIL, user.email)
            .putString(KEY_SESSION_PHOTO, user.photoUrl)
            .putBoolean(KEY_SESSION_GUEST, user.isGuest)
            .apply()
    }

    fun getSavedUserSession(): UserProfile? {
        val active = prefs.getBoolean(KEY_SESSION_ACTIVE, false)
        if (!active) return null
        
        val uid = prefs.getString(KEY_SESSION_UID, null) ?: return null
        val name = prefs.getString(KEY_SESSION_NAME, "User") ?: "User"
        val email = prefs.getString(KEY_SESSION_EMAIL, "") ?: ""
        val photoUrl = prefs.getString(KEY_SESSION_PHOTO, "") ?: ""
        val isGuest = prefs.getBoolean(KEY_SESSION_GUEST, false)
        
        return UserProfile(uid, name, email, photoUrl, isGuest)
    }

    fun clearUserSession() {
        prefs.edit()
            .putBoolean(KEY_SESSION_ACTIVE, false)
            .remove(KEY_SESSION_UID)
            .remove(KEY_SESSION_NAME)
            .remove(KEY_SESSION_EMAIL)
            .remove(KEY_SESSION_PHOTO)
            .remove(KEY_SESSION_GUEST)
            .apply()
    }

    fun getViewedCountries(): Set<String> {
        return prefs.getStringSet("viewed_countries_set", emptySet()) ?: emptySet()
    }

    fun addViewedCountry(country: String): Int {
        val current = prefs.getStringSet("viewed_countries_set", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(country.trim().lowercase())
        prefs.edit().putStringSet("viewed_countries_set", current).apply()
        return current.size
    }
}
