package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.os.Bundle
import com.example.data.local.FavoriteItem
import com.example.data.model.*
import com.example.data.repository.ExplorerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

// Unified sealed UI States for clean architecture success/error bounds
sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ExplorerViewModel(
    private val repository: ExplorerRepository,
    private val analyticsHelper: com.example.data.analytics.AnalyticsHelper,
    private val greetingService: com.example.data.GreetingService,
    private val updateRepository: com.example.data.repository.UpdateRepository,
    private val networkMonitor: com.example.data.network.NetworkMonitor
) : ViewModel() {

    // --- REALTIME NETWORK CONNECTION STATE ---
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // --- APP UPDATE SYSTEM ---
    private val _updateState = MutableStateFlow<com.example.data.repository.UpdateCheckResult>(
        com.example.data.repository.UpdateCheckResult.Success(
            config = com.example.data.repository.AppUpdateConfig(
                latestVersion = updateRepository.getCurrentVersion(),
                minimumSupportedVersion = updateRepository.getCurrentVersion(),
                updateUrl = "",
                forceUpdate = false,
                releaseNotes = "",
                releaseDate = ""
            ),
            isUpdateAvailable = false,
            isForceUpdate = false,
            currentVersion = updateRepository.getCurrentVersion(),
            lastCheckedTime = updateRepository.lastCheckedTime
        )
    )
    val updateState: StateFlow<com.example.data.repository.UpdateCheckResult> = _updateState.asStateFlow()

    private val _isUpdateDismissed = MutableStateFlow(false)
    val isUpdateDismissed: StateFlow<Boolean> = _isUpdateDismissed.asStateFlow()

    // --- USER ACHIEVEMENTS STATE ---
    val achievements: StateFlow<List<com.example.data.local.UserAchievement>> = repository.userAchievementDao.getAllAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SMART RECOMMENDATIONS STATE ---
    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()

    fun dismissUpdateDialog() {
        _isUpdateDismissed.value = true
    }

    fun resetUpdateDismissState() {
        _isUpdateDismissed.value = false
    }

    fun toggleSimulation(enabled: Boolean) {
        updateRepository.isSimulationEnabled = enabled
        checkForUpdates(silent = true)
    }

    fun updateSimulationConfig(
        latestVersion: String,
        minVersion: String,
        forceUpdate: Boolean,
        releaseNotes: String
    ) {
        updateRepository.simulatedLatestVersion = latestVersion
        updateRepository.simulatedMinVersion = minVersion
        updateRepository.simulatedForceUpdate = forceUpdate
        updateRepository.simulatedReleaseNotes = releaseNotes
        checkForUpdates(silent = true)
    }

    fun checkForUpdates(silent: Boolean = false) {
        if (!silent) {
            _updateState.value = com.example.data.repository.UpdateCheckResult.Loading
        }
        viewModelScope.launch {
            try {
                val config = updateRepository.checkForUpdates()
                val currentVersion = updateRepository.getCurrentVersion()
                val isAvailable = updateRepository.isVersionNewer(currentVersion, config.latestVersion)
                val isForce = config.forceUpdate || updateRepository.isUpdateRequired(currentVersion, config.minimumSupportedVersion)
                
                _updateState.value = com.example.data.repository.UpdateCheckResult.Success(
                    config = config,
                    isUpdateAvailable = isAvailable,
                    isForceUpdate = isForce,
                    currentVersion = currentVersion,
                    lastCheckedTime = updateRepository.lastCheckedTime
                )
            } catch (e: Exception) {
                if (!silent) {
                    _updateState.value = com.example.data.repository.UpdateCheckResult.Error(
                        e.message ?: "Failed to check for updates"
                    )
                }
            }
        }
    }

    val greetingState: StateFlow<String> = greetingService.greetingFlow

    fun refreshGreeting() {
        greetingService.refreshGreeting()
    }

    // --- USER PREFERENCES ---
    val themeState: StateFlow<String> = repository.userPreferencesManager.themeFlow
    val appLanguageState: StateFlow<String> = repository.userPreferencesManager.appLanguageFlow
    val translatorLangState: StateFlow<String> = repository.userPreferencesManager.translatorLangFlow
    val weatherUnitState: StateFlow<String> = repository.userPreferencesManager.weatherUnitFlow
    val pushNotificationsState: StateFlow<Boolean> = repository.userPreferencesManager.pushNotificationsFlow
    val notifNewsState: StateFlow<Boolean> = repository.userPreferencesManager.notifNewsFlow
    val notifWeatherState: StateFlow<Boolean> = repository.userPreferencesManager.notifWeatherFlow
    val notifFeaturesState: StateFlow<Boolean> = repository.userPreferencesManager.notifFeaturesFlow
    val privacyModeState: StateFlow<Boolean> = repository.userPreferencesManager.privacyModeFlow
    val cacheSettingsState: StateFlow<String> = repository.userPreferencesManager.cacheSettingsFlow
    val offlineModeState: StateFlow<Boolean> = repository.userPreferencesManager.offlineModeFlow
    val aiSuggestionsState: StateFlow<Boolean> = repository.userPreferencesManager.aiSuggestionsFlow
    val analyticsState: StateFlow<Boolean> = repository.userPreferencesManager.analyticsFlow
    val biometricLockState: StateFlow<Boolean> = repository.userPreferencesManager.biometricLockFlow
    val newsFilterState: StateFlow<String> = repository.userPreferencesManager.newsFilterFlow
    val accentColorState: StateFlow<String> = repository.userPreferencesManager.accentColorFlow

    init {
        checkForUpdates(silent = true)
        seedDefaultAchievements()
        observeNetworkForSync()
        refreshRecommendations()
        syncFcmSubscriptions()
    }

    fun updateTheme(theme: String) {
        repository.userPreferencesManager.setTheme(theme)
        if (theme.lowercase() == "dark") {
            earnAchievement("enabled_dark_mode")
        }
    }

    fun updateAccentColor(color: String) {
        repository.userPreferencesManager.setAccentColor(color)
        if (color == "wallpaper") {
            earnAchievement("enabled_dark_mode")
        }
    }

    // --- SEARCH HISTORY STATE & ACTIONS ---
    private val _countryHistory = MutableStateFlow<List<String>>(repository.userPreferencesManager.getCountrySearchHistory())
    val countryHistory: StateFlow<List<String>> = _countryHistory.asStateFlow()

    private val _translationHistory = MutableStateFlow<List<String>>(repository.userPreferencesManager.getTranslationSearchHistory())
    val translationHistory: StateFlow<List<String>> = _translationHistory.asStateFlow()

    fun addCountryQuery(query: String) {
        repository.userPreferencesManager.addCountrySearchQuery(query)
        _countryHistory.value = repository.userPreferencesManager.getCountrySearchHistory()
    }

    fun clearCountryHistory() {
        repository.userPreferencesManager.clearCountrySearchHistory()
        _countryHistory.value = emptyList()
    }

    fun addTranslationQuery(query: String) {
        repository.userPreferencesManager.addTranslationSearchQuery(query)
        _translationHistory.value = repository.userPreferencesManager.getTranslationSearchHistory()
    }

    fun clearTranslationHistory() {
        repository.userPreferencesManager.clearTranslationSearchHistory()
        _translationHistory.value = emptyList()
    }

    fun updateAppLanguage(lang: String) {
        repository.userPreferencesManager.setAppLanguage(lang)
    }

    fun updateTranslatorLang(lang: String) {
        repository.userPreferencesManager.setTranslatorLang(lang)
    }

    fun updateWeatherUnit(unit: String) {
        repository.userPreferencesManager.setWeatherUnit(unit)
    }

    fun updatePushNotifications(enabled: Boolean) {
        repository.userPreferencesManager.setPushNotifications(enabled)
        syncFcmSubscriptions()
    }

    fun updateNotifNews(enabled: Boolean) {
        repository.userPreferencesManager.setNotifNews(enabled)
        syncFcmSubscriptions()
    }

    fun updateNotifWeather(enabled: Boolean) {
        repository.userPreferencesManager.setNotifWeather(enabled)
        syncFcmSubscriptions()
    }

    fun updateNotifFeatures(enabled: Boolean) {
        repository.userPreferencesManager.setNotifFeatures(enabled)
        syncFcmSubscriptions()
    }

    fun syncFcmSubscriptions() {
        val globalEnabled = pushNotificationsState.value
        val newsEnabled = notifNewsState.value
        val weatherEnabled = notifWeatherState.value
        val featuresEnabled = notifFeaturesState.value

        try {
            val fcm = com.google.firebase.messaging.FirebaseMessaging.getInstance()
            if (globalEnabled) {
                if (newsEnabled) {
                    fcm.subscribeToTopic("news")
                } else {
                    fcm.unsubscribeFromTopic("news")
                }
                if (weatherEnabled) {
                    fcm.subscribeToTopic("weather")
                } else {
                    fcm.unsubscribeFromTopic("weather")
                }
                if (featuresEnabled) {
                    fcm.subscribeToTopic("features")
                } else {
                    fcm.unsubscribeFromTopic("features")
                }
            } else {
                fcm.unsubscribeFromTopic("news")
                fcm.unsubscribeFromTopic("weather")
                fcm.unsubscribeFromTopic("features")
            }
        } catch (e: Exception) {
            android.util.Log.e("ExplorerViewModel", "FCM synchronization failed", e)
        }
    }

    fun updatePrivacyMode(enabled: Boolean) {
        repository.userPreferencesManager.setPrivacyMode(enabled)
    }

    fun updateCacheSettings(mode: String) {
        repository.userPreferencesManager.setCacheSettings(mode)
    }

    fun updateOfflineMode(enabled: Boolean) {
        repository.userPreferencesManager.setOfflineMode(enabled)
    }

    fun updateAiSuggestions(enabled: Boolean) {
        repository.userPreferencesManager.setAiSuggestions(enabled)
    }

    fun updateAnalytics(enabled: Boolean) {
        repository.userPreferencesManager.setAnalytics(enabled)
    }

    fun updateBiometricLock(enabled: Boolean) {
        repository.userPreferencesManager.setBiometricLock(enabled)
    }

    fun updateNewsFilter(filter: String) {
        repository.userPreferencesManager.setNewsFilter(filter)
    }

    // --- FIREBASE EVENT LOGGING ---
    fun logOnboardingComplete() {
        analyticsHelper.logOnboardingComplete()
    }

    fun logScreenView(screenName: String) {
        analyticsHelper.logScreenView(screenName)
    }

    fun logFeatureCardUsage(featureName: String) {
        analyticsHelper.logFeatureCardUsage(featureName)
    }

    fun logSearchQuery(query: String, searchType: String) {
        analyticsHelper.logSearchQuery(query, searchType)
    }

    fun logProfileUpdate(userId: String, name: String, email: String) {
        analyticsHelper.logProfileUpdate(userId, name, email)
    }

    fun logToolUsage(toolName: String, action: String, extraInfo: Bundle? = null) {
        analyticsHelper.logToolUsage(toolName, action, extraInfo)
    }

    // --- FEEDBACK ENGAGEMENT ---
    val feedbackList: StateFlow<List<com.example.data.local.FeedbackItem>> = repository.allFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitFeedback(
        uid: String,
        userName: String,
        email: String,
        profilePhoto: String,
        rating: Int,
        category: String,
        message: String,
        screenshotUrl: String,
        appVersion: String,
        deviceModel: String,
        androidVersion: String,
        loginProvider: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.submitFeedback(
                com.example.data.local.FeedbackItem(
                    uid = uid,
                    userName = userName,
                    email = email,
                    profilePhoto = profilePhoto,
                    rating = rating,
                    category = category,
                    message = message,
                    screenshotUrl = screenshotUrl,
                    appVersion = appVersion,
                    deviceModel = deviceModel,
                    androidVersion = androidVersion,
                    loginProvider = loginProvider,
                    status = "Pending"
                )
            )
            onComplete()
        }
    }

    fun updateFeedbackStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateFeedbackStatus(id, status)
        }
    }

    fun deleteFeedbackById(id: Int) {
        viewModelScope.launch {
            repository.deleteFeedbackById(id)
        }
    }

    // --- FAVORITES STATES ---
    val favorites: StateFlow<List<FavoriteItem>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- WEATHER SCREEN STATES ---
    private val _weatherState = MutableStateFlow<UiState<WeatherData>>(UiState.Idle)
    val weatherState: StateFlow<UiState<WeatherData>> = _weatherState.asStateFlow()

    fun searchWeather(city: String) {
        if (city.isBlank()) return
        _weatherState.value = UiState.Loading
        viewModelScope.launch {
            trackInteraction("weather")
            earnAchievement("first_search")
            repository.getWeather(city)
                .catch { _weatherState.value = UiState.Error(it.message ?: "Failed to load weather") }
                .collect { _weatherState.value = UiState.Success(it) }
        }
    }

    // --- COUNTRIES STATES ---
    private val _countryState = MutableStateFlow<UiState<CountryData>>(UiState.Idle)
    val countryState: StateFlow<UiState<CountryData>> = _countryState.asStateFlow()

    fun searchCountry(name: String) {
        if (name.isBlank()) return
        _countryState.value = UiState.Loading
        viewModelScope.launch {
            trackInteraction("countries")
            earnAchievement("first_search")
            repository.getCountry(name)
                .catch { _countryState.value = UiState.Error(it.message ?: "Failed to load country facts") }
                .collect { country ->
                    if (country != null) {
                        _countryState.value = UiState.Success(country)
                        trackCountryVisit(country.name)
                        addCountryQuery(country.name)
                    } else {
                        _countryState.value = UiState.Error("Country not found")
                    }
                }
        }
    }

    // --- NEWS STATES ---
    private val _newsState = MutableStateFlow<UiState<List<NewsData>>>(UiState.Idle)
    val newsState: StateFlow<UiState<List<NewsData>>> = _newsState.asStateFlow()

    fun loadNews(category: String = "All") {
        _newsState.value = UiState.Loading
        viewModelScope.launch {
            trackInteraction("news")
            val filter = repository.userPreferencesManager.getNewsFilter()
            repository.getNews(category)
                .catch { _newsState.value = UiState.Error(it.message ?: "Failed to fetch news feed") }
                .collect { articles ->
                    val filtered = if (filter == "Safe") {
                        val sensitiveWords = listOf("accident", "death", "crash", "war", "conflict", "bomb", "blast", "kill", "murder", "terror", "crisis", "illegal", "dead", "shoot", "attack", "casualty")
                        articles.filter { article ->
                            sensitiveWords.none { word ->
                                article.title.lowercase(Locale.ROOT).contains(word) ||
                                article.summary.lowercase(Locale.ROOT).contains(word)
                            }
                        }
                    } else {
                        articles
                    }
                    _newsState.value = UiState.Success(filtered)
                }
        }
    }

    // --- JOKES STATES ---
    private val _jokeState = MutableStateFlow<UiState<JokeData>>(UiState.Idle)
    val jokeState: StateFlow<UiState<JokeData>> = _jokeState.asStateFlow()

    fun loadRandomJoke(category: String = "All") {
        _jokeState.value = UiState.Loading
        viewModelScope.launch {
            repository.getRandomJoke(category)
                .catch { _jokeState.value = UiState.Error(it.message ?: "Failed to tell a joke") }
                .collect { _jokeState.value = UiState.Success(it) }
        }
    }

    // --- CURRENCY CONVERTER STATES ---
    private val _currenciesState = MutableStateFlow<UiState<List<CurrencyRate>>>(UiState.Idle)
    val currenciesState: StateFlow<UiState<List<CurrencyRate>>> = _currenciesState.asStateFlow()

    val sourceAmount = MutableStateFlow("1.0")
    val sourceCurrency = MutableStateFlow("USD")
    val targetCurrency = MutableStateFlow("EUR")
    val convertedAmount = MutableStateFlow("0.92")

    fun loadCurrencies() {
        _currenciesState.value = UiState.Loading
        viewModelScope.launch {
            trackInteraction("currency")
            repository.getCurrencyRates()
                .catch { _currenciesState.value = UiState.Error(it.message ?: "Failed to fetch conversion rates") }
                .collect { rates ->
                    _currenciesState.value = UiState.Success(rates)
                    performCurrencyConversion(rates)
                }
        }
    }

    fun onSourceAmountChanged(amount: String) {
        sourceAmount.value = amount
        val state = _currenciesState.value
        if (state is UiState.Success) {
            performCurrencyConversion(state.data)
        }
    }

    fun onSourceCurrencyChanged(code: String) {
        sourceCurrency.value = code
        val state = _currenciesState.value
        if (state is UiState.Success) {
            performCurrencyConversion(state.data)
        }
    }

    fun onTargetCurrencyChanged(code: String) {
        targetCurrency.value = code
        val state = _currenciesState.value
        if (state is UiState.Success) {
            performCurrencyConversion(state.data)
        }
    }

    private fun performCurrencyConversion(rates: List<CurrencyRate>) {
        val amountVal = sourceAmount.value.toDoubleOrNull() ?: 0.0
        val srcRate = rates.find { it.code == sourceCurrency.value }?.rateToUSD ?: 1.0
        val tgtRate = rates.find { it.code == targetCurrency.value }?.rateToUSD ?: 1.0

        // Convert base source amount to USD, then USD to target rate
        val amountInUSD = amountVal / srcRate
        val result = amountInUSD * tgtRate
        convertedAmount.value = String.format(Locale.ROOT, "%.2f", result)

        val bundle = Bundle().apply {
            putString("source_currency", sourceCurrency.value)
            putString("target_currency", targetCurrency.value)
            putDouble("source_amount", amountVal)
            putDouble("converted_amount", result)
        }
        logToolUsage("currency_converter", "convert", bundle)
    }

    // --- TRANSLATOR STATES ---
    private val _translationState = MutableStateFlow<UiState<TranslationResult>>(UiState.Idle)
    val translationState: StateFlow<UiState<TranslationResult>> = _translationState.asStateFlow()

    fun translate(text: String, from: String, to: String) {
        if (text.isBlank()) return
        logSearchQuery(text, "translator")
        val bundle = Bundle().apply {
            putString("from_lang", from)
            putString("to_lang", to)
            putInt("text_length", text.length)
        }
        logToolUsage("translator", "translate", bundle)
        addTranslationQuery(text)
        _translationState.value = UiState.Loading
        viewModelScope.launch {
            trackInteraction("translator")
            earnAchievement("first_search")
            repository.translateText(text, from, to)
                .catch { _translationState.value = UiState.Error(it.message ?: "Translation failed") }
                .collect { _translationState.value = UiState.Success(it) }
        }
    }

    // --- DICTIONARY STATES ---
    private val _dictionaryState = MutableStateFlow<UiState<DictionaryWord>>(UiState.Idle)
    val dictionaryState: StateFlow<UiState<DictionaryWord>> = _dictionaryState.asStateFlow()

    fun lookupWord(word: String) {
        if (word.isBlank()) return
        logSearchQuery(word, "dictionary")
        _dictionaryState.value = UiState.Loading
        viewModelScope.launch {
            trackInteraction("dictionary")
            earnAchievement("first_search")
            repository.getDictionaryWord(word)
                .catch { _dictionaryState.value = UiState.Error(it.message ?: "Dictionary lookup failed") }
                .collect { result ->
                    if (result != null) {
                        _dictionaryState.value = UiState.Success(result)
                    } else {
                        _dictionaryState.value = UiState.Error("Word not found in database")
                    }
                }
        }
    }

    // --- UNIFIED SEARCH STATE (FOR UNIFIED SEARCH SCREEN) ---
    val searchUiQuery = MutableStateFlow("")
    private val _unifiedSearchState = MutableStateFlow<Map<String, UiState<Any>>>(emptyMap())
    val unifiedSearchState: StateFlow<Map<String, UiState<Any>>> = _unifiedSearchState.asStateFlow()

    fun performUnifiedSearch(query: String) {
        searchUiQuery.value = query
        if (query.isBlank()) {
            _unifiedSearchState.value = emptyMap()
            return
        }
        logSearchQuery(query, "unified")

        viewModelScope.launch {
            // Trigger parallel operations
            launch {
                repository.getWeather(query)
                    .catch { updateUnifiedState("WEATHER", UiState.Error(it.message ?: "Failed")) }
                    .collect { updateUnifiedState("WEATHER", UiState.Success(it)) }
            }
            launch {
                repository.getCountry(query)
                    .catch { updateUnifiedState("COUNTRY", UiState.Error(it.message ?: "Failed")) }
                    .collect { country ->
                        if (country != null) {
                            updateUnifiedState("COUNTRY", UiState.Success(country))
                        } else {
                            updateUnifiedState("COUNTRY", UiState.Error("Not found"))
                        }
                    }
            }
            launch {
                repository.getDictionaryWord(query)
                    .catch { updateUnifiedState("DICTIONARY", UiState.Error(it.message ?: "Failed")) }
                    .collect { word ->
                        if (word != null) {
                            updateUnifiedState("DICTIONARY", UiState.Success(word))
                        } else {
                            updateUnifiedState("DICTIONARY", UiState.Error("Not found"))
                        }
                    }
            }
        }
    }

    private fun updateUnifiedState(key: String, state: UiState<Any>) {
        val current = _unifiedSearchState.value.toMutableMap()
        current[key] = state
        _unifiedSearchState.value = current
    }

    // --- FAVORITES MANAGEMENT ACTIONS ---
    fun toggleFavorite(type: String, key: String, title: String, subtitle: String, content: String) {
        val id = "$type:$key"
        viewModelScope.launch {
            val exists = favorites.value.any { it.id == id }
            if (exists) {
                repository.removeFavorite(id)
            } else {
                repository.addFavorite(
                    FavoriteItem(
                        id = id,
                        type = type,
                        title = title,
                        subtitle = subtitle,
                        content = content
                    )
                )
                earnAchievement("favorite_saved")
            }
        }
    }

    fun removeFavoriteById(id: String) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }

    fun isItemFavoriteFlow(type: String, key: String): Flow<Boolean> {
        return favorites.map { list -> list.any { it.id == "$type:$key" } }
    }

    // --- VAULT NOTES State & Actions ---
    private val _notesList = MutableStateFlow<List<com.example.data.local.CachedData>>(emptyList())
    val notesList: StateFlow<List<com.example.data.local.CachedData>> = _notesList.asStateFlow()

    fun loadAllNotes() {
        viewModelScope.launch {
            _notesList.value = repository.getAllNotes()
        }
    }

    fun addVaultNote(title: String, content: String) {
        val id = System.currentTimeMillis().toString()
        viewModelScope.launch {
            trackInteraction("notes")
            repository.saveNote(id, title, content)
            loadAllNotes()
        }
    }

    fun deleteVaultNote(id: String) {
        val cleanId = id.replace("NOTE:", "")
        viewModelScope.launch {
            repository.deleteNoteById(cleanId)
            loadAllNotes()
        }
    }

    fun earnAchievement(id: String) {
        viewModelScope.launch {
            val achievement = repository.userAchievementDao.getAchievementById(id)
            if (achievement != null && !achievement.isEarned) {
                val updated = achievement.copy(isEarned = true, earnedTimestamp = System.currentTimeMillis())
                repository.userAchievementDao.insertAchievement(updated)
                syncAchievementsWithFirebase()
            }
        }
    }

    fun trackInteraction(featureId: String) {
        viewModelScope.launch {
            val existing = repository.userInteractionDao.getInteractionByFeature(featureId)
            if (existing == null) {
                repository.userInteractionDao.insertInteraction(com.example.data.local.UserInteraction(featureId, 1))
            } else {
                repository.userInteractionDao.incrementUseCount(featureId, System.currentTimeMillis())
            }
            refreshRecommendations()

            if (featureId == "translator") {
                val updated = repository.userInteractionDao.getInteractionByFeature(featureId)
                if (updated != null && updated.useCount >= 10) {
                    earnAchievement("used_translator_10")
                }
            }
        }
    }

    fun trackCountryVisit(countryName: String) {
        viewModelScope.launch {
            val count = repository.userPreferencesManager.addViewedCountry(countryName)
            if (count >= 5) {
                earnAchievement("visited_5_countries")
            }
        }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            val interactions = repository.userInteractionDao.getAllInteractions()
            val highest = interactions.maxByOrNull { it.useCount }
            
            val list = when (highest?.featureId) {
                "translator" -> listOf(
                    Recommendation("rec_countries_lang", "Explore Spain & France", "Discover rich country facts and information of Spanish or French territories.", "https://picsum.photos/seed/france_spain/400/250", "Explore Now", "countries"),
                    Recommendation("rec_news_lang", "Read Global News", "Catch up with the latest translated international headlines and reports.", "https://picsum.photos/seed/news_global/400/250", "Read News", "news")
                )
                "weather" -> listOf(
                    Recommendation("rec_countries_weather", "Travel Around the Globe", "Check geographical profiles, capitals, and populations of various nations.", "https://picsum.photos/seed/globe_weather/400/250", "View Countries", "countries"),
                    Recommendation("rec_currency_weather", "Plan Your Travel Budget", "Convert international exchange rates instantly for your destination.", "https://picsum.photos/seed/budget_weather/400/250", "Convert Currency", "currency")
                )
                "news" -> listOf(
                    Recommendation("rec_clock_news", "Global Time Telemetry", "Coordinate clock timezones while tracking live international headlines.", "https://picsum.photos/seed/clock_news/400/250", "Check Clocks", "world_clock"),
                    Recommendation("rec_notes_news", "Log News In Secure Notes", "Write down thoughts or key summaries in your offline Vault Notes.", "https://picsum.photos/seed/notes_news/400/250", "Open Vault", "vault_notes")
                )
                "countries" -> listOf(
                    Recommendation("rec_translator_countries", "Speak Like a Native", "Translate phrases or vocabulary from English to Spanish, Hindi, or Japanese.", "https://picsum.photos/seed/translator_countries/400/250", "Translate Now", "translator"),
                    Recommendation("rec_currency_countries", "Currency Exchange Values", "Check official currency conversion and exchange values of nations.", "https://picsum.photos/seed/currency_countries/400/250", "Convert Rates", "currency")
                )
                "dictionary" -> listOf(
                    Recommendation("rec_translator_dict", "Translate Word Definitions", "Instantly translate word pronunciations, phonetic guides, and details.", "https://picsum.photos/seed/translator_dict/400/250", "Go Translate", "translator"),
                    Recommendation("rec_news_dict", "Vocabulary Expansion News", "Enrich your word power and spelling by reading real-time curated feeds.", "https://picsum.photos/seed/news_dict/400/250", "Read Articles", "news")
                )
                "currency" -> listOf(
                    Recommendation("rec_countries_currency", "Regions Using Exchange Coins", "Explore land profiles, facts, and fun details about countries using specific currencies.", "https://picsum.photos/seed/countries_currency/400/250", "View Countries", "countries"),
                    Recommendation("rec_weather_currency", "Target Travel Conditions", "Track live weather forecasts of travel zones before budgeting.", "https://picsum.photos/seed/weather_currency/400/250", "Check Weather", "weather")
                )
                else -> listOf(
                    Recommendation("rec_weather_default", "Explore Weather Radar", "Check forecast telemetry and temperatures for your local city.", "https://picsum.photos/seed/weather_default/400/250", "Track Live", "weather"),
                    Recommendation("rec_translator_default", "Translate Core Phrases", "Instantly convert text between English, Spanish, Japanese, and Hindi.", "https://picsum.photos/seed/translator_default/400/250", "Try Translator", "translator"),
                    Recommendation("rec_news_default", "Read Curated News Feed", "Stay informed with safe and filtered technology, business, or general news.", "https://picsum.photos/seed/news_default/400/250", "Check Feed", "news")
                )
            }
            _recommendations.value = list
        }
    }

    private fun seedDefaultAchievements() {
        viewModelScope.launch {
            val defaults = listOf(
                com.example.data.local.UserAchievement("first_search", "First Search Performed", "Searched weather, country details, or dictionary words.", false),
                com.example.data.local.UserAchievement("favorite_saved", "Favorite Item Saved", "Added an item to bookmarks / saved favorites.", false),
                com.example.data.local.UserAchievement("profile_completed", "Profile Completed", "Completed bio, username, and contact information.", false),
                com.example.data.local.UserAchievement("visited_5_countries", "Visited 5 Countries", "Explored country details for 5 unique nations.", false),
                com.example.data.local.UserAchievement("used_translator_10", "Used Translator 10 times", "Used the AI Translator 10 times to translate phrases.", false),
                com.example.data.local.UserAchievement("enabled_dark_mode", "Enabled Dark Mode", "Enabled dark mode theme in appearance settings.", false)
            )
            defaults.forEach { ach ->
                val existing = repository.userAchievementDao.getAchievementById(ach.id)
                if (existing == null) {
                    repository.userAchievementDao.insertAchievement(ach)
                }
            }
            pullAchievementsFromFirebase()
        }
    }

    private fun observeNetworkForSync() {
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    syncDataOnConnectionRestored()
                }
            }
        }
    }

    private fun syncDataOnConnectionRestored() {
        viewModelScope.launch {
            try {
                syncAchievementsWithFirebase()
                pullAchievementsFromFirebase()
                val city = "Mumbai"
                repository.getWeather(city).collect {}
                repository.getNews("All").collect {}
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun syncAchievementsWithFirebase() {
        viewModelScope.launch {
            try {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null && !firebaseUser.isAnonymous) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val allRoomAchievements = mutableListOf<com.example.data.local.UserAchievement>()
                    listOf("first_search", "favorite_saved", "profile_completed", "visited_5_countries", "used_translator_10", "enabled_dark_mode").forEach { id ->
                        repository.userAchievementDao.getAchievementById(id)?.let { allRoomAchievements.add(it) }
                    }
                    val data = allRoomAchievements.associate { it.id to mapOf(
                        "title" to it.title,
                        "description" to it.description,
                        "isEarned" to it.isEarned,
                        "earnedTimestamp" to it.earnedTimestamp
                    )}
                    db.collection("users")
                        .document(firebaseUser.uid)
                        .collection("achievements")
                        .document("status")
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun pullAchievementsFromFirebase() {
        viewModelScope.launch {
            try {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null && !firebaseUser.isAnonymous) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users")
                        .document(firebaseUser.uid)
                        .collection("achievements")
                        .document("status")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                viewModelScope.launch {
                                    document.data?.forEach { (id, value) ->
                                        val map = value as? Map<*, *> ?: return@forEach
                                        val isEarned = map["isEarned"] as? Boolean ?: false
                                        val earnedTimestamp = map["earnedTimestamp"] as? Long
                                        val roomAchievement = repository.userAchievementDao.getAchievementById(id.toString())
                                        if (roomAchievement != null && isEarned && !roomAchievement.isEarned) {
                                            repository.userAchievementDao.insertAchievement(
                                                roomAchievement.copy(isEarned = true, earnedTimestamp = earnedTimestamp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    companion object {
        fun provideFactory(
            explorerRepository: ExplorerRepository,
            analyticsHelper: com.example.data.analytics.AnalyticsHelper,
            greetingService: com.example.data.GreetingService,
            updateRepository: com.example.data.repository.UpdateRepository,
            networkMonitor: com.example.data.network.NetworkMonitor
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExplorerViewModel(explorerRepository, analyticsHelper, greetingService, updateRepository, networkMonitor) as T
                }
            }
    }
}
