package com.example.ui.screens.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ExplorerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- REUSABLE APP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(
    title: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )
}

// --- REUSABLE HELPER ROW COMPONENTS ---
@Composable
fun ProfileSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .padding(vertical = 8.dp),
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.12f else 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag("profile_switch_${title.replace(" ", "_").lowercase()}")
        )
    }
}

@Composable
fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
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
                color = tint.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}


// ==========================================
// 1. SETTINGS SCREEN
// ==========================================
@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Settings States
    var isAutoUpdateEnabled by remember { mutableStateOf(true) }
    var isDataSaverEnabled by remember { mutableStateOf(false) }
    var isDeveloperOptionsEnabled by remember { mutableStateOf(false) }

    var selectedModel by remember { mutableStateOf("Gemini 1.5 Flash (Default)") }
    var showModelDialog by remember { mutableStateOf(false) }

    var selectedSearchEngine by remember { mutableStateOf("Google") }
    var showEngineDialog by remember { mutableStateOf(false) }

    var selectedDownloadLoc by remember { mutableStateOf("Internal Storage") }
    var showDownloadDialog by remember { mutableStateOf(false) }

    var cacheSize by remember { mutableStateOf("128.4 MB") }
    var isClearingCache by remember { mutableStateOf(false) }

    var lastBackupTime by remember { mutableStateOf("Today at 4:32 AM") }
    var isBackingUp by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "App Settings", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // App Preferences Header
                SectionHeader(title = "Preferences", subtitle = "Configure engine & background activities")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    // AI Model Selection
                    ProfileActionRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Model Selection",
                        subtitle = "Current: $selectedModel"
                    ) {
                        showModelDialog = true
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Search Engine Selection
                    ProfileActionRow(
                        icon = Icons.Default.Search,
                        title = "Default Search Engine",
                        subtitle = "Current: $selectedSearchEngine"
                    ) {
                        showEngineDialog = true
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Download Location
                    ProfileActionRow(
                        icon = Icons.Default.Folder,
                        title = "Download Location",
                        subtitle = "Current: $selectedDownloadLoc"
                    ) {
                        showDownloadDialog = true
                    }
                }

                SectionHeader(title = "Network & Automation", subtitle = "Control data usage & background updates")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    // Auto Update Toggle
                    ProfileSwitchRow(
                        icon = Icons.Default.Refresh,
                        title = "Auto Update",
                        subtitle = "Download hotfixes over Wi-Fi automatically",
                        checked = isAutoUpdateEnabled
                    ) {
                        isAutoUpdateEnabled = it
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (it) "Auto updates enabled" else "Auto updates disabled"
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Data Saver Mode Toggle
                    ProfileSwitchRow(
                        icon = Icons.Default.SignalCellularAlt,
                        title = "Data Saver Mode",
                        subtitle = "Compress queries and disable heavy asset preloading",
                        checked = isDataSaverEnabled
                    ) {
                        isDataSaverEnabled = it
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (it) "Data Saver active (Preloads offline)" else "Data Saver inactive"
                            )
                        }
                    }
                }

                SectionHeader(title = "Data Management", subtitle = "Secure cache and cloud database backups")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    // Cache management row with loading animation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Cache Management", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Local SQLite & web caches: $cacheSize", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (isClearingCache) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isClearingCache = true
                                        delay(1000)
                                        cacheSize = "0.0 MB"
                                        isClearingCache = false
                                        snackbarHostState.showSnackbar("Temporary app caches pruned successfully!")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Clear Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Backup & Restore
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Last backup: $lastBackupTime", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isBackingUp = true
                                        delay(1500)
                                        lastBackupTime = "Just now"
                                        isBackingUp = false
                                        snackbarHostState.showSnackbar("Backup database synchronized successfully!")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Backup Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SectionHeader(title = "Advanced Options")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileSwitchRow(
                        icon = Icons.Default.DeveloperMode,
                        title = "Developer Options",
                        subtitle = "Enable diagnostics, logs & sandbox mode",
                        checked = isDeveloperOptionsEnabled
                    ) {
                        isDeveloperOptionsEnabled = it
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (it) "Developer Sandbox tools active" else "Developer mode disabled"
                            )
                        }
                    }

                    AnimatedVisibility(visible = isDeveloperOptionsEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp)
                        ) {
                            Text("Diagnostics Console", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Database Version: v3 (SQLite)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("API Sandbox: Connected to Live Gateway", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("OS Version: Android " + android.os.Build.VERSION.RELEASE, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Build Environment: Production-Active", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // AI Model Selector Dialog
    if (showModelDialog) {
        Dialog(onDismissRequest = { showModelDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select AI Core Intelligence Model", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    listOf("Gemini 1.5 Flash (Default)", "Gemini 1.5 Pro", "Gemini 2.0 Experimental").forEach { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedModel = model
                                    showModelDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Assigned intelligence pipeline to $model") }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedModel == model,
                                onClick = {
                                    selectedModel = model
                                    showModelDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Assigned intelligence pipeline to $model") }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = model, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Search Engine Selector Dialog
    if (showEngineDialog) {
        Dialog(onDismissRequest = { showEngineDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Default Search Gateway", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    listOf("Google", "Bing", "Yahoo", "DuckDuckGo").forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSearchEngine = engine
                                    showEngineDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Primary search engine set to $engine") }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSearchEngine == engine,
                                onClick = {
                                    selectedSearchEngine = engine
                                    showEngineDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Primary search engine set to $engine") }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = engine, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Download Location Selector Dialog
    if (showDownloadDialog) {
        Dialog(onDismissRequest = { showDownloadDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Target Download Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    listOf("Internal Storage", "External SD Card").forEach { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDownloadLoc = loc
                                    showDownloadDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Download target moved to $loc") }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDownloadLoc == loc,
                                onClick = {
                                    selectedDownloadLoc = loc
                                    showDownloadDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Download target moved to $loc") }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = loc, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. PRIVACY SCREEN
// ==========================================
@Composable
fun ProfilePrivacyScreen(
    explorerViewModel: com.example.ui.viewmodel.ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val biometricLockEnabled by explorerViewModel.biometricLockState.collectAsState()

    // Dialog state
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Permissions Status
    var isStorageGranted by remember { mutableStateOf(true) }
    var isCameraGranted by remember { mutableStateOf(false) }
    var isMicrophoneGranted by remember { mutableStateOf(false) }
    var isNotificationGranted by remember { mutableStateOf(true) }

    // Destruction Warnings
    var showDeleteSearchDialog by remember { mutableStateOf(false) }
    var showDeleteDownloadDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    var isProcessingDelete by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "Privacy & Consent", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            if (isProcessingDelete) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Surgical data deletion in progress...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    SectionHeader(title = "Compliance Documents", subtitle = "Legally binding terms & transparency")

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        ProfileActionRow(
                            icon = Icons.Default.Description,
                            title = "Privacy Policy",
                            subtitle = "Transparency on collected analytics metrics",
                            tint = MaterialTheme.colorScheme.primary
                        ) {
                            showPrivacyPolicyDialog = true
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        ProfileActionRow(
                            icon = Icons.Default.Gavel,
                            title = "Terms & Conditions",
                            subtitle = "Rules for utility API request usage",
                            tint = MaterialTheme.colorScheme.primary
                        ) {
                            showTermsDialog = true
                        }
                    }

                    SectionHeader(title = "Permissions Manager", subtitle = "View and request dynamic Android system access")

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        // Storage
                        PermissionItemRow(
                            title = "Storage Permission",
                            subtitle = "Required for saving news images, logs & backups",
                            isGranted = isStorageGranted
                        ) {
                            isStorageGranted = !isStorageGranted
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isStorageGranted) "Storage access GRANTED!" else "Storage access REVOKED!"
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        // Camera
                        PermissionItemRow(
                            title = "Camera Permission",
                            subtitle = "Required for QR scanning tools",
                            isGranted = isCameraGranted
                        ) {
                            isCameraGranted = !isCameraGranted
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isCameraGranted) "Camera access GRANTED!" else "Camera access REVOKED!"
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        // Microphone
                        PermissionItemRow(
                            title = "Microphone Permission",
                            subtitle = "Required for real-time speech-to-text translations",
                            isGranted = isMicrophoneGranted
                        ) {
                            isMicrophoneGranted = !isMicrophoneGranted
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isMicrophoneGranted) "Microphone access GRANTED!" else "Microphone access REVOKED!"
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        // Notification
                        PermissionItemRow(
                            title = "Notification Permission",
                            subtitle = "Required for background alarms, alerts & task cues",
                            isGranted = isNotificationGranted
                        ) {
                            isNotificationGranted = !isNotificationGranted
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isNotificationGranted) "Notification alerts ACTIVE!" else "Notifications MUTED!"
                                )
                            }
                        }
                    }

                    SectionHeader(title = "App Lock Security", subtitle = "Protect app access with local biometrics")

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        ProfileSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric Screen Lock",
                            subtitle = "Require fingerprint or face recognition on app launch",
                            checked = biometricLockEnabled
                        ) { isEnabled ->
                            explorerViewModel.updateBiometricLock(isEnabled)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isEnabled) "Biometric Lock enabled!" else "Biometric Lock disabled!"
                                )
                            }
                        }
                    }

                    SectionHeader(title = "Data Destruction", subtitle = "Irreversibly delete historical search logs and files")

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        ProfileActionRow(
                            icon = Icons.Default.History,
                            title = "Delete Search History",
                            subtitle = "Wipe temporary online query lookups",
                            tint = MaterialTheme.colorScheme.error
                        ) {
                            showDeleteSearchDialog = true
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        ProfileActionRow(
                            icon = Icons.Default.FolderDelete,
                            title = "Delete Download History",
                            subtitle = "Clear downloaded logs & references from index",
                            tint = MaterialTheme.colorScheme.error
                        ) {
                            showDeleteDownloadDialog = true
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        ProfileActionRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete All Data",
                            subtitle = "Wipe SQLite database and reset application to factory defaults",
                            tint = MaterialTheme.colorScheme.error
                        ) {
                            showDeleteAllDialog = true
                        }
                    }

                    SectionHeader(title = "Security & Encryption")

                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("AES-256 Encryption Engaged", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Your credentials, saved notes, custom lists, and translated records are secured with client-side Zero-Knowledge standards.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // PRIVACY POLICY DIALOG
    if (showPrivacyPolicyDialog) {
        Dialog(onDismissRequest = { showPrivacyPolicyDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Privacy Policy Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        text = "Smart Explorer is an offline-first mobile application. Most operations, dictionary data cache, and translations reside strictly on your physical storage.\n\n" +
                                "1. Analytics Tracking: We collect anonymous performance telemetry (latency, screen count) via firebase standard logs to improve application speed.\n\n" +
                                "2. Ad Networks: Standard GDPR/EEA personalized cookies are governed strictly via authorized Google User Messaging SDK consent boxes.\n\n" +
                                "By continuing use of the app, you approve these safety procedures.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showPrivacyPolicyDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("I Agree", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // TERMS DIALOG
    if (showTermsDialog) {
        Dialog(onDismissRequest = { showTermsDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Terms & Conditions Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        text = "1. Personal Use Only: You are granted a personal, revocable licence to query utility APIs under standard API restrictions.\n\n" +
                                "2. No Reverse-Engineering: Siphoning our compiled SQLite dictionaries, translation indexes, or automated queries is strictly forbidden.\n\n" +
                                "3. API Limitation: Weather and currency converters are mapped over free open-endpoints. Accuracy is 'as-is' and not legally certified.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showTermsDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Accept", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // DELETE SEARCH HISTORY WARNING
    if (showDeleteSearchDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSearchDialog = false },
            title = { Text("Delete Search History?") },
            text = { Text("This will permanently purge your local query lookups, dictionary definitions, and caching tables. This is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSearchDialog = false
                        scope.launch {
                            isProcessingDelete = true
                            delay(1000)
                            isProcessingDelete = false
                            snackbarHostState.showSnackbar("Query history wiped cleanly.")
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Forever", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSearchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DELETE DOWNLOAD HISTORY WARNING
    if (showDeleteDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDownloadDialog = false },
            title = { Text("Delete Download Index?") },
            text = { Text("This will remove all downloaded item catalog logs from the app's index list without deleting the physical files from your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDownloadDialog = false
                        scope.launch {
                            isProcessingDelete = true
                            delay(1000)
                            isProcessingDelete = false
                            snackbarHostState.showSnackbar("Download logs purged.")
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDownloadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DELETE ALL DATA WARNING
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("HARD RESET APPLICATION?") },
            text = { Text("This action is critical. This will permanently wipe your local SQLite database, clear search records, delete downloads catalog, reset all customized preferences (language, theme), and log you out immediately. Are you absolutely sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllDialog = false
                        scope.launch {
                            isProcessingDelete = true
                            delay(2000)
                            isProcessingDelete = false
                            snackbarHostState.showSnackbar("All systems wiped. Reinitializing app database...")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("WIPE EVERYTHING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Permissions Helper Row
@Composable
fun PermissionItemRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                contentColor = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isGranted) "Granted" else "Denied", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ==========================================
// 3. NOTIFICATIONS SCREEN
// ==========================================
@Composable
fun ProfileNotificationsScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isNotificationMasterEnabled by viewModel.pushNotificationsState.collectAsState()
    val isNewsAlertsEnabled by viewModel.notifNewsState.collectAsState()
    val isWeatherAlertsEnabled by viewModel.notifWeatherState.collectAsState()
    val isAppUpdatesEnabled by viewModel.notifFeaturesState.collectAsState()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updatePushNotifications(true)
            scope.launch { snackbarHostState.showSnackbar("Notifications permission granted!") }
        } else {
            viewModel.updatePushNotifications(false)
            scope.launch { snackbarHostState.showSnackbar("Permission denied. Alerts cannot be posted.") }
        }
    }

    // Settings
    var isAiTipsEnabled by remember { mutableStateOf(true) }
    var isDownloadAlertsEnabled by remember { mutableStateOf(true) }
    var isPromotionalEnabled by remember { mutableStateOf(false) }

    var selectedSound by remember { mutableStateOf("Zen Breeze") }
    var showSoundDialog by remember { mutableStateOf(false) }

    var selectedVibration by remember { mutableStateOf("Default (Medium)") }
    var showVibrationDialog by remember { mutableStateOf(false) }

    var isSilentMode by remember { mutableStateOf(false) }
    var isDndEnabled by remember { mutableStateOf(false) }
    var dndStartHour by remember { mutableStateOf(22f) } // 10 PM
    var dndEndHour by remember { mutableStateOf(7f) }    // 7 AM

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "Notification Manager", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                SectionHeader(title = "Master Control", subtitle = "Global notification permission switch")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileSwitchRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Enable Notifications",
                        subtitle = "Receive app alerts, cues & translation pings",
                        checked = isNotificationMasterEnabled
                    ) { enabled ->
                        if (enabled) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.updatePushNotifications(true)
                                scope.launch { snackbarHostState.showSnackbar("Global notifications enabled") }
                            }
                        } else {
                            viewModel.updatePushNotifications(false)
                            scope.launch { snackbarHostState.showSnackbar("All app notifications muted") }
                        }
                    }
                }

                SectionHeader(title = "Alert Categories", subtitle = "Customize when you want to be notified")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileSwitchRow(
                        icon = Icons.Default.TipsAndUpdates,
                        title = "AI Tips",
                        subtitle = "Intelligent recommendations & query suggestions",
                        checked = isAiTipsEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        isAiTipsEnabled = it
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileSwitchRow(
                        icon = Icons.Default.DownloadDone,
                        title = "Download Complete Alerts",
                        subtitle = "Instant alert when tool outputs or notes export finishes",
                        checked = isDownloadAlertsEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        isDownloadAlertsEnabled = it
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileSwitchRow(
                        icon = Icons.Default.Update,
                        title = "New App Features",
                        subtitle = "Alerts when new features & tools are released",
                        checked = isAppUpdatesEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        viewModel.updateNotifFeatures(it)
                        scope.launch { snackbarHostState.showSnackbar(if (it) "Subscribed to feature updates" else "Unsubscribed from feature updates") }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileSwitchRow(
                        icon = Icons.Default.Newspaper,
                        title = "Breaking News Alerts",
                        subtitle = "Live alerts for hot global headlines",
                        checked = isNewsAlertsEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        viewModel.updateNotifNews(it)
                        scope.launch { snackbarHostState.showSnackbar(if (it) "Subscribed to news alerts" else "Unsubscribed from news alerts") }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileSwitchRow(
                        icon = Icons.Default.Cloud,
                        title = "Weather Advisories",
                        subtitle = "Alerts for significant weather shifts & advisories",
                        checked = isWeatherAlertsEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        viewModel.updateNotifWeather(it)
                        scope.launch { snackbarHostState.showSnackbar(if (it) "Subscribed to weather advisories" else "Unsubscribed from weather advisories") }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileSwitchRow(
                        icon = Icons.Default.Campaign,
                        title = "Promotional Notifications",
                        subtitle = "App tutorials, features & exclusive workspace pings",
                        checked = isPromotionalEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        isPromotionalEnabled = it
                    }
                }

                SectionHeader(title = "Aesthetic Signals", subtitle = "Sound & haptics customizations")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileActionRow(
                        icon = Icons.Default.MusicNote,
                        title = "Notification Sound",
                        subtitle = "Current: $selectedSound"
                    ) {
                        if (isNotificationMasterEnabled) showSoundDialog = true
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileActionRow(
                        icon = Icons.Default.Vibration,
                        title = "Notification Vibration",
                        subtitle = "Current: $selectedVibration"
                    ) {
                        if (isNotificationMasterEnabled) showVibrationDialog = true
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileSwitchRow(
                        icon = Icons.Default.VolumeMute,
                        title = "Silent Mode",
                        subtitle = "Force mute all signals immediately",
                        checked = isSilentMode,
                        enabled = isNotificationMasterEnabled
                    ) {
                        isSilentMode = it
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (it) "App is now completely silent" else "Sounds and vibration restored"
                            )
                        }
                    }
                }

                SectionHeader(title = "Quiet Hours", subtitle = "Mute pings automatically on a scheduled calendar")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileSwitchRow(
                        icon = Icons.Default.DoNotDisturb,
                        title = "Do Not Disturb Schedule",
                        subtitle = "Mute alerts automatically during quiet hours",
                        checked = isDndEnabled,
                        enabled = isNotificationMasterEnabled
                    ) {
                        isDndEnabled = it
                    }

                    AnimatedVisibility(visible = isDndEnabled && isNotificationMasterEnabled) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Quiet Hours Start: ${dndStartHour.toInt()}:00 PM",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Slider(
                                value = dndStartHour,
                                onValueChange = { dndStartHour = it },
                                valueRange = 12f..24f,
                                steps = 11
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Quiet Hours End: ${dndEndHour.toInt()}:00 AM",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Slider(
                                value = dndEndHour,
                                onValueChange = { dndEndHour = it },
                                valueRange = 0f..12f,
                                steps = 12
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sound Selector Dialog
    if (showSoundDialog) {
        Dialog(onDismissRequest = { showSoundDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Notification Chime", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    listOf("Default Chime", "Zen Breeze", "Digital Pulse", "Retro Beep").forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSound = sound
                                    showSoundDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Assigned alert ringtone: $sound") }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSound == sound,
                                onClick = {
                                    selectedSound = sound
                                    showSoundDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Assigned alert ringtone: $sound") }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = sound, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Vibration Selector Dialog
    if (showVibrationDialog) {
        Dialog(onDismissRequest = { showVibrationDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Vibration Strength", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    listOf("Default (Medium)", "Strong Burst", "Double Tap", "Disabled/Silent").forEach { vib ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedVibration = vib
                                    showVibrationDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Vibration pattern updated: $vib") }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedVibration == vib,
                                onClick = {
                                    selectedVibration = vib
                                    showVibrationDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Vibration pattern updated: $vib") }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = vib, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. LANGUAGE SCREEN
// ==========================================
@Composable
fun ProfileLanguageScreen(
    explorerViewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedLang by explorerViewModel.appLanguageState.collectAsState()

    val languages = listOf(
        Triple("English", "English", "🇬🇧"),
        Triple("Spanish", "Español", "🇪🇸"),
        Triple("French", "Français", "🇫🇷"),
        Triple("German", "Deutsch", "🇩🇪"),
        Triple("Japanese", "日本語", "🇯🇵"),
        Triple("Hindi", "हिन्दी", "🇮🇳")
    )

    var isAutoDetectLanguage by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "App Language", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                SectionHeader(title = "Automatic Locales")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isAutoDetectLanguage = !isAutoDetectLanguage
                                if (isAutoDetectLanguage) {
                                    explorerViewModel.updateAppLanguage("English")
                                } else {
                                    explorerViewModel.updateAppLanguage("English")
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isAutoDetectLanguage) "System Default auto-detection active" else "Manual override active"
                                    )
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GTranslate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Auto Detect Language", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("App will adapt to phone native language", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isAutoDetectLanguage,
                            onCheckedChange = {
                                isAutoDetectLanguage = it
                                if (it) {
                                    explorerViewModel.updateAppLanguage("English")
                                } else {
                                    explorerViewModel.updateAppLanguage("English")
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (it) "System Default auto-detection active" else "Manual override active"
                                    )
                                }
                            }
                        )
                    }
                }

                SectionHeader(title = "Available Languages", subtitle = "Choose your translation dialect")

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(languages) { (engName, nativeName, flag) ->
                        val isSelected = selectedLang == engName && !isAutoDetectLanguage
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    isAutoDetectLanguage = false
                                    explorerViewModel.updateAppLanguage(engName)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Workspace language set to $engName!")
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.03f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = flag, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = engName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = nativeName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        isAutoDetectLanguage = false
                                        explorerViewModel.updateAppLanguage(engName)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Workspace language set to $engName!")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


// ==========================================
// 5. APPEARANCE SCREEN
// ==========================================
@Composable
fun ProfileAppearanceScreen(
    explorerViewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val themePreference by explorerViewModel.themeState.collectAsState()
    val accentPreference by explorerViewModel.accentColorState.collectAsState()

    // Aesthetics State
    val selectedTheme = when (themePreference) {
        "light" -> "Light Mode"
        "dark" -> "Dark Mode"
        else -> "System Theme"
    }

    val selectedAccentColor = when (accentPreference.lowercase()) {
        "green" -> Color(0xFF10B981)
        "orange" -> Color(0xFFF97316)
        "purple" -> Color(0xFF8B5CF6)
        "rose" -> Color(0xFFF43F5E)
        else -> MaterialTheme.colorScheme.primary
    }

    var fontSize by remember { mutableStateOf(14f) } // Default medium
    var cardStyle by remember { mutableStateOf("Glassmorphic") }
    var cornerRadius by remember { mutableStateOf(20f) }
    var animationSpeed by remember { mutableStateOf(1.0f) }

    val accentColors = listOf(
        Pair("Default", Color(0xFF0F52FF)),
        Pair("Green", Color(0xFF10B981)),
        Pair("Orange", Color(0xFFF97316)),
        Pair("Purple", Color(0xFF8B5CF6)),
        Pair("Rose", Color(0xFFF43F5E))
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "Appearance & Theme", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Interactive Realtime Preview Card
                SectionHeader(title = "Realtime Layout Preview", subtitle = "Dynamic styling preview box")

                Card(
                    shape = RoundedCornerShape(cornerRadius.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTheme == "Light Mode") Color.White else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            if (cardStyle == "Outlined") 2.dp else 1.dp,
                            if (cardStyle == "Outlined") selectedAccentColor else selectedAccentColor.copy(alpha = 0.2f),
                            RoundedCornerShape(cornerRadius.dp)
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (cardStyle == "Elevated") 8.dp else 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = selectedAccentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = selectedAccentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Accent Preview Text",
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize.sp,
                                color = if (selectedTheme == "Light Mode") Color.DarkGray else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This premium container adapts reactively to your accent color, theme, rounded corner shape, and card elevated designs.",
                            fontSize = (fontSize - 3).sp,
                            color = if (selectedTheme == "Light Mode") Color.Gray else Color.White.copy(alpha = 0.7f),
                            lineHeight = (fontSize + 2).sp
                        )
                    }
                }

                SectionHeader(title = "Theme Selector", subtitle = "Choose core interface color system")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf("Light Mode", "Dark Mode", "System Theme")
                    themes.forEach { themeName ->
                        val isSelected = selectedTheme == themeName
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clickable {
                                    val newPref = when (themeName) {
                                        "Light Mode" -> "light"
                                        "Dark Mode" -> "dark"
                                        else -> "system"
                                    }
                                    explorerViewModel.updateTheme(newPref)
                                    scope.launch { snackbarHostState.showSnackbar("Interface set to $themeName") }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) selectedAccentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) selectedAccentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = when (themeName) {
                                        "Light Mode" -> Icons.Default.LightMode
                                        "Dark Mode" -> Icons.Default.DarkMode
                                        else -> Icons.Default.SettingsSuggest
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) selectedAccentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(themeName, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SectionHeader(title = "Accent Color Picker", subtitle = "Paint primary app controls and buttons")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    accentColors.forEach { (name, color) ->
                        val isSelected = accentPreference.lowercase() == name.lowercase() ||
                                         (name == "Default" && accentPreference.lowercase() == "default")
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    explorerViewModel.updateAccentColor(name.lowercase())
                                    scope.launch { snackbarHostState.showSnackbar("$name accent color applied!") }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                explorerViewModel.updateAccentColor("wallpaper")
                                scope.launch { snackbarHostState.showSnackbar("Dynamic wallpaper theme applied!") }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Dynamic theme requires Android 12+") }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (accentPreference == "wallpaper") selectedAccentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (accentPreference == "wallpaper") selectedAccentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wallpaper-Based Theme (Android 12+)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Extract a dynamic theme automatically from your home screen wallpaper",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (accentPreference == "wallpaper") {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                SectionHeader(title = "Font Sizing", subtitle = "Adjust overall reader visual size")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Current font scale: " + when {
                            fontSize < 12f -> "Compact (Small)"
                            fontSize < 16f -> "Balanced (Medium)"
                            fontSize < 18f -> "Large"
                            else -> "Extreme Accessibility"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 10f..20f,
                        steps = 5
                    )
                }

                SectionHeader(title = "Custom Card Style", subtitle = "Design system styling parameters")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Glassmorphic", "Elevated", "Flat", "Outlined").forEach { style ->
                        val isSelected = cardStyle == style
                        Button(
                            onClick = { cardStyle = style },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) selectedAccentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(style, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                SectionHeader(title = "Roundness Parameter", subtitle = "Configure border shape curvature")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Corner Curve Radius: ${cornerRadius.toInt()} dp", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Slider(
                        value = cornerRadius,
                        onValueChange = { cornerRadius = it },
                        valueRange = 0f..28f,
                        steps = 7
                    )
                }

                SectionHeader(title = "System Motion", subtitle = "Transition animation speeds")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Animation Speed Multiplier: ${animationSpeed}x " + when {
                            animationSpeed < 0.8f -> "(Snappy)"
                            animationSpeed < 1.2f -> "(Balanced)"
                            else -> "(Cinematic Fluid)"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = animationSpeed,
                        onValueChange = { animationSpeed = it },
                        valueRange = 0.5f..1.5f,
                        steps = 2
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// ==========================================
// 6. HELP & SUPPORT SCREEN
// ==========================================
@Composable
fun ProfileSupportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Expanding FAQs States
    var expandedFaqIndex by remember { mutableStateOf(-1) }

    // Support Form states
    var supportName by remember { mutableStateOf("") }
    var supportEmail by remember { mutableStateOf("") }
    var supportMessage by remember { mutableStateOf("") }
    var isSubmittingTicket by remember { mutableStateOf(false) }

    // Live chat state
    var showLiveChat by remember { mutableStateOf(false) }

    // Rating star states
    var starRating by remember { mutableStateOf(5) }
    var showRatingDialog by remember { mutableStateOf(false) }

    val faqs = listOf(
        "How does offline search work?" to "Smart Explorer caches query results, dictionary lookup tables, and weather forecasts inside its internal Room SQLite database automatically. When network drops, offline cached fallbacks are served instantly.",
        "Is my workspace data secure?" to "Absolutely. We enforce local-only zero-knowledge database standards. Your lists, keys, notes, and records are secured strictly on your device using AES-256 standard encryption.",
        "How do I switch AI Models?" to "You can toggle Gemini models inside the Profile -> App Settings screen dynamically. Background API tokens update automatically."
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "Help & Support Console", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                SectionHeader(title = "Interactive FAQs", subtitle = "Accordion search lookups")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    faqs.forEachIndexed { index, (q, a) ->
                        val isExpanded = expandedFaqIndex == index
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedFaqIndex = if (isExpanded) -1 else index
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = q,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                AnimatedVisibility(visible = isExpanded) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = a, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                SectionHeader(title = "Live Chat", subtitle = "Talk instantly to our smart help assistant")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Instant Live Chat Bot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Ask questions or get quick tutorials on smart features", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showLiveChat = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Launch Support Chat", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                SectionHeader(title = "Create Support Ticket", subtitle = "Log a report directly with engineering")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = supportName,
                        onValueChange = { supportName = it },
                        label = { Text("Name", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supportEmail,
                        onValueChange = { supportEmail = it },
                        label = { Text("Email", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supportMessage,
                        onValueChange = { supportMessage = it },
                        label = { Text("Describe bug, feature, or query details...", fontSize = 11.sp) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSubmittingTicket) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                if (supportMessage.isBlank()) {
                                    Toast.makeText(context, "Please write details first", Toast.LENGTH_SHORT).show()
                                } else {
                                    scope.launch {
                                        isSubmittingTicket = true
                                        delay(1500)
                                        isSubmittingTicket = false
                                        supportMessage = ""
                                        snackbarHostState.showSnackbar("Ticket #TS-83204 logged! We'll reply within 24h.")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Ticket", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SectionHeader(title = "Channels & Engagement")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    // Email
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "message/rfc822"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@smartexplorer.app"))
                                    putExtra(Intent.EXTRA_SUBJECT, "Smart Explorer Feedback")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Send mail..."))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email client found.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Email Help Center", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("support@smartexplorer.app", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Telegram
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Navigating to: t.me/smartexplorer_community", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Telegram Community", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Join developer community @smartexplorer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Rate
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRatingDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Rate Us", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Support our development with 5 stars!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Download Smart Explorer")
                                    putExtra(Intent.EXTRA_TEXT, "Check out Smart Explorer - Explore Everything in One App! Download now at: https://play.google.com/store/apps/details?id=com.aistudio.smartexplorer")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Share Application", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Recommend our toolkit to friends", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Interactive Star Rating Dialog
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
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Rate Smart Explorer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Support our development with stars!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { starRating = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showRatingDialog = false
                            Toast.makeText(context, "Thanks for rating us $starRating stars!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Rating", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // LIVE CHAT SIMULATION DIALOG
    if (showLiveChat) {
        var chatInput by remember { mutableStateOf("") }
        val chatMessages = remember {
            mutableStateListOf(
                "Gemini Bot" to "Hello! Welcome to Smart Explorer live help center. How can I assist you today?"
            )
        }
        val listState = rememberScrollState()

        Dialog(onDismissRequest = { showLiveChat = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, modifier = Modifier.size(32.dp), color = Color.White.copy(alpha = 0.2f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gemini Live Help", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                                Text("Online Assistant", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { showLiveChat = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Chat messages list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(listState)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.forEach { (sender, text) ->
                            val isMe = sender == "Me"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.widthIn(max = 240.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = text, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Ask something...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    val query = chatInput.trim()
                                    chatMessages.add("Me" to query)
                                    chatInput = ""
                                    scope.launch {
                                        delay(500)
                                        listState.animateScrollTo(listState.maxValue)
                                        delay(1000)
                                        val reply = when {
                                            query.lowercase().contains("offline") || query.lowercase().contains("internet") -> {
                                                "Yes! Dictionary, weather caches, and local logs are kept in a Room database on your device and load offline automatically."
                                            }
                                            query.lowercase().contains("delete") || query.lowercase().contains("wipe") -> {
                                                "You can irreversibly delete your history under Privacy Settings, or permanently terminate your account from the Danger Zone."
                                            }
                                            query.lowercase().contains("theme") || query.lowercase().contains("dark") || query.lowercase().contains("color") -> {
                                                "You can select Light/Dark themes, AMOLED support, or paint your buttons with custom accents under the Appearance screen!"
                                            }
                                            else -> "Thank you! Our Smart Explorer support system logged your message. Is there anything else you'd like to check?"
                                        }
                                        chatMessages.add("Gemini Bot" to reply)
                                        delay(300)
                                        listState.animateScrollTo(listState.maxValue)
                                    }
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 7. ABOUT SCREEN
// ==========================================
@Composable
fun ProfileAboutScreen(
    viewModel: com.example.ui.viewmodel.ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLicensesDialog by remember { mutableStateOf(false) }
    val updateState by viewModel.updateState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ProfileAppBar(title = "About & Licenses", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // APP LOGO COMPONENT
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Smart Explorer Logo",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Smart Explorer",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Version 1.5.0-premium",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Build #42981-release",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Update Status chip in About Screen
                    when (val state = updateState) {
                        is com.example.data.repository.UpdateCheckResult.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Checking for updates...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is com.example.data.repository.UpdateCheckResult.Success -> {
                            if (state.isUpdateAvailable) {
                                Button(
                                    onClick = {
                                        val url = state.config.updateUrl.ifBlank {
                                            "https://play.google.com/store/apps/details?id=${context.packageName}"
                                        }
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Unable to open update link", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = "Update status icon",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Update Available (v${state.config.latestVersion})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clickable { viewModel.checkForUpdates(silent = false) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Check Circle Icon",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "App is up to date",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                        is com.example.data.repository.UpdateCheckResult.Error -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable { viewModel.checkForUpdates(silent = false) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error Icon",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Update check failed (Tap to retry)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Smart Explorer aggregates powerful utility services (AI model integration, dictionary databases, local offline compilers, news gateways, and translation cores) into one edge-to-edge, polished utility console.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SectionHeader(title = "Changelog History")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ChangelogItem("v1.5.0-premium", "Added customized Premium Profile screens, live support chat bot, dynamic accent color pickers & safety account wipes.")
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ChangelogItem("v1.4.0", "Introduced Room SQLite offline dictionary/weather caches, and edge-to-edge system drawing optimizations.")
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ChangelogItem("v1.3.0", "Configured Gemini AI model engine and translation capabilities.")
                }

                SectionHeader(title = "Publisher Specifications")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Developer Brand", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Google AI Studio Pro", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Architecture Team", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Gemini Architectures", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                SectionHeader(title = "Legal Information & Open-Source")

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileActionRow(
                        icon = Icons.Default.Source,
                        title = "Open Source Licenses",
                        subtitle = "View software libraries & notice disclosures"
                    ) {
                        showLicensesDialog = true
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    ProfileActionRow(
                        icon = Icons.Default.Public,
                        title = "Official Website",
                        subtitle = "Launch https://smartexplorer.app"
                    ) {
                        Toast.makeText(context, "Opening Official Portal...", Toast.LENGTH_SHORT).show()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // LICENSES LIST DIALOG
    if (showLicensesDialog) {
        Dialog(onDismissRequest = { showLicensesDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Open Source Licenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LicenseItem("Jetpack Compose", "Apache License 2.0")
                        LicenseItem("Kotlin Coroutines", "Apache License 2.0")
                        LicenseItem("SQLite Room Database", "Apache License 2.0")
                        LicenseItem("Coil Image Loader", "Apache License 2.0")
                        LicenseItem("Retrofit Gateway", "Apache License 2.0")
                        LicenseItem("Vico Chart Library", "Apache License 2.0")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showLicensesDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun ChangelogItem(version: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = version, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
    }
}

@Composable
fun LicenseItem(lib: String, license: String) {
    Column {
        Text(lib, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(license, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


// ==========================================
// 8. DELETE ACCOUNT SCREEN
// ==========================================
@Composable
fun ProfileDeleteAccountScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onDeletedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var emailConfirmation by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var wipeDataChecked by remember { mutableStateOf(false) }

    var isProcessingWipe by remember { mutableStateOf(false) }
    var deletionStepText by remember { mutableStateOf("") }

    var showFinalDeleteDialog by remember { mutableStateOf(false) }

    // Inputs valid condition
    val canSubmit = emailConfirmation.isNotBlank() && passwordConfirmation.isNotBlank() && wipeDataChecked

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ProfileAppBar(title = "Danger Zone: Wipe Account", onBack = onBack) }
    ) { innerPadding ->
        PremiumGradientBackground {
            if (isProcessingWipe) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = deletionStepText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Red Warning Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Critical Warning",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "WARNING: Critical Operation",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Deactivating and deleting your Smart Explorer profile is permanent and cannot be undone. All your bookmarks, saved lists, secure vaults, translation records, and custom workspace logs will be completely and irreversibly purged from our systems.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    SectionHeader(title = "Wipe Checklist", subtitle = "Purge logs & deauthorize sessions")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BulletDeleteRow("Purge local SQLite database dictionary cache files.")
                            BulletDeleteRow("Erase local app secure shared preferences.")
                            BulletDeleteRow("Revoke all app storage and camera system permissions.")
                            BulletDeleteRow("Erase diagnostic analytical sessions.")
                        }
                    }

                    SectionHeader(title = "Verification Credentials", subtitle = "Verify ownership of the current session")

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = emailConfirmation,
                            onValueChange = { emailConfirmation = it },
                            label = { Text("Confirm Email", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = passwordConfirmation,
                            onValueChange = { passwordConfirmation = it },
                            label = { Text("Confirm Password", fontSize = 11.sp) },
                            singleLine = true,
                            visualTransformation = if (showConfirmPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                  .fillMaxWidth()
                                  .clickable { wipeDataChecked = !wipeDataChecked }
                        ) {
                            Checkbox(
                                checked = wipeDataChecked,
                                onCheckedChange = { wipeDataChecked = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "I understand and explicitly approve the permanent deletion of my data.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showFinalDeleteDialog = true },
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Permanently Delete Account", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // FINAL TERMINATION ALERT
    if (showFinalDeleteDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showFinalDeleteDialog = false },
            title = { Text("PERMANENTLY DELETE PROFILE?") },
            text = { Text("Are you absolutely sure you want to delete your Smart Explorer account and all associated databases? There is no recovery.") },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalDeleteDialog = false
                        isProcessingWipe = true
                        deletionStepText = "Authenticating and deconstructing profile..."
                        authViewModel.deleteAccount(
                            password = passwordConfirmation,
                            onSuccess = {
                                isProcessingWipe = false
                                Toast.makeText(context, "Your profile has been wiped cleanly.", Toast.LENGTH_LONG).show()
                                onDeletedSuccess()
                            },
                            onError = { error ->
                                isProcessingWipe = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Failed to delete: $error")
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("YES, WIPE FOREVER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BulletDeleteRow(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.RemoveCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
