package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAchievementDao {
    @Query("SELECT * FROM user_achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<UserAchievement>>

    @Query("SELECT * FROM user_achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): UserAchievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: UserAchievement)

    @Query("UPDATE user_achievements SET isEarned = 1, earnedTimestamp = :timestamp WHERE id = :id")
    suspend fun markAsEarned(id: String, timestamp: Long)
}
