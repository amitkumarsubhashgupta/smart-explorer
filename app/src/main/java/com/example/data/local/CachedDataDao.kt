package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedDataDao {
    @Query("SELECT * FROM cached_data WHERE id = :id")
    suspend fun getCacheById(id: String): CachedData?

    @Query("SELECT * FROM cached_data WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getCacheByType(type: String): List<CachedData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CachedData)

    @Query("DELETE FROM cached_data WHERE id = :id")
    suspend fun deleteCacheById(id: String)

    @Query("DELETE FROM cached_data WHERE (type = 'WEATHER' AND :currentTime - timestamp > :weatherTtl) OR (type = 'NEWS' AND :currentTime - timestamp > :newsTtl)")
    suspend fun deleteStaleCache(currentTime: Long, weatherTtl: Long, newsTtl: Long): Int

    @Query("SELECT COUNT(*) FROM cached_data WHERE (type = 'WEATHER' AND :currentTime - timestamp > :weatherTtl) OR (type = 'NEWS' AND :currentTime - timestamp > :newsTtl)")
    suspend fun countStaleCache(currentTime: Long, weatherTtl: Long, newsTtl: Long): Int

    @Query("DELETE FROM cached_data WHERE type = :type")
    suspend fun clearCacheByType(type: String)

    @Query("DELETE FROM cached_data")
    suspend fun clearAllCache()
}
