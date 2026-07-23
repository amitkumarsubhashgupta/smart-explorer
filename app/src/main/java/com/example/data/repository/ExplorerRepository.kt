package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.CachedData
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteItem
import com.example.data.local.CachedDataDao
import com.example.data.model.*
import com.example.data.network.RetrofitClient
import com.example.data.network.RestCountryResponse
import com.example.data.network.NewsArticle
import com.example.data.network.WeatherResponse
import com.example.data.network.ForecastResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import kotlin.random.Random
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ExplorerRepository(
    private val favoriteDao: FavoriteDao,
    private val cachedDataDao: CachedDataDao,
    private val cacheInvalidationService: com.example.data.local.CacheInvalidationService,
    private val feedbackDao: com.example.data.local.FeedbackDao,
    val userPreferencesManager: com.example.data.local.UserPreferencesManager,
    val userAchievementDao: com.example.data.local.UserAchievementDao,
    val userInteractionDao: com.example.data.local.UserInteractionDao
) {

    // --- FEEDBACK (ROOM) ---
    val allFeedback: Flow<List<com.example.data.local.FeedbackItem>> = feedbackDao.getAllFeedback()

    suspend fun submitFeedback(feedback: com.example.data.local.FeedbackItem) {
        feedbackDao.insertFeedback(feedback)
    }

    suspend fun updateFeedbackStatus(id: Int, status: String) {
        feedbackDao.updateFeedbackStatus(id, status)
    }

    suspend fun deleteFeedbackById(id: Int) {
        feedbackDao.deleteFeedbackById(id)
    }

    suspend fun clearAllFeedback() {
        feedbackDao.clearAllFeedback()
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // --- FAVORITES (ROOM) ---
    val allFavorites: Flow<List<FavoriteItem>> = favoriteDao.getAllFavorites()

    fun getFavoritesByType(type: String): Flow<List<FavoriteItem>> = 
        favoriteDao.getFavoritesByType(type)

    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite(id)

    suspend fun addFavorite(item: FavoriteItem) {
        favoriteDao.insertFavorite(item)
    }

    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }

    // --- CACHE HELPER METHODS ---
    private inline fun <reified T> fromJson(json: String): T? {
        return try {
            moshi.adapter(T::class.java).fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    private inline fun <reified T> toJson(value: T): String {
        return moshi.adapter(T::class.java).toJson(value)
    }

    private fun <T> fromJsonList(json: String, elementClass: Class<T>): List<T>? {
        return try {
            val type = Types.newParameterizedType(List::class.java, elementClass)
            moshi.adapter<List<T>>(type).fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    private fun <T> toJsonList(list: List<T>, elementClass: Class<T>): String {
        val type = Types.newParameterizedType(List::class.java, elementClass)
        return moshi.adapter<List<T>>(type).toJson(list)
    }

    // --- WEATHER SERVICE (OpenWeatherMap with Offline Cache & Fallback) ---
    fun getWeather(city: String): Flow<WeatherData> = flow {
        val cacheId = "WEATHER:${city.trim().lowercase(Locale.ROOT)}"
        val cached = cachedDataDao.getCacheById(cacheId)
        
        // If unexpired cache exists, return it immediately without network request
        if (cached != null && !cacheInvalidationService.isCacheStale("WEATHER", cached.timestamp)) {
            val decoded = fromJson<WeatherData>(cached.content)
            if (decoded != null) {
                emit(decoded)
                return@flow
            }
        }

        try {
            val apiKey = BuildConfig.WEATHER_API_KEY
            if (apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
                throw IllegalStateException("API key missing")
            }
            
            // Fetch current and forecast in parallel
            val current = RetrofitClient.weatherApi.getCurrentWeather(city, apiKey)
            val forecast = RetrofitClient.weatherApi.getForecast(city, apiKey)
            
            val weatherData = mapWeather(current, forecast)
            // Save cache
            cachedDataDao.insertCache(CachedData(cacheId, "WEATHER", city, toJson(weatherData)))
            emit(weatherData)
        } catch (e: Exception) {
            // Read from cache as fallback even if it is stale
            if (cached != null) {
                val decoded = fromJson<WeatherData>(cached.content)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            // Offline/API failure fallback
            emit(getLocalWeatherFallback(city))
        }
    }

    private fun mapWeather(curr: WeatherResponse, fore: ForecastResponse): WeatherData {
        val cityTitle = curr.name ?: "Unknown Location"
        val temp = curr.main?.temp ?: 20.0
        val cond = curr.weather?.firstOrNull()?.main ?: "Clear"
        val humid = curr.main?.humidity ?: 60
        val wind = curr.wind?.speed ?: 5.0
        val press = curr.main?.pressure ?: 1013
        val feels = curr.main?.feels_like ?: temp
        
        // Map 5 unique days from 3-hourly forecast list
        val forecastList = mutableListOf<ForecastDay>()
        val seenDays = mutableSetOf<String>()
        val items = fore.list ?: emptyList()
        for (item in items) {
            val dtTxt = item.dt_txt ?: ""
            if (dtTxt.length >= 10) {
                val dateStr = dtTxt.substring(0, 10) // e.g. "2026-07-17"
                if (!seenDays.contains(dateStr) && forecastList.size < 5) {
                    seenDays.add(dateStr)
                    val forecastTemp = item.main?.temp ?: temp
                    val forecastCond = item.weather?.firstOrNull()?.main ?: "Clear"
                    val dayName = when (forecastList.size) {
                        0 -> "Today"
                        1 -> "Tomorrow"
                        2 -> "Day 3"
                        3 -> "Day 4"
                        else -> "Day 5"
                    }
                    forecastList.add(ForecastDay(dayName, Math.round(forecastTemp * 10.0) / 10.0, forecastCond))
                }
            }
        }
        
        if (forecastList.isEmpty()) {
            forecastList.addAll(listOf(
                ForecastDay("Sat", temp + 1.0, cond),
                ForecastDay("Sun", temp - 0.5, cond),
                ForecastDay("Mon", temp + 2.0, "Sunny"),
                ForecastDay("Tue", temp - 1.2, "Rainy"),
                ForecastDay("Wed", temp + 0.3, "Partly Cloudy")
            ))
        }

        return WeatherData(
            city = cityTitle,
            temperature = Math.round(temp * 10.0) / 10.0,
            condition = cond,
            humidity = humid,
            windSpeed = Math.round(wind * 10.0) / 10.0,
            pressure = press,
            feelsLike = Math.round(feels * 10.0) / 10.0,
            uvIndex = if (cond == "Clear" || cond == "Sunny") 7 else 3,
            forecast = forecastList
        )
    }

    private fun getLocalWeatherFallback(city: String): WeatherData {
        val normalized = city.trim().lowercase(Locale.ROOT)
        val temp = when {
            normalized.contains("london") -> 16.5
            normalized.contains("paris") -> 19.0
            normalized.contains("new york") -> 24.5
            normalized.contains("tokyo") -> 22.0
            normalized.contains("mumbai") -> 31.0
            else -> 15.0 + Random.nextDouble(0.0, 20.0)
        }
        val condition = if (temp > 25) "Sunny" else if (temp > 18) "Partly Cloudy" else "Cloudy"
        return WeatherData(
            city = city.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            temperature = Math.round(temp * 10.0) / 10.0,
            condition = condition,
            humidity = 65,
            windSpeed = 12.4,
            pressure = 1012,
            feelsLike = Math.round((temp + 0.5) * 10.0) / 10.0,
            uvIndex = 5,
            forecast = listOf(
                ForecastDay("Sat", Math.round((temp + 1.2) * 10.0) / 10.0, condition),
                ForecastDay("Sun", Math.round((temp - 0.5) * 10.0) / 10.0, "Sunny"),
                ForecastDay("Mon", Math.round((temp + 0.3) * 10.0) / 10.0, "Partly Cloudy"),
                ForecastDay("Tue", Math.round((temp - 1.5) * 10.0) / 10.0, "Rainy"),
                ForecastDay("Wed", Math.round((temp + 2.0) * 10.0) / 10.0, "Sunny")
            )
        )
    }

    // --- COUNTRIES SERVICE (REST Countries Offline Cache & Fallback) ---
    fun getCountry(name: String): Flow<CountryData?> = flow {
        val cacheId = "COUNTRY:${name.trim().lowercase(Locale.ROOT)}"
        try {
            val responseList = RetrofitClient.countriesApi.getCountryByName(name)
            val item = responseList.firstOrNull()
            if (item != null) {
                val mapped = mapCountry(item)
                cachedDataDao.insertCache(CachedData(cacheId, "COUNTRY", name, toJson(mapped)))
                emit(mapped)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            val cached = cachedDataDao.getCacheById(cacheId)
            if (cached != null) {
                val decoded = fromJson<CountryData>(cached.content)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            emit(getLocalCountryFallback(name))
        }
    }

    private fun mapCountry(item: RestCountryResponse): CountryData {
        val nameCommon = item.name?.common ?: "Unknown"
        val nameOfficial = item.name?.official ?: "Unknown Official"
        val capitalStr = item.capital?.firstOrNull() ?: item.capitals?.firstOrNull() ?: "Unknown Capital"
        val pop = item.population ?: 0L
        val reg = item.region ?: "Unknown Region"
        val subreg = item.subregion ?: "Unknown Subregion"
        val flagEmo = item.flag ?: "🌐"
        val currCode = item.currencies?.keys?.firstOrNull() ?: "USD"
        val langList = item.languages?.values?.toList() ?: listOf("English")
        val countryArea = item.area ?: 0.0
        
        val mapLink = item.maps?.googleMaps ?: "https://maps.google.com/?q=$nameCommon"
        val fact = "$nameCommon ($nameOfficial) has a total area of $countryArea sq km. Google Maps locator: $mapLink"

        return CountryData(
            name = nameCommon,
            officialName = nameOfficial,
            capital = capitalStr,
            population = pop,
            region = reg,
            subregion = subreg,
            flagEmoji = flagEmo,
            currency = currCode,
            languages = langList,
            area = countryArea,
            funFact = fact
        )
    }

    private fun getLocalCountryFallback(name: String): CountryData? {
        if (name.isBlank()) return null
        val capped = name.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        val flag = when (capped.lowercase()) {
            "india" -> "🇮🇳"
            "france" -> "🇫🇷"
            "japan" -> "🇯🇵"
            "united kingdom", "uk" -> "🇬🇧"
            "united states", "usa" -> "🇺🇸"
            else -> "🌐"
        }
        return CountryData(
            name = capped,
            officialName = "Republic of $capped",
            capital = "${capped} City",
            population = 45000000,
            region = "Global Explorer",
            subregion = "Cosmic Subregion",
            flagEmoji = flag,
            currency = "USD",
            languages = listOf("English", "Local Language"),
            area = 120000.0,
            funFact = "$capped is a stunning sovereign nation known for rich heritage, tourist spots, and historic architecture."
        )
    }

    // --- NEWS SERVICE (NewsAPI with Offline Cache & Fallback) ---
    fun getNews(category: String): Flow<List<NewsData>> = flow {
        val cacheId = "NEWS_API:${category.lowercase(Locale.ROOT)}"
        val cached = cachedDataDao.getCacheById(cacheId)
        val apiKey = BuildConfig.NEWS_API_KEY
        
        // If unexpired cache exists, return it immediately without network request
        if (cached != null && !cacheInvalidationService.isCacheStale("NEWS", cached.timestamp)) {
            val decoded = fromJsonList(cached.content, NewsData::class.java)
            if (decoded != null) {
                emit(decoded)
                return@flow
            }
        }

        if (apiKey.isEmpty() || apiKey == "YOUR_NEWS_API_KEY") {
            // Fallback to RSS/Cached if API key is not present or is the default placeholder
            if (cached != null) {
                val decoded = fromJsonList(cached.content, NewsData::class.java)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            emit(getLocalNewsFallback(category))
            return@flow
        }

        try {
            // Map category parameter to NewsAPI supported category or search query
            val response = if (category.lowercase(Locale.ROOT) in listOf("all", "india", "pib", "pib news", "times of india")) {
                RetrofitClient.newsApi.getTopHeadlines(country = "us", category = "general", apiKey = apiKey)
            } else if (category.lowercase(Locale.ROOT) in listOf("tech", "science", "business")) {
                RetrofitClient.newsApi.getTopHeadlines(country = "us", category = category.lowercase(Locale.ROOT), apiKey = apiKey)
            } else {
                RetrofitClient.newsApi.searchNews(query = category, apiKey = apiKey)
            }
            
            val articles = response.articles
            if (articles != null && articles.isNotEmpty()) {
                val list = articles.mapIndexed { idx, article ->
                    val imageSeed = (article.url ?: "").hashCode().toString()
                    NewsData(
                        id = article.url ?: "url_$idx",
                        title = article.title ?: "No Title",
                        summary = article.description ?: "No description available.",
                        source = article.source?.name ?: "News Feed",
                        category = category,
                        timeAgo = article.publishedAt ?: "Recent",
                        imageUrl = article.urlToImage ?: "https://picsum.photos/seed/$imageSeed/400/250"
                    )
                }
                cachedDataDao.insertCache(CachedData(cacheId, "NEWS", category, toJsonList(list, NewsData::class.java)))
                emit(list)
            } else {
                throw Exception("Empty news results from NewsAPI")
            }
        } catch (e: Exception) {
            if (cached != null) {
                val decoded = fromJsonList(cached.content, NewsData::class.java)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            emit(getLocalNewsFallback(category))
        }
    }

    private fun parseGoogleNewsRss(xml: String, category: String): List<NewsData> {
        val newsList = mutableListOf<NewsData>()
        try {
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(java.io.StringReader(xml))
            
            var eventType = xpp.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentPubDate = ""
            var currentSource = ""
            var currentDescription = ""
            var currentImageUrl = ""
            var insideItem = false
            var currentTag = ""
            
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    org.xmlpull.v1.XmlPullParser.START_TAG -> {
                        currentTag = xpp.name
                        if (currentTag == "item") {
                            insideItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentPubDate = ""
                            currentSource = ""
                            currentDescription = ""
                            currentImageUrl = ""
                        } else if (insideItem && currentTag == "enclosure") {
                            val urlVal = xpp.getAttributeValue(null, "url")
                            if (!urlVal.isNullOrEmpty()) {
                                currentImageUrl = urlVal
                            }
                        }
                    }
                    org.xmlpull.v1.XmlPullParser.TEXT -> {
                        val text = xpp.text.trim()
                        if (insideItem && text.isNotEmpty()) {
                            when (currentTag) {
                                "title" -> currentTitle += text
                                "link" -> currentLink += text
                                "pubDate" -> currentPubDate += text
                                "source" -> currentSource += text
                                "description" -> currentDescription += text
                            }
                        }
                    }
                    org.xmlpull.v1.XmlPullParser.END_TAG -> {
                        if (xpp.name == "item") {
                            insideItem = false
                            if (currentTitle.isNotEmpty()) {
                                val id = currentLink.trim()
                                
                                // Clean title and try to extract source if it's formatted as "Title - Source"
                                val lastDashIndex = currentTitle.lastIndexOf(" - ")
                                var (cleanTitle, parsedSource) = if (lastDashIndex != -1) {
                                    Pair(
                                        currentTitle.substring(0, lastDashIndex).trim(),
                                        currentTitle.substring(lastDashIndex + 3).trim()
                                    )
                                } else {
                                    Pair(currentTitle.trim(), currentSource.trim())
                                }
                                
                                // Clean up the source
                                if (parsedSource.isEmpty()) {
                                    parsedSource = if (id.contains("pib.gov.in")) {
                                        "Press Information Bureau"
                                    } else if (id.contains("timesofindia")) {
                                        "Times of India"
                                    } else {
                                        "Smart News"
                                    }
                                }

                                // Handle CDATA & HTML inside description
                                val cleanDesc = currentDescription.replace(Regex("<[^>]*>"), "").trim()
                                // If the description contains an image tag inside html, extract image url if empty
                                if (currentImageUrl.isEmpty()) {
                                    val srcIndex = currentDescription.indexOf("src=\"")
                                    if (srcIndex != -1) {
                                        val start = srcIndex + 5
                                        val end = currentDescription.indexOf("\"", start)
                                        if (end != -1) {
                                            currentImageUrl = currentDescription.substring(start, end)
                                        }
                                    }
                                }
                                
                                val imageSeed = id.hashCode().toString()
                                val imageUrl = if (currentImageUrl.isNotEmpty()) {
                                    currentImageUrl
                                } else {
                                    "https://picsum.photos/seed/${imageSeed}/400/250"
                                }
                                
                                val summaryText = if (cleanDesc.isNotEmpty()) {
                                    cleanDesc
                                } else {
                                    "Read the complete, detailed press report on $parsedSource."
                                }

                                newsList.add(
                                    NewsData(
                                        id = id,
                                        title = cleanTitle,
                                        summary = summaryText,
                                        source = parsedSource,
                                        category = category,
                                        timeAgo = if (currentPubDate.length > 16) currentPubDate.substring(0, 16) else if (currentPubDate.isNotEmpty()) currentPubDate else "Recent",
                                        imageUrl = imageUrl
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = xpp.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return newsList
    }

    private fun getLocalNewsFallback(category: String): List<NewsData> {
        return listOf(
            NewsData("1", "Mars Explorer Rover Uncovers Ancient Water Traces", "Latest physical samples collected from Mars surface show trace mineral deposits pointing to flowing rivers.", "Space Discovery", category, "2h ago", "https://picsum.photos/seed/mars/400/250"),
            NewsData("2", "Global Clean Energy Grid Hits Record High Performance", "Sustainable electrical installations around Europe and India reached a brand-new peak contribution of 42%.", "Eco Watch", category, "4h ago", "https://picsum.photos/seed/green/400/250"),
            NewsData("3", "Next-Gen Quantum Micro-Processors Announced", "Global tech consortia have unveiled desktop-grade solid-state chips designed to cut quantum computing cooling budgets.", "Tech Pulse", category, "6h ago", "https://picsum.photos/seed/quantum/400/250")
        )
    }

    // --- JOKES SERVICE (Official Joke API Offline Cache & Fallback) ---
    fun getRandomJoke(category: String): Flow<JokeData> = flow {
        val cacheId = "JOKE:${category.lowercase(Locale.ROOT)}"
        try {
            val response = RetrofitClient.officialJokeApi.getRandomJoke()
            val joke = JokeData(
                id = (response.id ?: Random.nextInt()).toString(),
                setup = response.setup ?: "",
                punchline = response.punchline ?: "",
                category = "General"
            )
            cachedDataDao.insertCache(CachedData(cacheId, "JOKE", category, toJson(joke)))
            emit(joke)
        } catch (e: Exception) {
            val cached = cachedDataDao.getCacheById(cacheId)
            if (cached != null) {
                val decoded = fromJson<JokeData>(cached.content)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            // Fallback list of jokes
            val jokes = listOf(
                JokeData("j1", "Why don't scientists trust atoms?", "Because they make up everything!", "Science"),
                JokeData("j2", "What do you call a fake noodle?", "An impasta!", "General"),
                JokeData("j3", "Why did the scarecrow win an award?", "Because he was outstanding in his field!", "General"),
                JokeData("j4", "There are 10 types of people in the world.", "Those who understand binary, and those who don't.", "Programming")
            )
            val filtered = if (category.lowercase() == "all") jokes else jokes.filter { it.category.lowercase() == category.lowercase() }
            emit(filtered.randomOrNull() ?: jokes.first())
        }
    }

    // --- CURRENCY CONVERTER (Frankfurter Offline Cache & Fallback) ---
    fun getCurrencyRates(): Flow<List<CurrencyRate>> = flow {
        val cacheId = "CURRENCY_RATES"
        try {
            val response = RetrofitClient.currencyApi.getRates(from = "USD")
            val ratesMap = response.rates ?: emptyMap()
            
            val rateList = mutableListOf<CurrencyRate>()
            rateList.add(CurrencyRate("USD", "US Dollar", "🇺🇸", 1.0))
            
            val currencyNames = mapOf(
                "EUR" to Pair("Euro", "🇪🇺"),
                "GBP" to Pair("British Pound", "🇬🇧"),
                "JPY" to Pair("Japanese Yen", "🇯🇵"),
                "INR" to Pair("Indian Rupee", "🇮🇳"),
                "CAD" to Pair("Canadian Dollar", "🇨🇦"),
                "AUD" to Pair("Australian Dollar", "🇦🇺"),
                "BRL" to Pair("Brazilian Real", "🇧🇷"),
                "CHF" to Pair("Swiss Franc", "🇨🇭"),
                "CNY" to Pair("Chinese Yuan", "🇨🇳")
            )
            
            ratesMap.forEach { (code, value) ->
                val meta = currencyNames[code] ?: Pair("Foreign Currency", "🌐")
                rateList.add(CurrencyRate(code, meta.first, meta.second, value))
            }
            
            if (rateList.size > 1) {
                cachedDataDao.insertCache(CachedData(cacheId, "CURRENCY", "ALL", toJsonList(rateList, CurrencyRate::class.java)))
                emit(rateList)
            } else {
                throw Exception("Failed to map currency rates")
            }
        } catch (e: Exception) {
            val cached = cachedDataDao.getCacheById(cacheId)
            if (cached != null) {
                val decoded = fromJsonList(cached.content, CurrencyRate::class.java)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            // Standard currency fallback rates
            emit(listOf(
                CurrencyRate("USD", "US Dollar", "🇺🇸", 1.0),
                CurrencyRate("EUR", "Euro", "🇪🇺", 0.92),
                CurrencyRate("GBP", "British Pound", "🇬🇧", 0.78),
                CurrencyRate("JPY", "Japanese Yen", "🇯🇵", 158.5),
                CurrencyRate("INR", "Indian Rupee", "🇮🇳", 83.5),
                CurrencyRate("CAD", "Canadian Dollar", "🇨🇦", 1.37),
                CurrencyRate("AUD", "Australian Dollar", "🇦🇺", 1.51),
                CurrencyRate("BRL", "Brazilian Real", "🇧🇷", 5.43)
            ))
        }
    }

    // --- TRANSLATOR (Gemini API with Offline Cache & Fallback) ---
    fun translateText(text: String, from: String, to: String): Flow<TranslationResult> = flow {
        val cacheId = "TRANSLATE_GEMINI:${from}_to_${to}:${text.trim().lowercase(Locale.ROOT)}"
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback mock mapper or offline cache if API key is missing
            val cached = cachedDataDao.getCacheById(cacheId)
            if (cached != null) {
                val decoded = fromJson<TranslationResult>(cached.content)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            val translations = mapOf(
                "hello" to mapOf("Spanish" to "Hola", "French" to "Bonjour", "German" to "Hallo", "Japanese" to "こんにちは", "Hindi" to "नमस्ते"),
                "thank you" to mapOf("Spanish" to "Gracias", "French" to "Merci", "German" to "Danke", "Japanese" to "ありがとう", "Hindi" to "धन्यवाद"),
                "good morning" to mapOf("Spanish" to "Buenos días", "French" to "Bonjour", "German" to "Guten Morgen", "Japanese" to "おはようございます", "Hindi" to "शुभ प्रभात")
            )
            val clean = text.trim().lowercase()
            val match = translations[clean]?.get(to) ?: "[$to: $text]"
            emit(TranslationResult(text, match, from, to))
            return@flow
        }

        try {
            val prompt = "Translate the following text from $from to $to. Provide only the direct translation, with absolutely no surrounding conversational text, explanations, or quotes.\n\nText: $text"
            
            val jsonRequestBody = """
                {
                    "contents": [{
                        "parts": [{
                            "text": ${toJsonString(prompt)}
                        }]
                    }]
                }
            """.trimIndent()

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = jsonRequestBody.toRequestBody(mediaType)
            
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
                
            val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw java.io.IOException("Unexpected HTTP code: ${response.code}")
                    response.body?.string() ?: ""
                }
            }
            
            val translated = parseGeminiResponseText(responseText) ?: throw Exception("Empty translation from Gemini")
            
            val result = TranslationResult(text, translated, from, to)
            cachedDataDao.insertCache(CachedData(cacheId, "TRANSLATOR", text, toJson(result)))
            emit(result)
        } catch (e: Exception) {
            val cached = cachedDataDao.getCacheById(cacheId)
            if (cached != null) {
                val decoded = fromJson<TranslationResult>(cached.content)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            // Fallback mock mapper
            val translations = mapOf(
                "hello" to mapOf("Spanish" to "Hola", "French" to "Bonjour", "German" to "Hallo", "Japanese" to "こんにちは", "Hindi" to "नमस्ते"),
                "thank you" to mapOf("Spanish" to "Gracias", "French" to "Merci", "German" to "Danke", "Japanese" to "ありがとう", "Hindi" to "धन्यवाद"),
                "good morning" to mapOf("Spanish" to "Buenos días", "French" to "Bonjour", "German" to "Guten Morgen", "Japanese" to "おはようございます", "Hindi" to "शुभ प्रभात")
            )
            val clean = text.trim().lowercase()
            val match = translations[clean]?.get(to) ?: "[$to: $text]"
            emit(TranslationResult(text, match, from, to))
        }
    }

    private fun mapLanguageCode(lang: String): String {
        return when (lang.lowercase()) {
            "spanish" -> "es"
            "french" -> "fr"
            "german" -> "de"
            "japanese" -> "ja"
            "hindi" -> "hi"
            "chinese" -> "zh"
            else -> "en"
        }
    }

    // --- DICTIONARY (Free Dictionary API Offline Cache & Fallback) ---
    fun getDictionaryWord(word: String): Flow<DictionaryWord?> = flow {
        val cacheId = "DICTIONARY:${word.trim().lowercase(Locale.ROOT)}"
        try {
            val responses = RetrofitClient.dictionaryApi.getWordDefinition(word)
            val response = responses.firstOrNull()
            if (response != null) {
                val meaning = response.meanings?.firstOrNull()
                val definition = meaning?.definitions?.firstOrNull()
                
                val result = DictionaryWord(
                    word = response.word ?: word,
                    phonetic = response.phonetic ?: response.phonetics?.firstOrNull { !it.text.isNullOrEmpty() }?.text ?: "",
                    partOfSpeech = meaning?.partOfSpeech ?: "noun",
                    definition = definition?.definition ?: "Definition unavailable.",
                    example = definition?.example ?: "No examples found.",
                    synonyms = meaning?.synonyms ?: emptyList()
                )
                
                cachedDataDao.insertCache(CachedData(cacheId, "DICTIONARY", word, toJson(result)))
                emit(result)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            val cached = cachedDataDao.getCacheById(cacheId)
            if (cached != null) {
                val decoded = fromJson<DictionaryWord>(cached.content)
                if (decoded != null) {
                    emit(decoded)
                    return@flow
                }
            }
            emit(getLocalDictionaryFallback(word))
        }
    }

    private fun getLocalDictionaryFallback(word: String): DictionaryWord? {
        if (word.isBlank()) return null
        val clean = word.trim().lowercase()
        val staticDict = mapOf(
            "explorer" to DictionaryWord("explorer", "/ɪkˈsplɔːrər/", "noun", "A person who explores a new or unfamiliar area.", "The brave explorer hacked through the dense Martian jungle.", listOf("adventurer", "scout", "pioneer")),
            "curiosity" to DictionaryWord("curiosity", "/ˌkjʊəriˈɒsəti/", "noun", "A strong desire to know or learn something.", "Her dynamic curiosity led her to uncover ancient ruins.", listOf("inquisitiveness", "interest", "wonder"))
        )
        return staticDict[clean] ?: DictionaryWord(
            word = word,
            phonetic = "/${word.lowercase()}/",
            partOfSpeech = "noun / verb",
            definition = "A searched term queried in the Smart Explorer Dictionary widget.",
            example = "He found the definition of '$word' using the dictionary offline search capability.",
            synonyms = listOf("expression", "vocabulary", "phrase")
        )
    }

    // --- VAULT NOTES ---
    suspend fun saveNote(id: String, title: String, content: String) {
        cachedDataDao.insertCache(CachedData(id = "NOTE:$id", type = "NOTES", key = title, content = content))
    }

    suspend fun getAllNotes(): List<CachedData> {
        return cachedDataDao.getCacheByType("NOTES")
    }

    suspend fun deleteNoteById(id: String) {
        cachedDataDao.deleteCacheById("NOTE:$id")
    }

    // --- GEMINI TRANSLATION HELPER UTILS ---
    private fun toJsonString(raw: String): String {
        return moshi.adapter(String::class.java).toJson(raw)
    }

    private fun parseGeminiResponseText(responseText: String): String? {
        return try {
            val adapter = moshi.adapter(GeminiResponse::class.java)
            val response = adapter.fromJson(responseText)
            response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
        } catch (e: Exception) {
            null
        }
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class GeminiPart(val text: String?)

@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class GeminiContent(val parts: List<GeminiPart>?)

@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class GeminiCandidate(val content: GeminiContent?)

@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class GeminiResponse(val candidates: List<GeminiCandidate>?)
