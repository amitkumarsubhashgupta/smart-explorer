package com.example

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.SmartExplorerNavGraph
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ExplorerViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.components.AdUnits
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : FragmentActivity() {

    private val TAG = "MainActivity"
    private var appOpenAd: AppOpenAd? = null
    private var isAdShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep-link referral codes
        handleIncomingReferral(intent)

        // Access the central service locator container
        val appContainer = (application as SmartExplorerApplication).container

        // Instantiate ViewModels using our dependency provider factories
        val authViewModel: AuthViewModel by viewModels {
            AuthViewModel.provideFactory(appContainer.authRepository, appContainer.analyticsHelper)
        }
        val explorerViewModel: ExplorerViewModel by viewModels {
            ExplorerViewModel.provideFactory(
                appContainer.explorerRepository,
                appContainer.analyticsHelper,
                appContainer.greetingService,
                appContainer.updateRepository,
                appContainer.networkMonitor
            )
        }

        // Initialize Google UMP Consent SDK for GDPR/EEA compliance
        requestUserConsent()

        setContent {
            val themePreference by explorerViewModel.themeState.collectAsState()
            val accentColorPreference by explorerViewModel.accentColorState.collectAsState()
            val isDarkTheme = when (themePreference) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            val updateState by explorerViewModel.updateState.collectAsState()
            val isDismissed by explorerViewModel.isUpdateDismissed.collectAsState()
            val isOnline by explorerViewModel.isOnline.collectAsState()
            val appLanguage by explorerViewModel.appLanguageState.collectAsState()

            val appStrings = when (appLanguage) {
                "Spanish" -> com.example.ui.components.SpanishStrings
                "French" -> com.example.ui.components.FrenchStrings
                "German" -> com.example.ui.components.GermanStrings
                "Japanese" -> com.example.ui.components.JapaneseStrings
                "Hindi" -> com.example.ui.components.HindiStrings
                else -> com.example.ui.components.EnglishStrings
            }

            MyApplicationTheme(darkTheme = isDarkTheme, accentColor = accentColorPreference) {
                androidx.compose.runtime.CompositionLocalProvider(com.example.ui.components.LocalAppStrings provides appStrings) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SmartExplorerNavGraph(
                                authViewModel = authViewModel,
                                explorerViewModel = explorerViewModel
                            )

                            // Global Realtime Connection Status Banner Overlay
                            AnimatedVisibility(
                                visible = !isOnline,
                                enter = slideInVertically(initialOffsetY = { -it }),
                                exit = slideOutVertically(targetOffsetY = { -it }),
                                modifier = Modifier.align(Alignment.TopCenter).zIndex(99f)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .statusBarsPadding(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WifiOff,
                                            contentDescription = "Offline Mode Indicator",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = com.example.ui.components.AppStrings.current.offlineMode,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = com.example.ui.components.AppStrings.current.offlineWarning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }


                    }
                }
            }
        }
    }
}

    /**
     * Integrates UMP Consent Form for EEA/GDPR compliance.
     */
    private fun requestUserConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeAdMob()
                    }
                }
            },
            { requestError ->
                Log.w(TAG, "Consent request update failed: ${requestError.message}")
                // Fallback to initialize AdMob on error
                initializeAdMob()
            }
        )
    }

    private fun initializeAdMob() {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {
            Log.d(TAG, "MobileAds SDK initialized.")
            loadAppOpenAd()
        }
    }

    /**
     * Loads the App Open Ad.
     */
    private fun loadAppOpenAd() {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            AdUnits.APP_OPEN_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d(TAG, "App Open Ad loaded successfully.")
                    showAppOpenAdIfAvailable()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Shows the loaded App Open Ad if available.
     */
    private fun showAppOpenAdIfAvailable() {
        val ad = appOpenAd
        if (ad != null && !isAdShowing) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isAdShowing = false
                    Log.d(TAG, "App Open Ad dismissed.")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isAdShowing = false
                    Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    isAdShowing = true
                    Log.d(TAG, "App Open Ad showing.")
                }
            }
            ad.show(this)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingReferral(intent)
    }

    private fun handleIncomingReferral(intent: android.content.Intent?) {
        val uri = intent?.data
        Log.d(TAG, "Incoming deep link URI: $uri")
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                val cleanCode = code.trim().uppercase(java.util.Locale.ROOT)
                val prefs = getSharedPreferences("referral_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("pending_referral_code", cleanCode).apply()
                Log.d(TAG, "Captured pending referral code from deep link: $cleanCode")
                
                android.widget.Toast.makeText(
                    this,
                    "Referral Code '$cleanCode' detected! Points will be credited upon login.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
