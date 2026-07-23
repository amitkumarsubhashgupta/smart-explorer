package com.example

import android.app.Application
import com.example.data.AppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SmartExplorerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        
        // Programmatically initialize Firebase with the provided credentials
        try {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyAh5F2-WstNCqoibt8PBCcIVc3MbnTVx40")
                .setApplicationId("1:332710921765:web:9b40619da60e053f5561cc")
                .setProjectId("smart-explorer-24268")
                .setDatabaseUrl("https://smart-explorer-24268-default-rtdb.asia-southeast1.firebasedatabase.app")
                .setStorageBucket("smart-explorer-24268.firebasestorage.app")
                .setGcmSenderId("332710921765")
                .build()

            FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        container = AppContainer(this)
    }
}
