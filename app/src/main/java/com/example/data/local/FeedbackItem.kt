package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "feedback_items")
data class FeedbackItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val userName: String,
    val email: String,
    val profilePhoto: String,
    val rating: Int, // 1 to 5 stars
    val category: String, // "Bug Report", "Feature Request", "Performance", "UI/UX", "General Feedback", "Other"
    val message: String,
    val screenshotUrl: String = "",
    val appVersion: String = "1.0.0",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val loginProvider: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Pending" // "Pending", "Reviewed", "Resolved"
)

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM feedback_items ORDER BY createdAt DESC")
    fun getAllFeedback(): Flow<List<FeedbackItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackItem)

    @Query("UPDATE feedback_items SET status = :status WHERE id = :id")
    suspend fun updateFeedbackStatus(id: Int, status: String)

    @Query("DELETE FROM feedback_items WHERE id = :id")
    suspend fun deleteFeedbackById(id: Int)

    @Query("DELETE FROM feedback_items")
    suspend fun clearAllFeedback()
}
