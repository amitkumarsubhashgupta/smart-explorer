package com.example.data.network

import retrofit2.http.*

// ==========================================
// 1. WEATHER API (OpenWeatherMap)
// ==========================================
interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}

data class WeatherResponse(
    val name: String?,
    val main: MainData?,
    val weather: List<WeatherDescription>?,
    val wind: WindData?,
    val sys: SysData?
)

data class MainData(
    val temp: Double?,
    val feels_like: Double?,
    val humidity: Int?,
    val pressure: Int?
)

data class WeatherDescription(
    val main: String?,
    val description: String?
)

data class WindData(
    val speed: Double?
)

data class SysData(
    val sunrise: Long?,
    val sunset: Long?
)

data class ForecastResponse(
    val list: List<ForecastItem>?
)

data class ForecastItem(
    val dt: Long?,
    val main: MainData?,
    val weather: List<WeatherDescription>?,
    val dt_txt: String?
)


// ==========================================
// 2. COUNTRIES API (REST Countries)
// ==========================================
interface CountriesApi {
    @GET("name/{name}")
    suspend fun getCountryByName(
        @Path("name") name: String
    ): List<RestCountryResponse>
}

data class RestCountryResponse(
    val name: CountryName?,
    val capitals: List<String>?, // Some API versions have capital
    val capital: List<String>?,
    val population: Long?,
    val region: String?,
    val subregion: String?,
    val flag: String?, // Flag emoji sometimes
    val flags: FlagUrl?,
    val currencies: Map<String, RestCurrency>?,
    val languages: Map<String, String>?,
    val area: Double?,
    val maps: MapsUrl?,
    val timezones: List<String>?
)

data class CountryName(
    val common: String?,
    val official: String?
)

data class FlagUrl(
    val png: String?,
    val svg: String?
)

data class RestCurrency(
    val name: String?,
    val symbol: String?
)

data class MapsUrl(
    val googleMaps: String?
)


// ==========================================
// 3. NEWS API (NewsAPI)
// ==========================================
interface NewsApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "in",
        @Query("category") category: String?,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}

data class NewsResponse(
    val articles: List<NewsArticle>?
)

data class NewsArticle(
    val source: NewsSource?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

data class NewsSource(
    val name: String?
)


// ==========================================
// 4. JOKE API (JokeAPI)
// ==========================================
interface JokeApi {
    @GET("joke/{category}")
    suspend fun getJoke(
        @Path("category") category: String,
        @Query("safe-mode") safeMode: Boolean = true
    ): RestJokeResponse
}

data class RestJokeResponse(
    val id: Int?,
    val category: String?,
    val type: String?, // "single" or "twopart"
    val joke: String?,
    val setup: String?,
    val delivery: String?
)


// ==========================================
// 5. CURRENCY API (Frankfurter)
// ==========================================
interface CurrencyApi {
    @GET("latest")
    suspend fun getRates(
        @Query("from") from: String = "USD"
    ): FrankfurterResponse
}

data class FrankfurterResponse(
    val base: String?,
    val rates: Map<String, Double>?
)


// ==========================================
// 6. LIBRETRANSLATE API
// ==========================================
interface TranslateApi {
    @POST("translate")
    @FormUrlEncoded
    suspend fun translate(
        @Field("q") text: String,
        @Field("source") source: String,
        @Field("target") target: String,
        @Field("format") format: String = "text"
    ): LibreTranslateResponse
}

data class LibreTranslateResponse(
    val translatedText: String?
)


// ==========================================
// 7. FREE DICTIONARY API
// ==========================================
interface DictionaryApi {
    @GET("entries/en/{word}")
    suspend fun getWordDefinition(
        @Path("word") word: String
    ): List<RestDictionaryResponse>
}

data class RestDictionaryResponse(
    val word: String?,
    val phonetic: String?,
    val phonetics: List<PhoneticData>?,
    val meanings: List<MeaningData>?
)

data class PhoneticData(
    val text: String?,
    val audio: String?
)

data class MeaningData(
    val partOfSpeech: String?,
    val definitions: List<DefinitionData>?,
    val synonyms: List<String>?,
    val antonyms: List<String>?
)

data class DefinitionData(
    val definition: String?,
    val example: String?
)

// ==========================================
// 8. OFFICIAL ENGLISH JOKE API
// ==========================================
interface OfficialJokeApi {
    @GET("jokes/random")
    suspend fun getRandomJoke(): OfficialJokeResponse
}

data class OfficialJokeResponse(
    val id: Int?,
    val type: String?,
    val setup: String?,
    val punchline: String?
)

