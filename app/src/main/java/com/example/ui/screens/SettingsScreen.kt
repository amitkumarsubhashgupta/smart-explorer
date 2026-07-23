package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.components.BannerAd
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SectionHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: com.example.ui.viewmodel.ExplorerViewModel,
    authViewModel: com.example.ui.viewmodel.AuthViewModel,
    onBack: () -> Unit,
    onNavigateToAboutUs: () -> Unit,
    onNavigateToContactUs: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsConditions: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Access the database directly for cache flushing
    val database = remember {
        (context.applicationContext as com.example.SmartExplorerApplication).container.database
    }

    val cacheInvalidationService = remember {
        (context.applicationContext as com.example.SmartExplorerApplication).container.cacheInvalidationService
    }

    var staleCacheCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        staleCacheCount = cacheInvalidationService.countStaleData()
        viewModel.checkForUpdates(silent = true)
    }

    // Dynamic preferences states
    val currentThemePreference by viewModel.themeState.collectAsState()
    val defaultTargetLangPreference by viewModel.translatorLangState.collectAsState()
    val weatherUnitPreference by viewModel.weatherUnitState.collectAsState()
    val isNotificationsEnabled by viewModel.pushNotificationsState.collectAsState()
    val isOfflineModeEnabled by viewModel.offlineModeState.collectAsState()
    val isPrivacyModeEnabled by viewModel.privacyModeState.collectAsState()
    val cacheSettingsPreference by viewModel.cacheSettingsState.collectAsState()
    val isAiSuggestionsEnabled by viewModel.aiSuggestionsState.collectAsState()
    val isAnalyticsEnabled by viewModel.analyticsState.collectAsState()
    val newsFilterPreference by viewModel.newsFilterState.collectAsState()

    val updateState by viewModel.updateState.collectAsState()
    var showDeveloperSimulation by remember { mutableStateOf(false) }

    var isSafeSearchEnabled by remember { mutableStateOf(false) }
    
    // Language options dialog
    var showLanguageDialog by remember { mutableStateOf(false) }
    val languages = listOf("English", "Spanish", "French", "German", "Japanese", "Hindi")

    // Theme selector options
    var showThemeDialog by remember { mutableStateOf(false) }
    val themes = listOf("System Default" to "system", "Light Theme" to "light", "Dark Theme" to "dark")

    // Weather unit selector options
    var showUnitDialog by remember { mutableStateOf(false) }
    val units = listOf("Celsius (°C, km/h)" to "Celsius", "Fahrenheit (°F, mph)" to "Fahrenheit")

    // News content filter options
    var showNewsFilterDialog by remember { mutableStateOf(false) }
    val newsFilterOptions = listOf("None (Show all news)" to "None", "Safe (Filter sensitive topics)" to "Safe")

    // Cache settings dialog options
    var showCacheSettingsDialog by remember { mutableStateOf(false) }
    val cacheSettingsOptions = listOf("Automatic", "Off", "Weekly")

    val user by authViewModel.currentUser.collectAsState()
    val isAdmin = remember(user) {
        user?.email == "kunalgupta131212@gmail.com" || user?.email == "admin@smartexplorer.com" || user?.email?.contains("admin", ignoreCase = true) == true
    }

    // Feedback States
    var feedbackType by remember { mutableStateOf("General Feedback") }
    val feedbackTypes = listOf("Bug Report", "Feature Request", "Performance", "UI/UX", "General Feedback", "Other")
    var feedbackEmail by remember { mutableStateOf("") }
    var feedbackRating by remember { mutableStateOf(5) }
    var feedbackComment by remember { mutableStateOf("") }
    var screenshotUrl by remember { mutableStateOf("") }
    var isAttachingScreenshot by remember { mutableStateOf(false) }
    var isSubmittingFeedback by remember { mutableStateOf(false) }
    var showFeedbackSuccessDialog by remember { mutableStateOf(false) }
    val feedbackList by viewModel.feedbackList.collectAsState()

    // Rating Dialog state
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(5) }

    // Rating Dialog
    if (showRatingDialog) {
        Dialog(onDismissRequest = { showRatingDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rate Smart Explorer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your ratings and feedback help us build a faster, smarter utility gateway for everyone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { selectedRating = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRatingDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            showRatingDialog = false
                            Toast.makeText(context, "Thank you for rating us $selectedRating stars!", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }

    // Language Dropdown Dialog (Translator default target language)
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Default Translator Language",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTranslatorLang(lang)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Translator default set to $lang!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = defaultTargetLangPreference == lang,
                                onClick = {
                                    viewModel.updateTranslatorLang(lang)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Translator default set to $lang!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Preferred Theme Dialog selector
    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Preferred Theme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    themes.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTheme(pair.second)
                                    showThemeDialog = false
                                    Toast.makeText(context, "Preferred theme set to ${pair.first}!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemePreference == pair.second,
                                onClick = {
                                    viewModel.updateTheme(pair.second)
                                    showThemeDialog = false
                                    Toast.makeText(context, "Preferred theme set to ${pair.first}!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = pair.first, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Weather Unit Dialog selector
    if (showUnitDialog) {
        Dialog(onDismissRequest = { showUnitDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Weather Unit System",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    units.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateWeatherUnit(pair.second)
                                    showUnitDialog = false
                                    Toast.makeText(context, "Weather unit updated to ${pair.first}!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = weatherUnitPreference == pair.second,
                                onClick = {
                                    viewModel.updateWeatherUnit(pair.second)
                                    showUnitDialog = false
                                    Toast.makeText(context, "Weather unit updated to ${pair.first}!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = pair.first, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // News Content Filter Dialog selector
    if (showNewsFilterDialog) {
        Dialog(onDismissRequest = { showNewsFilterDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "News Content Filter",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    newsFilterOptions.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateNewsFilter(pair.second)
                                    showNewsFilterDialog = false
                                    Toast.makeText(context, "News filter updated to ${pair.first}!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = newsFilterPreference == pair.second,
                                onClick = {
                                    viewModel.updateNewsFilter(pair.second)
                                    showNewsFilterDialog = false
                                    Toast.makeText(context, "News filter updated to ${pair.first}!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = pair.first, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Cache Settings Dialog selector
    if (showCacheSettingsDialog) {
        Dialog(onDismissRequest = { showCacheSettingsDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Cache Management Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    cacheSettingsOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCacheSettings(option)
                                    showCacheSettingsDialog = false
                                    Toast.makeText(context, "Cache mode updated to $option!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = cacheSettingsPreference == option,
                                onClick = {
                                    viewModel.updateCacheSettings(option)
                                    showCacheSettingsDialog = false
                                    Toast.makeText(context, "Cache mode updated to $option!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Feedback Submission Success Dialog
    if (showFeedbackSuccessDialog) {
        Dialog(onDismissRequest = { showFeedbackSuccessDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Feedback Received!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thank you for your report. Our engineering team has logged this submission in the database and is reviewing it to improve Smart Explorer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showFeedbackSuccessDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("feedback_success_close")
                    ) {
                        Text("Awesome", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Application Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeader(title = "General Preferences")

                // NOTIFICATIONS ROW
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Get alerted about crucial news and updates",
                    checked = isNotificationsEnabled,
                    onCheckedChange = { viewModel.updatePushNotifications(it) }
                )

                // OFFLINE ROW
                SettingsSwitchRow(
                    icon = Icons.Default.NetworkWifi,
                    title = "Offline Cache Sync",
                    subtitle = "Automatically cache queries for offline access",
                    checked = isOfflineModeEnabled,
                    onCheckedChange = { viewModel.updateOfflineMode(it) }
                )

                // PRIVACY MODE ROW
                SettingsSwitchRow(
                    icon = Icons.Default.RemoveRedEye,
                    title = "Privacy Mode",
                    subtitle = "Secure personal analytics and history logs",
                    checked = isPrivacyModeEnabled,
                    onCheckedChange = { viewModel.updatePrivacyMode(it) }
                )

                // SAFE SEARCH ROW
                SettingsSwitchRow(
                    icon = Icons.Default.Security,
                    title = "Safe Search Filters",
                    subtitle = "Moderate translated search queries",
                    checked = isSafeSearchEnabled,
                    onCheckedChange = { isSafeSearchEnabled = it }
                )

                // AI SUGGESTIONS ROW
                SettingsSwitchRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI Suggestions",
                    subtitle = "Receive dynamic assistant suggestions based on context",
                    checked = isAiSuggestionsEnabled,
                    onCheckedChange = { viewModel.updateAiSuggestions(it) }
                )

                // ANALYTICS ROW
                SettingsSwitchRow(
                    icon = Icons.Default.Analytics,
                    title = "Analytics Collection",
                    subtitle = "Share anonymous usage stats to help build better tools",
                    checked = isAnalyticsEnabled,
                    onCheckedChange = { viewModel.updateAnalytics(it) }
                )

                // DYNAMIC PREFERENCES
                SettingsActionRow(
                    icon = Icons.Default.Palette,
                    title = "Preferred Theme",
                    subtitle = "Current: ${currentThemePreference.replaceFirstChar { it.uppercase() }}",
                    onClick = { showThemeDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.Language,
                    title = "Default Translator Language",
                    subtitle = "Current: $defaultTargetLangPreference",
                    onClick = { showLanguageDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.DeviceThermostat,
                    title = "Weather Unit System",
                    subtitle = "Current: $weatherUnitPreference",
                    onClick = { showUnitDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.FilterList,
                    title = "News Content Filter",
                    subtitle = "Current: $newsFilterPreference",
                    onClick = { showNewsFilterDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.Storage,
                    title = "Cache Persistence Mode",
                    subtitle = "Current: $cacheSettingsPreference",
                    onClick = { showCacheSettingsDialog = true }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                SectionHeader(title = "Data Management")

                // CLEAR CACHE BUTTON
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Clean Local Search Cache",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deletes temporary dictionary lookups, weather cache, translation histories, and local country data without resetting favorites.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    database.cachedDataDao().clearAllCache()
                                    staleCacheCount = cacheInvalidationService.countStaleData()
                                    Toast.makeText(context, "Search cache successfully pruned!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("clear_cache_button")
                        ) {
                            Text("Flush Cache Storage", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // TTL CACHE INVALIDATION CARD
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TTL Cache Invalidation",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (staleCacheCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = if (staleCacheCount > 0) "$staleCacheCount Stale Items" else "All Fresh",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (staleCacheCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Clears weather cache entries older than 30 mins and news cache entries older than 1 hour to optimize local storage dynamically or on-demand.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val count = cacheInvalidationService.invalidateStaleData()
                                    staleCacheCount = cacheInvalidationService.countStaleData()
                                    Toast.makeText(context, "Pruned $count stale cache entries successfully!", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("invalidate_stale_cache_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear Stale TTL Cache", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                SectionHeader(title = "Engagement & Promotion")

                SettingsActionRow(
                    icon = Icons.Default.Star,
                    title = "Rate Application",
                    subtitle = "Support our developers by leaving a rating",
                    onClick = { showRatingDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.Share,
                    title = "Share Application",
                    subtitle = "Recommend Smart Explorer to your friends",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Download Smart Explorer")
                            putExtra(Intent.EXTRA_TEXT, "Check out Smart Explorer - Explore Everything in One App! Download now at: https://play.google.com/store/apps/details?id=com.aistudio.smartexplorer")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                SectionHeader(title = "In-App Feedback System")

                if (isAdmin) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAdminDashboard() }
                            .testTag("admin_dashboard_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Admin Feedback Dashboard",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "You are logged in as Admin. Click to access all submitted user feedback, review analytics, and export CSV logs.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Submit Feedback, Bug, or Feature Request",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Found a bug? Have a great feature idea? Share it directly with our product team.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Type selector Row
                        Text(text = "Feedback Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            feedbackTypes.forEach { type ->
                                val isSelected = feedbackType == type
                                OutlinedButton(
                                    onClick = { feedbackType = type },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("feedback_type_${type.replace(" ", "_").lowercase()}")
                                ) {
                                    Text(text = type, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rating selector Row
                        Text(text = "Your Rating", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= feedbackRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { feedbackRating = i }
                                        .testTag("feedback_star_$i")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Contact Email field
                        OutlinedTextField(
                            value = feedbackEmail,
                            onValueChange = { feedbackEmail = it },
                            label = { Text("Contact Email (Optional)", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("feedback_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Comment field
                        OutlinedTextField(
                            value = feedbackComment,
                            onValueChange = { feedbackComment = it },
                            label = { Text("Describe details, bugs, or suggestions...", fontSize = 12.sp) },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("feedback_comment_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        // Screenshot Attachment Section
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Screenshot Attachment (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (screenshotUrl.isEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isAttachingScreenshot = true
                                        delay(800) // Simulate capturing & compressing screenshot
                                        screenshotUrl = "https://picsum.photos/seed/settings_screenshot_${System.currentTimeMillis()}/800/600"
                                        isAttachingScreenshot = false
                                        Toast.makeText(context, "Screenshot attached successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isAttachingScreenshot,
                                modifier = Modifier.fillMaxWidth().testTag("feedback_attach_screenshot_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isAttachingScreenshot) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Processing device screen...", fontSize = 12.sp)
                                    } else {
                                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Simulate Screen Capture & Attach", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            // Screenshot Attached Thumbnail
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = screenshotUrl,
                                        contentDescription = "Feedback Attachment Thumbnail",
                                        modifier = Modifier
                                            .size(94.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Settings_Screenshot.png", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Ready for secure upload", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }
                                    IconButton(
                                        onClick = { screenshotUrl = "" },
                                        modifier = Modifier.testTag("feedback_remove_screenshot")
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove screenshot", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (feedbackComment.trim().isEmpty()) {
                                    Toast.makeText(context, "Please write details of your feedback first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    scope.launch {
                                        isSubmittingFeedback = true
                                        delay(1200) // Simulate compression and upload

                                        val uid = user?.uid ?: "guest_${System.currentTimeMillis()}"
                                        val userName = if (user?.isGuest == true) "Guest" else (user?.name ?: "Guest")
                                        val email = if (feedbackEmail.isNotBlank()) feedbackEmail else (user?.email ?: "guest@smartexplorer.com")
                                        val profilePhoto = user?.photoUrl ?: ""
                                        val loginProvider = if (user?.isGuest == true) "Guest" else (user?.loginProvider ?: "Email")

                                        viewModel.submitFeedback(
                                            uid = uid,
                                            userName = userName,
                                            email = email,
                                            profilePhoto = profilePhoto,
                                            rating = feedbackRating,
                                            category = feedbackType,
                                            message = feedbackComment,
                                            screenshotUrl = screenshotUrl,
                                            appVersion = "1.0.0",
                                            deviceModel = android.os.Build.MODEL,
                                            androidVersion = android.os.Build.VERSION.RELEASE,
                                            loginProvider = loginProvider
                                        ) {
                                            feedbackComment = ""
                                            feedbackEmail = ""
                                            feedbackRating = 5
                                            screenshotUrl = ""
                                            isSubmittingFeedback = false
                                            showFeedbackSuccessDialog = true
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmittingFeedback,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("feedback_submit_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSubmittingFeedback) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compressing & Uploading feedback...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Feedback Report", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // SCOPED FEEDBACK LOG HISTORY
                val userFeedbacks = remember(feedbackList, user) {
                    feedbackList.filter { it.uid == user?.uid }
                }

                if (userFeedbacks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Submitted Feedback (${userFeedbacks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        userFeedbacks.forEach { fb ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (fb.category) {
                                                "Bug Report" -> MaterialTheme.colorScheme.errorContainer
                                                "Feature Request" -> MaterialTheme.colorScheme.primaryContainer
                                                "Performance" -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.tertiaryContainer
                                            }
                                        ) {
                                            Text(
                                                text = fb.category,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = when (fb.category) {
                                                    "Bug Report" -> MaterialTheme.colorScheme.onErrorContainer
                                                    "Feature Request" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    "Performance" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Status Badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (fb.status.lowercase()) {
                                                    "pending" -> Color(0xFFFFB300).copy(alpha = 0.15f)
                                                    "reviewed" -> Color(0xFF8E24AA).copy(alpha = 0.15f)
                                                    "resolved" -> Color(0xFF00C853).copy(alpha = 0.15f)
                                                    else -> Color.Gray.copy(alpha = 0.15f)
                                                }
                                            ) {
                                                Text(
                                                    text = fb.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (fb.status.lowercase()) {
                                                        "pending" -> Color(0xFFFFB300)
                                                        "reviewed" -> Color(0xFF8E24AA)
                                                        "resolved" -> Color(0xFF00C853)
                                                        else -> Color.Gray
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            // Rating Stars
                                            Row {
                                                for (star in 1..5) {
                                                    Icon(
                                                        imageVector = if (star <= fb.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = fb.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (fb.screenshotUrl.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                            Text("Screenshot attached", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    if (fb.email.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Contact: ${fb.email}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                SectionHeader(title = "App Update & Diagnostics")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_update_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Update status icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Smart Update Hub",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Keep your client binaries fresh and synced",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                        // Render update status depending on state
                        when (val state = updateState) {
                            is com.example.data.repository.UpdateCheckResult.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Querying live servers for update data...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is com.example.data.repository.UpdateCheckResult.Error -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Error icon",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sync Failed: ${state.message}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "Ensure you are connected to the internet and retry.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is com.example.data.repository.UpdateCheckResult.Success -> {
                                val config = state.config
                                val isUpdateAvailable = state.isUpdateAvailable
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Status message
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isUpdateAvailable) Icons.Default.Info else Icons.Default.CheckCircle,
                                            contentDescription = "Status indicator",
                                            tint = if (isUpdateAvailable) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isUpdateAvailable) {
                                                if (state.isForceUpdate) "Critical update required! (v${config.latestVersion})" else "New update available (v${config.latestVersion})"
                                            } else {
                                                "You're using the latest version."
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isUpdateAvailable) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                                        )
                                    }

                                    // Details Grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Installed Version",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = state.currentVersion,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Latest Server Build",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = config.latestVersion,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Display Last Checked Time
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Last Checked",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        // Simple formatting of state.lastCheckedTime
                                        val timeStr = if (state.lastCheckedTime == 0L) "Never" else {
                                            try {
                                                val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                                sdf.format(java.util.Date(state.lastCheckedTime))
                                            } catch (e: Exception) {
                                                "Just now"
                                            }
                                        }
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.checkForUpdates(silent = false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("check_updates_button"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Check update icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Check Updates",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Show Simulation Config Toggle Button to Developers/Reviewers
                            OutlinedButton(
                                onClick = { showDeveloperSimulation = !showDeveloperSimulation },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("simulate_panel_toggle_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "Bug report icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showDeveloperSimulation) "Close Config" else "Simulate Updates",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Simulation Configuration panel
                        if (showDeveloperSimulation) {
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Developer Simulation Panel",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Simulate various update configurations locally to inspect UI flows instantly:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Simulation states trigger buttons
                                Button(
                                    onClick = {
                                        viewModel.toggleSimulation(true)
                                        viewModel.updateSimulationConfig(
                                            latestVersion = "1.5.0",
                                            minVersion = "1.4.0",
                                            forceUpdate = false,
                                            releaseNotes = "No new changes."
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Simulate Up To Date (v1.5.0)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Button(
                                    onClick = {
                                        viewModel.toggleSimulation(true)
                                        viewModel.updateSimulationConfig(
                                            latestVersion = "1.6.0",
                                            minVersion = "1.4.0",
                                            forceUpdate = false,
                                            releaseNotes = "• Added Offline Translate\n• Fixed Referral Links\n• Added Interactive Map Updates"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Simulate Optional Update (v1.6.0)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }

                                Button(
                                    onClick = {
                                        viewModel.toggleSimulation(true)
                                        viewModel.updateSimulationConfig(
                                            latestVersion = "1.7.0",
                                            minVersion = "1.6.0",
                                            forceUpdate = true,
                                            releaseNotes = "• CRITICAL: Account security upgrade.\n• This is a mandatory update to maintain cloud synchronization access."
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Simulate Critical Force Update (v1.7.0)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.toggleSimulation(false)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reset to Live Cloud Backend", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                SectionHeader(title = "Legal Compliance & Info")

                SettingsNavigationRow(title = "About Us", onClick = onNavigateToAboutUs)
                SettingsNavigationRow(title = "Contact Us", onClick = onNavigateToContactUs)
                SettingsNavigationRow(title = "Privacy Policy", onClick = onNavigateToPrivacyPolicy)
                SettingsNavigationRow(title = "Terms & Conditions", onClick = onNavigateToTermsConditions)

                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            BannerAd(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag("settings_switch_${title.lowercase().replace(" ", "_")}")
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsNavigationRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.size(20.dp)
        )
    }
}
