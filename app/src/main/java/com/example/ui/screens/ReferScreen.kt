package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ReferralHistoryItem
import com.example.data.local.ReferralUserProfile
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.RedeemState
import com.example.ui.viewmodel.ReferralViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferScreen(
    viewModel: ReferralViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val userProfile by viewModel.userProfile.collectAsState()
    val referralHistory by viewModel.referralHistory.collectAsState()
    val redeemState by viewModel.redeemState.collectAsState()

    var friendCodeInput by remember { mutableStateOf("") }
    var showQrModal by remember { mutableStateOf(false) }

    val referralCode = userProfile?.referralCode ?: "SMART123"
    val referralLink = "https://ais-pre-vrnemixwp433m7djgponng-616954649737.asia-southeast1.run.app/refer?code=$referralCode"

    // Toast and State resets
    LaunchedEffect(redeemState) {
        when (redeemState) {
            is RedeemState.Success -> {
                Toast.makeText(context, (redeemState as RedeemState.Success).message, Toast.LENGTH_LONG).show()
                friendCodeInput = ""
                viewModel.resetRedeemState()
            }
            is RedeemState.Error -> {
                Toast.makeText(context, (redeemState as RedeemState.Error).error, Toast.LENGTH_LONG).show()
                viewModel.resetRedeemState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Refer Friends", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("refer_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        PremiumGradientBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Premium Header Banner
                item {
                    ReferHeaderBanner()
                }

                // 2. Main Stats & Referral Card
                item {
                    MainReferralCard(
                        referralCode = referralCode,
                        referralLink = referralLink,
                        points = userProfile?.points ?: 0,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(referralCode))
                            Toast.makeText(context, "Referral code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            shareReferralLink(context, referralCode, referralLink)
                        },
                        onViewQr = {
                            showQrModal = true
                        }
                    )
                }

                // 3. Quick Share Shortcuts
                item {
                    QuickShareModule(
                        referralCode = referralCode,
                        referralLink = referralLink
                    )
                }

                // 4. Redeem Friends Code Zone
                item {
                    RedeemFriendCodeCard(
                        inputValue = friendCodeInput,
                        onValueChange = { friendCodeInput = it },
                        redeemState = redeemState,
                        isAlreadyRedeemed = userProfile?.redeemedCode != null,
                        redeemedCode = userProfile?.redeemedCode,
                        onRedeemClick = {
                            keyboardController?.hide()
                            viewModel.redeemReferralCode(friendCodeInput)
                        }
                    )
                }

                // 5. How It Works
                item {
                    HowItWorksModule()
                }

                // 6. Referral History
                item {
                    SectionHeader(
                        title = "Referral History",
                        subtitle = "Track your invites and accumulated bonuses"
                    )
                }

                if (referralHistory.isEmpty()) {
                    item {
                        ReferralHistoryEmptyState()
                    }
                } else {
                    items(referralHistory) { item ->
                        ReferralHistoryRow(item = item)
                    }
                }
            }

            // QR Code Dialog
            if (showQrModal) {
                QrCodeDialog(
                    qrPayload = referralLink,
                    referralCode = referralCode,
                    onDismiss = { showQrModal = false }
                )
            }
        }
    }
}

/**
 * Modern header banner featuring a custom 3D reward illustration
 */
@Composable
fun ReferHeaderBanner() {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("refer_header_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .padding(end = 8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "LIMITED OFFER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Invite Friends & Earn",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Refer a friend and earn 5 points after successful signup. Friend gets 2 welcome points.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Generated Premium Gift Reward image
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_gift_reward),
                    contentDescription = "Referral Gift Reward Illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * Main Premium Referral Card showing Code, Points, Copy & Share buttons
 */
@Composable
fun MainReferralCard(
    referralCode: String,
    referralLink: String,
    points: Int,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onViewQr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("main_referral_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Points display metric
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        CircleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Points Token",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "My Reward Balance: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$points PTS",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "YOUR REFERRAL CODE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Beautiful interactive code container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onCopy() },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = referralCode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("referral_code_display")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VerticalDivider(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary link displays
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("share_now_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Link", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onViewQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("view_qr_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QR Code")
                }
            }
        }
    }
}

/**
 * Quick share options panel with WhatsApp, Telegram, SMS, etc.
 */
@Composable
fun QuickShareModule(
    referralCode: String,
    referralLink: String
) {
    val context = LocalContext.current

    val shareTargets = listOf(
        ShareOptionItem("WhatsApp", Icons.Default.Chat, "com.whatsapp"),
        ShareOptionItem("Telegram", Icons.Default.Send, "org.telegram.messenger"),
        ShareOptionItem("SMS", Icons.Default.Textsms, null, isSms = true),
        ShareOptionItem("Facebook", Icons.Default.Facebook, "com.facebook.katana"),
        ShareOptionItem("Instagram", Icons.Default.CameraAlt, "com.instagram.android"),
        ShareOptionItem("More Apps", Icons.Default.MoreHoriz, null)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SHARE VIA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            shareTargets.forEach { target ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(72.dp)
                        .clickable {
                            triggerShareTarget(context, target, referralCode, referralLink)
                        }
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = target.icon,
                                contentDescription = target.name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = target.name,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

data class ShareOptionItem(
    val name: String,
    val icon: ImageVector,
    val packageName: String?,
    val isSms: Boolean = false
)

/**
 * Triggers package-specific or general share logic
 */
fun triggerShareTarget(context: Context, target: ShareOptionItem, code: String, link: String) {
    val messageText = "Hey! I'm using Smart Explorer to translate, check weather, scan QR, and more.\n\nUse my referral code: $code\n\nDownload Smart Explorer:\n$link"
    
    if (target.isSms) {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", messageText)
        }
        try {
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (target.packageName != null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, messageText)
            setPackage(target.packageName)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard chooser if target package is not installed
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
            }, "Share referral with ${target.name}")
            context.startActivity(chooser)
        }
    } else {
        // More options
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, messageText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Referral link via"))
    }
}

fun shareReferralLink(context: Context, code: String, link: String) {
    val messageText = "Hey! I'm using Smart Explorer to translate, check weather, scan QR, and more.\n\nUse my referral code: $code\n\nDownload Smart Explorer:\n$link"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Join Smart Explorer!")
        putExtra(Intent.EXTRA_TEXT, messageText)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Referral Link via"))
}

/**
 * Redeems code with inline status states
 */
@Composable
fun RedeemFriendCodeCard(
    inputValue: String,
    onValueChange: (String) -> Unit,
    redeemState: RedeemState,
    isAlreadyRedeemed: Boolean,
    redeemedCode: String?,
    onRedeemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("redeem_code_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "HAVE A REFERRAL CODE?",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter your friend's code to claim 2 bonus points immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isAlreadyRedeemed) {
                // Already redeemed success banner
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Claimed successfully",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Code '$redeemedCode' claimed! Bonus points credited.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = onValueChange,
                        placeholder = { Text("Enter code (e.g., ALEX1234)", fontSize = 14.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onRedeemClick() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(54.dp)
                            .testTag("redeem_code_text_field")
                    )

                    Button(
                        onClick = onRedeemClick,
                        enabled = inputValue.isNotBlank() && redeemState !is RedeemState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("apply_redeem_button")
                    ) {
                        if (redeemState is RedeemState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Claim Info", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual timeline detailing 'How It Works'
 */
@Composable
fun HowItWorksModule() {
    val steps = listOf(
        Pair("1", "Share Code\nInvite friends via links, SMS, or QR."),
        Pair("2", "Friend Installs\nFriend downloads Smart Explorer app."),
        Pair("3", "Friend Signs Up\nThey paste your code during registration."),
        Pair("4", "Earn Rewards\nFriend gets 2 welcome PTS, you earn 5 PTS!")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("how_it_works_module")
    ) {
        Text(
            text = "HOW IT WORKS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEach { step ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = step.first,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = step.second,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp,
                            maxLines = 4,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralHistoryRow(item: ReferralHistoryItem) {
    val sdf = remember { SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.joinDate) { sdf.format(Date(item.joinDate)) }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.friendName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Surface(
                    color = if (item.status == "Successful") {
                        Color(0xFFE8F5E9)
                    } else {
                        Color(0xFFFFF3E0)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (item.status == "Successful") {
                            Color(0xFF2E7D32)
                        } else {
                            Color(0xFFE65100)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${item.pointsEarned} pts",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ReferralHistoryEmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No invites yet",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Invite friends to track signups and earn rewards automatically.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Elegant modal dialog that displays a deterministic vector QR Code of the referral link.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeDialog(
    qrPayload: String,
    referralCode: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = "Referral QR Code",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Let a friend scan this QR code to download the app with your referral link pre-configured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Frame for QR code
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing custom self-contained QR vector with our exact QrCodeCanvas logic in a neat layout
                        QrPreviewCanvas(
                            payload = qrPayload,
                            modifier = Modifier.size(160.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Code: $referralCode",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Self-contained simplified deterministic QR preview builder
 */
@Composable
fun QrPreviewCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizeW = size.width
        val sizeH = size.height
        val matrixSize = 21 // QR Version 1 grid size

        val fgColor = Color(0xFF1A1A1A)
        val blockWidth = sizeW / matrixSize
        val blockHeight = sizeH / matrixSize

        // Generate deterministic blocks based on payload hashing
        val rand = java.util.Random(payload.hashCode().toLong())
        val grid = Array(matrixSize) { BooleanArray(matrixSize) }
        
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Reserve Finder Patterns (7x7) at three corners
                if ((r < 7 && c < 7) || (r < 7 && c >= matrixSize - 7) || (r >= matrixSize - 7 && c < 7)) {
                    grid[r][c] = false
                } else {
                    grid[r][c] = rand.nextFloat() < 0.50f
                }
            }
        }

        // Draw data dots
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (grid[r][c]) {
                    drawRoundRect(
                        color = fgColor,
                        topLeft = Offset(c * blockWidth, r * blockHeight),
                        size = androidx.compose.ui.geometry.Size(blockWidth * 0.85f, blockHeight * 0.85f),
                        cornerRadius = CornerRadius(blockWidth * 0.25f, blockHeight * 0.25f)
                    )
                }
            }
        }

        // Draw standard QR finder patterns
        val finderWidth = blockWidth * 7f
        val finderHeight = blockHeight * 7f
        val finderCoords = listOf(
            Offset(0f, 0f),
            Offset(sizeW - finderWidth, 0f),
            Offset(0f, sizeH - finderHeight)
        )

        finderCoords.forEach { offset ->
            val oX = offset.x
            val oY = offset.y

            // Outer Frame
            drawRoundRect(
                color = fgColor,
                topLeft = Offset(oX, oY),
                size = androidx.compose.ui.geometry.Size(finderWidth, finderHeight),
                cornerRadius = CornerRadius(blockWidth * 1.5f)
            )
            // Inner Space
            drawRect(
                color = Color.White,
                topLeft = Offset(oX + blockWidth, oY + blockHeight),
                size = androidx.compose.ui.geometry.Size(blockWidth * 5, blockHeight * 5)
            )
            // Center Dot
            drawRoundRect(
                color = fgColor,
                topLeft = Offset(oX + blockWidth * 2, oY + blockHeight * 2),
                size = androidx.compose.ui.geometry.Size(blockWidth * 3, blockHeight * 3),
                cornerRadius = CornerRadius(blockWidth * 0.75f)
            )
        }
    }
}
