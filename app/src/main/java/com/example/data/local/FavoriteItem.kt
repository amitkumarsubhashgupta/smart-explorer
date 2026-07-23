package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_items")
data class FavoriteItem(
    @PrimaryKey val id: String, // format: "type:key" (e.g. "WEATHER:Paris", "JOKE:1")
    val type: String,          // e.g. "WEATHER", "COUNTRY", "NEWS", "JOKE", "CURRENCY", "TRANSLATOR", "DICTIONARY"
    val title: String,
    val subtitle: String,
    val content: String,       // JSON or details representing the item
    val timestamp: Long = System.currentTimeMillis()
)
