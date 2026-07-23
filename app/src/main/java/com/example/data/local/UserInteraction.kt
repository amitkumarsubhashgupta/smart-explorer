package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_interactions")
data class UserInteraction(
    @PrimaryKey val featureId: String, // e.g. "weather", "translator", "news", "countries", "dictionary"
    val useCount: Int,
    val lastTimestamp: Long = System.currentTimeMillis()
)
