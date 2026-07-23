package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.ExplorerRepository
import com.example.data.repository.ReferralRepository
import com.example.data.network.NetworkMonitor
import com.example.data.network.LiveNetworkMonitor

class AppContainer(private val context: Context) {
    val networkMonitor: NetworkMonitor by lazy {
        LiveNetworkMonitor(context)
    }

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    
    val userPreferencesManager: com.example.data.local.UserPreferencesManager by lazy {
        com.example.data.local.UserPreferencesManager(context)
    }

    val analyticsHelper: com.example.data.analytics.AnalyticsHelper by lazy {
        com.example.data.analytics.AnalyticsHelper.getInstance(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(userPreferencesManager)
    }
    
    val cacheInvalidationService: com.example.data.local.CacheInvalidationService by lazy {
        com.example.data.local.CacheInvalidationService(database.cachedDataDao())
    }

    val greetingService: GreetingService by lazy {
        GreetingService()
    }
    
    val explorerRepository: ExplorerRepository by lazy {
        ExplorerRepository(
            database.favoriteDao(),
            database.cachedDataDao(),
            cacheInvalidationService,
            database.feedbackDao(),
            userPreferencesManager,
            database.userAchievementDao(),
            database.userInteractionDao()
        )
    }

    val referralRepository: ReferralRepository by lazy {
        ReferralRepository(
            database.referralDao(),
            authRepository,
            context
        )
    }

    val updateRepository: com.example.data.repository.UpdateRepository by lazy {
        com.example.data.repository.UpdateRepository(context)
    }
}
