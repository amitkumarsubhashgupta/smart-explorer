package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteItem::class, CachedData::class, FeedbackItem::class, ReferralUserProfile::class, ReferralHistoryItem::class, UserAchievement::class, UserInteraction::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cachedDataDao(): CachedDataDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun referralDao(): ReferralDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun userInteractionDao(): UserInteractionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_explorer_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
