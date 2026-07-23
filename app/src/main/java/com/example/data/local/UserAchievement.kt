package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey val id: String, // e.g. "first_search", "favorite_saved", "profile_completed", "visited_5_countries"
    val title: String,
    val description: String,
    val isEarned: Boolean,
    val earnedTimestamp: Long? = null
)
