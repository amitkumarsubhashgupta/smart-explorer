package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_data")
data class CachedData(
    @PrimaryKey val id: String, // format: "type:key" (e.g. "WEATHER:London")
    val type: String,          // e.g. "WEATHER", "COUNTRY", "NEWS", "JOKE", "CURRENCY", "TRANSLATOR", "DICTIONARY"
    val key: String,           // query term
    val content: String,       // Serialized JSON of API response
    val timestamp: Long = System.currentTimeMillis()
)
