package com.example.data.local

import android.util.Log

class CacheInvalidationService(private val cachedDataDao: CachedDataDao) {

    companion object {
        private const val TAG = "CacheInvalidation"
        
        // TTL policy definitions (in milliseconds)
        const val WEATHER_TTL = 30 * 60 * 1000L // 30 minutes
        const val NEWS_TTL = 60 * 60 * 1000L    // 1 hour
    }

    /**
     * Clears weather and news cache entries that exceed their TTL.
     * Returns the number of records cleared.
     */
    suspend fun invalidateStaleData(): Int {
        val currentTime = System.currentTimeMillis()
        Log.d(TAG, "Running manual cache invalidation at $currentTime")
        val deletedCount = cachedDataDao.deleteStaleCache(currentTime, WEATHER_TTL, NEWS_TTL)
        Log.d(TAG, "Cleared $deletedCount stale cache entries")
        return deletedCount
    }

    /**
     * Counts how many weather and news cache entries are currently stale.
     */
    suspend fun countStaleData(): Int {
        val currentTime = System.currentTimeMillis()
        return cachedDataDao.countStaleCache(currentTime, WEATHER_TTL, NEWS_TTL)
    }

    /**
     * Checks if a cached item is stale.
     */
    fun isCacheStale(type: String, timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return when (type) {
            "WEATHER" -> age > WEATHER_TTL
            "NEWS" -> age > NEWS_TTL
            else -> false
        }
    }
}
