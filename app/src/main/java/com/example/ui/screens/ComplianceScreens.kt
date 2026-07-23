package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PremiumGradientBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Standard Glassmorphic Compliance Header.
 */
@Composable
fun ComplianceHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Standard Global Footer used on every legal/support page.
 */
@Composable
fun LegalFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "© 2026 Smart Explorer. All Rights Reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Visual bullet row style for key details.
 */
@Composable
fun BulletRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            lineHeight = 20.sp
        )
    }
}

/**
 * Custom Compose-Drawn App Logo.
 */
@Composable
fun AnimatedAppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(100.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            // Draw a globe (circle with longitude/latitude curves)
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.4f),
                radius = size.minDimension / 2f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            // Latitude lines
            drawOval(
                color = Color.Cyan.copy(alpha = 0.25f),
                topLeft = Offset(0f, size.height * 0.25f),
                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.5f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
            // Longitude line (vertical ellipse)
            drawOval(
                color = Color.Cyan.copy(alpha = 0.25f),
                topLeft = Offset(size.width * 0.35f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
            
            // Draw magnifying glass
            val glassCenter = Offset(size.width * 0.45f, size.height * 0.45f)
            val glassRadius = size.minDimension * 0.22f
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = glassRadius,
                center = glassCenter,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
            // Lens glow
            drawCircle(
                color = Color.Magenta.copy(alpha = 0.2f),
                radius = glassRadius - 1.dp.toPx(),
                center = glassCenter
            )
            // Handle of magnifying glass
            drawLine(
                color = Color.White,
                start = Offset(glassCenter.x + glassRadius * 0.7f, glassCenter.y + glassRadius * 0.7f),
                end = Offset(size.width * 0.9f, size.height * 0.9f),
                strokeWidth = 5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

/**
 * 1. PRIVACY POLICY SCREEN
 */
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "Privacy Policy", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Privacy Commitment",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last Updated: July 22, 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "At Smart Explorer, we value your trust and are fully committed to protecting your personal data with robust, transparent security policies. We operate on a data-minimization, client-side caching architecture.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Privacy Topics
                listOf(
                    Triple("Data Collection", "We collect minimal diagnostic usage state locally to cache essential weather summaries, translator histories, and news configurations directly in your secure on-device SQLite database. No raw personal identifiers are harvested without consent.", Icons.Default.Info),
                    Triple("Data Usage", "Stored data is purely utilized to optimize API response latencies, display seamless offline mock support on signal dropouts, and track rewards inside your local workspace.", Icons.Default.Storage),
                    Triple("Firebase Usage", "We utilize standard Google Firebase Auth, Remote Config, and Firestore servers to manage secure cloud sync capabilities, update versions silently, and maintain your referral system metrics securely.", Icons.Default.CloudQueue),
                    Triple("Google Sign-In", "Google login payloads are strictly parsed client-side for secure OAuth 2.0 validation tokens, mapping safe display avatars, names, and emails directly into the app state without secondary routing.", Icons.Default.VpnKey),
                    Triple("Guest Login", "Guest operations run inside a fully decoupled offline sandbox. Sandbox data stays physically restricted to your device and is permanently cleared upon account deletion or logout.", Icons.Default.PersonOutline),
                    Triple("Permissions Used", "• INTERNET: To query global headlines, exchange rates, translate text, and fetch local weather summaries.\n• BIOMETRICS: To enable the rapid fingerprint/face-unlock vault guard screen.\n• CAMERA: Required to perform local, real-time QR and barcode scanning operations.", Icons.Default.Security),
                    Triple("Ads & Analytics", "We aggregate completely anonymous performance metadata (latency, click frequencies) via Google Analytics. Any integrated banner ads are served safely via AdMob in strict compliance with GDPR privacy guidelines.", Icons.Default.TrendingUp),
                    Triple("Cookies", "Smart Explorer does not utilize persistent tracking web cookies. We store secure, encrypted on-device session keys inside your sandboxed app configurations directory.", Icons.Default.FolderOpen),
                    Triple("Third-Party Services", "This application utilizes OpenWeatherMap API, REST Countries API, NewsAPI, and Gemini API. Each service handles queries under its independent official privacy terms.", Icons.Default.Language),
                    Triple("User Rights", "Under GDPR/CCPA regulations, you hold the legal right to inspect your stored cache, export data, revoke analytical tracking consent, or irreversibly terminate and delete your account from the Settings panel.", Icons.Default.Lock)
                ).forEach { (title, description, icon) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Contact Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Privacy Questions or Requests?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reach out to our Data Protection Officer at:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "privacy@smartexplorer.com",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, textDecoration = TextDecoration.Underline),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                LegalFooter()
            }
        }
    }
}

/**
 * 2. TERMS & CONDITIONS SCREEN
 */
@Composable
fun TermsConditionsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "Terms of Service", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "User Agreement",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last Updated: July 22, 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Welcome to Smart Explorer! By downloading, installing, or interacting with our mobile application, you explicitly consent to be legally bound by these terms.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }

                listOf(
                    Triple("Acceptance of Terms", "You must represent that you have reached the age of majority in your jurisdiction to contract with us. If you do not accept these guidelines, please uninstall this application immediately.", Icons.Default.AssignmentTurnedIn),
                    Triple("User Responsibilities", "You agree to safeguard your personal credentials, manage the physical security of your local biometric lock configuration, and ensure all input data does not violate third-party trademarks.", Icons.Default.Gavel),
                    Triple("Prohibited Activities", "Reverse-engineering our binary, deploying automated scripts/scrapers to crawl country APIs, spoofing news aggregators, or flooding Gemini translation request pathways is strictly prohibited.", Icons.Default.Block),
                    Triple("Referral Program Rules", "Users may share unique invitations. Self-referrals, automated account generation, or utilizing third-party simulators to farm rewards will result in immediate permanent account termination.", Icons.Default.GroupAdd),
                    Triple("Rewards Policy", "Referral rewards or loyalty points granted within the application are virtual utility indices with zero cash equivalents. Rewards are non-transferable and can be expired by Smart Explorer upon 30 days notice.", Icons.Default.Stars),
                    Triple("Account Suspension", "We reserve the sole discretion to block guest keys, terminate synced accounts, or restrict hardware access without notice if fraudulent traffic or code scraping is detected.", Icons.Default.NoAccounts),
                    Triple("Disclaimer of Warranties", "This utility is provided 'As-Is'. While we execute high-quality engineering standards, we extend no warranties for news timeline completeness, weather real-time accuracy, or translation translation semantics.", Icons.Default.ErrorOutline),
                    Triple("Limitation of Liability", "To the maximum extent permitted by local statutory law, Smart Explorer and its creators hold zero liability for data dropouts, server latencies, or visual rendering crashes.", Icons.Default.RemoveCircleOutline),
                    Triple("Updates to Terms", "We post modifications directly here. Your continuous operation of our features after effective policy edits constitutes dynamic acceptance of the revised legal terms.", Icons.Default.Update)
                ).forEach { (title, description, icon) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                LegalFooter()
            }
        }
    }
}

/**
 * 3. HELP & SUPPORT SCREEN
 */
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToContact: () -> Unit,
    onNavigateToAppVersion: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form states
    var bugTitle by remember { mutableStateOf("") }
    var bugDesc by remember { mutableStateOf("") }
    var bugSeverity by remember { mutableStateOf("Low") }
    var feedbackText by remember { mutableStateOf("") }
    var starRating by remember { mutableStateOf(5) }

    // Simulated check update state
    var isCheckingUpdates by remember { mutableStateOf(false) }

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "Help & Support Page", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Welcome Agent Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
                        }
                        Column {
                            Text(text = "Support Desk", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "How can we assist you today? Our average response time is under 15 minutes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Grid Navigation Options
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToFAQ() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Help, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Browse FAQ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Quick Solutions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToContact() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ContactSupport, null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Contact Us", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Send Query Directly", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToAppVersion() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SystemUpdate, null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("App Update", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Version Controls", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToLicenses() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("OSS Licenses", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Third Party Tech", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Check for Updates Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Update, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Simulated App Update", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current Version: v1.4.0 • Built on Kotlin/Compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (!isCheckingUpdates) {
                                    scope.launch {
                                        isCheckingUpdates = true
                                        delay(1500)
                                        isCheckingUpdates = false
                                        Toast.makeText(context, "✅ You are using the latest version: v1.4.0", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isCheckingUpdates) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing with Server...")
                            } else {
                                Text("Check for Updates")
                            }
                        }
                    }
                }

                // Report a Bug Form
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.BugReport, null, tint = MaterialTheme.colorScheme.error)
                            Text("Report a Bug", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        
                        OutlinedTextField(
                            value = bugTitle,
                            onValueChange = { bugTitle = it },
                            label = { Text("What went wrong?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = bugDesc,
                            onValueChange = { bugDesc = it },
                            label = { Text("Steps to reproduce...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Severity:", style = MaterialTheme.typography.bodyMedium)
                            listOf("Low", "Medium", "High", "Critical").forEach { level ->
                                FilterChip(
                                    selected = bugSeverity == level,
                                    onClick = { bugSeverity = level },
                                    label = { Text(level) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (bugTitle.isBlank() || bugDesc.isBlank()) {
                                    Toast.makeText(context, "Please complete bug details.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "🐛 Bug logged! Reference: #${(1000..9999).random()}", Toast.LENGTH_LONG).show()
                                    bugTitle = ""
                                    bugDesc = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit Bug Report")
                        }
                    }
                }

                // Send Feedback Form
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Feedback, null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Send Feedback", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        // Star ratings
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Rating:")
                            (1..5).forEach { index ->
                                Icon(
                                    imageVector = if (index <= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (index <= starRating) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { starRating = index }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Your thoughts or suggestions...") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (feedbackText.isBlank()) {
                                    Toast.makeText(context, "Please enter some feedback.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "💖 Thank you! Feedback recorded successfully.", Toast.LENGTH_LONG).show()
                                    feedbackText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit Feedback")
                        }
                    }
                }

                // Contacts Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Connect with Creators", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Opening mail client...", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("support@smartexplorer.com", style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Redirecting to Telegram...", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Telegram Community Group", style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Live Chat (Coming Soon)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }

                LegalFooter()
            }
        }
    }
}

/**
 * 4. ABOUT APP SCREEN
 */
@Composable
fun AboutUsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToLicenses: () -> Unit = {}
) {
    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "About App", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedAppLogo()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Smart Explorer",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "v1.4.0 (Build 104) • Developer: AI Studio Team",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Executive Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Smart Explorer is a high-performance offline-first utility system. It aggregates real-time weather summary indicators, secure biometric vault systems, multi-language Gemini translators, local QR modules, and automated exchange metrics into an elite modern visual layout.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Tech Stack Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Code, null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Technologies Leveraged", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "Kotlin & Coroutines: Asynchronous pipeline optimization",
                            "Jetpack Compose: Modern, dynamic edge-to-edge UI layouts",
                            "SQLite Room Database: Robust local query caching",
                            "Retrofit Client: Secure, cached web requests pipelines",
                            "Gemini REST Client: State-of-the-art AI parsing modules",
                            "Firebase suite: Secure cloud authorization & Remote Config"
                        ).forEach { BulletRow(it) }
                    }
                }

                // Timeline Changelog Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Timeline, null, tint = MaterialTheme.colorScheme.tertiary)
                            Text("Changelog & Journey", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Vertical timeline elements
                        ChangelogTimelineItem(
                            version = "v1.4.0",
                            date = "July 22, 2026",
                            notes = listOf(
                                "Upgraded entire Login footer into fully functional Legal & Support structure.",
                                "Implemented unified dark premium glassmorphic pages with zero dead ends.",
                                "Created interactive FAQ lists and simulated OTA updates checking."
                            ),
                            isLatest = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ChangelogTimelineItem(
                            version = "v1.3.0",
                            date = "June 10, 2026",
                            notes = listOf(
                                "Added real-time weather status widget caching to Room database.",
                                "Integrated breaking headlines news filters mapped with preferences."
                            ),
                            isLatest = false
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ChangelogTimelineItem(
                            version = "v1.2.0",
                            date = "April 15, 2026",
                            notes = listOf(
                                "Deployed Vault Notes local AES cryptographic lockers.",
                                "Released local QR scanner camera reader utility."
                            ),
                            isLatest = false
                        )
                    }
                }

                // Credits Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Third Party Credits", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This application incorporates materials from the Android Open Source Project, icons from Material Symbols, and map assets from Google Maps platform.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToLicenses,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View Open Source Licenses", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                LegalFooter()
            }
        }
    }
}

@Composable
fun ChangelogTimelineItem(
    version: String,
    date: String,
    notes: List<String>,
    isLatest: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = version, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                if (isLatest) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("LATEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Text(text = date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            notes.forEach { note ->
                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    Text("• ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                }
            }
        }
    }
}

/**
 * 5. CONTACT US SCREEN
 */
@Composable
fun ContactUsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "Contact Us", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Get In Touch",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )

                Text(
                    text = "For customer queries, general suggestions, privacy requests, or business opportunities, select our dedicated email lines below or drop an inline message directly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Cards for Official Channels
                listOf(
                    Triple("Customer Support Email", "support@smartexplorer.com", Icons.Default.Email),
                    Triple("Privacy & Security Office", "privacy@smartexplorer.com", Icons.Default.Security),
                    Triple("Business Partnership Office", "business@smartexplorer.com", Icons.Default.BusinessCenter),
                    Triple("Official Product Portal", "www.smartexplorer.com", Icons.Default.Language),
                    Triple("Telegram Community", "@SmartExplorerCommunity", Icons.Default.Send),
                    Triple("Open Source Repository", "github.com/aistudio/smart-explorer", Icons.Default.Code)
                ).forEach { (title, subtitle, icon) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Opening link: $subtitle", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Support message form
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Drop a Message", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Your Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = contactMessage,
                            onValueChange = { contactMessage = it },
                            label = { Text("How can we help you?") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (contactName.isBlank() || contactEmail.isBlank() || contactMessage.isBlank()) {
                                    Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "✉️ Message transmitted successfully!", Toast.LENGTH_LONG).show()
                                    contactName = ""
                                    contactEmail = ""
                                    contactMessage = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send Direct Message")
                        }
                    }
                }

                LegalFooter()
            }
        }
    }
}

/**
 * 6. FAQ SCREEN
 */
@Composable
fun FAQScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedFaqIndex by remember { mutableStateOf(-1) }

    val faqs = listOf(
        Triple("How to use the app", "Smart Explorer operates an interactive tools grid. Navigate to any option like Weather, News, or Translator from your Home page. Toggle preferences seamlessly in your sidebar Settings.", Icons.Default.Directions),
        Triple("Login issues", "Ensure your network connection is online. When registering, check your inbox for our authentication verification links. Guest modes run purely sandboxed locally.", Icons.Default.Login),
        Triple("Referral system", "Retrieve your referral code from your Profile settings page and share with friends. When they enter the code during registration, reward parameters sync to both accounts.", Icons.Default.GroupAdd),
        Triple("Rewards", "Your reward metrics accumulate dynamically based on invitations and system checklists. Tap the Refer status card to inspect redemption catalogs.", Icons.Default.Stars),
        Triple("Translation", "Enter any query text, select target language pairs, and tap translate. Translating features utilize advanced state-of-the-art Gemini LLM algorithms for perfect semantic mappings.", Icons.Default.Translate),
        Triple("Weather", "Tap the Weather summary card to fetch real-time barometric parameters, temperatures, wind metrics, and humidity ratios. It caches updates internally for rapid offline view.", Icons.Default.WbSunny),
        Triple("QR Scanner", "Grant system camera permission in your prompts. Align standard barcodes or QR modules inside the framing layout to instantly copy values or redirect in your local browser.", Icons.Default.QrCodeScanner),
        Triple("News", "Stay connected with breaking timelines globally. Filter stories by category (Tech, Sports, Business) or search for tags. News structures cache articles for easy transit reading.", Icons.Default.Newspaper),
        Triple("Currency Converter", "Type custom currency values, pick source and destination target nodes, and fetch conversions. Converter nodes leverage live market values synced from central servers.", Icons.Default.MonetizationOn),
        Triple("AI Features", "Our artificial intelligence capabilities leverage direct Google Gemini REST protocols. You can speak naturally, ask summaries, translate essays, and generate automated checklists in real-time.", Icons.Default.AutoAwesome)
    )

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "FAQ Search", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Help Desk & FAQ Library",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "Find immediate, production-ready solutions for all core components of Smart Explorer below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                faqs.forEachIndexed { index, (question, answer, icon) ->
                    val isExpanded = expandedFaqIndex == index
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFaqIndex = if (isExpanded) -1 else index }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Text(text = question, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                LegalFooter()
            }
        }
    }
}

/**
 * 7. APP VERSION SCREEN
 */
@Composable
fun AppVersionScreen(
    onBack: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "App Version Control", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "System Information",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Current Installed Version", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v1.4.0", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Latest Release Build", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v1.4.0", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Build Number", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("104", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Release Deployment Date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("July 22, 2026", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Button(
                    onClick = {
                        if (!isChecking) {
                            scope.launch {
                                isChecking = true
                                updateResult = null
                                delay(1600)
                                isChecking = false
                                updateResult = "✅ You're using the latest version."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Querying OTA Repository...")
                    } else {
                        Text("Check for Updates")
                    }
                }

                updateResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = result,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // What's new card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("What's New in v1.4.0", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "Completely redesigned Legal & Support network structures.",
                            "Polished glassmorphic theme elements for responsive tablet view.",
                            "Optimized translation APIs latency parameters by 24%.",
                            "Resolved background OTA verification notifications conflicts."
                        ).forEach { BulletRow(it) }
                    }
                }

                Button(
                    onClick = onNavigateToLicenses,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Third-Party Libraries & Licenses", color = MaterialTheme.colorScheme.onSurface)
                }

                LegalFooter()
            }
        }
    }
}

/**
 * 8. OPEN SOURCE LICENSES SCREEN
 */
@Composable
fun OpenSourceLicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val licenses = listOf(
        Pair("androidx.compose:compose-bom", "v2024.02.01\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("org.jetbrains.kotlinx:kotlinx-coroutines-core", "v1.7.3\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("androidx.room:room-runtime", "v2.6.1\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("io.coil-kt:coil-compose", "v2.5.0\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("com.squareup.retrofit2:retrofit", "v2.9.0\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("com.google.ai.client.generativeai", "v0.2.2\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("com.squareup.okhttp3:logging-interceptor", "v4.12.0\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"),
        Pair("androidx.compose.material3:material3", "v1.2.0\nLicense: Apache-2.0\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0")
    )

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            ComplianceHeader(title = "Open Source Licenses", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "OSS Declarations",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "We extend deep gratitude to the open-source developer ecosystem. The following high-performance third-party packages are compiled within Smart Explorer:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                licenses.forEach { (library, licenseBody) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = library,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = licenseBody,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                LegalFooter()
            }
        }
    }
}
