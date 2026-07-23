package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherData(
    val city: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    val feelsLike: Double,
    val uvIndex: Int,
    val forecast: List<ForecastDay>
)

@JsonClass(generateAdapter = true)
data class ForecastDay(
    val day: String,
    val temperature: Double,
    val condition: String
)

@JsonClass(generateAdapter = true)
data class CountryData(
    val name: String,
    val officialName: String,
    val capital: String,
    val population: Long,
    val region: String,
    val subregion: String,
    val flagEmoji: String,
    val currency: String,
    val languages: List<String>,
    val area: Double,
    val funFact: String
)

@JsonClass(generateAdapter = true)
data class NewsData(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val category: String,
    val timeAgo: String,
    val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class JokeData(
    val id: String,
    val setup: String,
    val punchline: String,
    val category: String = "General"
)

@JsonClass(generateAdapter = true)
data class CurrencyRate(
    val code: String,
    val name: String,
    val flag: String,
    val rateToUSD: Double
)

@JsonClass(generateAdapter = true)
data class DictionaryWord(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val synonyms: List<String>
)

@JsonClass(generateAdapter = true)
data class TranslationResult(
    val sourceText: String,
    val targetText: String,
    val sourceLanguage: String,
    val targetLanguage: String
)

@JsonClass(generateAdapter = true)
data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val actionLabel: String,
    val featureId: String
)
