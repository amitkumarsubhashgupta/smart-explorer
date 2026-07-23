package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "referral_user_profiles")
data class ReferralUserProfile(
    @PrimaryKey val userId: String,
    val referralCode: String,
    val points: Int = 0,
    val redeemedCode: String? = null // To prevent duplicate redemptions / self-referrals
)

@Entity(tableName = "referral_history")
data class ReferralHistoryItem(
    @PrimaryKey val id: String,
    val userId: String, // Owner of this history item
    val friendName: String,
    val joinDate: Long,
    val status: String, // "Successful" or "Pending"
    val pointsEarned: Int
)

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referral_user_profiles WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<ReferralUserProfile?>

    @Query("SELECT * FROM referral_user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): ReferralUserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: ReferralUserProfile)

    @Query("SELECT * FROM referral_history WHERE userId = :userId ORDER BY joinDate DESC")
    fun getHistoryFlow(userId: String): Flow<List<ReferralHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: ReferralHistoryItem)

    @Query("DELETE FROM referral_user_profiles")
    suspend fun clearProfiles()

    @Query("DELETE FROM referral_history")
    suspend fun clearHistory()
}
