package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserInteractionDao {
    @Query("SELECT * FROM user_interactions")
    suspend fun getAllInteractions(): List<UserInteraction>

    @Query("SELECT * FROM user_interactions WHERE featureId = :featureId")
    suspend fun getInteractionByFeature(featureId: String): UserInteraction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: UserInteraction)

    @Query("UPDATE user_interactions SET useCount = useCount + 1, lastTimestamp = :timestamp WHERE featureId = :featureId")
    suspend fun incrementUseCount(featureId: String, timestamp: Long)
}
