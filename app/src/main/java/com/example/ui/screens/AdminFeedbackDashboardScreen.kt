package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.FeedbackItem
import com.example.ui.components.EmptyState
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ExplorerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackDashboardScreen(
    viewModel: ExplorerViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val feedbackList by viewModel.feedbackList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Filter states
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedRatingFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var feedbackToDelete by remember { mutableStateOf<FeedbackItem?>(null) }
    var screenshotToZoom by remember { mutableStateOf<String?>(null) }

    // Categories list matches specifications
    val categories = listOf("All", "Bug Report", "Feature Request", "Performance", "UI/UX", "General Feedback", "Other")
    val ratingFilters = listOf("All", "5★ Only", "4★ Only", "3★ Only", "2★ Only", "1★ Only")

    // Filter feedback based on criteria
    val filteredFeedback = remember(feedbackList, selectedCategory, selectedRatingFilter, searchQuery) {
        feedbackList.filter { fb ->
            val matchesCategory = selectedCategory == "All" || fb.category.equals(selectedCategory, ignoreCase = true)
            
            val matchesRating = when (selectedRatingFilter) {
                "5★ Only" -> fb.rating == 5
                "4★ Only" -> fb.rating == 4
                "3★ Only" -> fb.rating == 3
                "2★ Only" -> fb.rating == 2
                "1★ Only" -> fb.rating == 1
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() || 
                    fb.userName.contains(searchQuery, ignoreCase = true) || 
                    fb.email.contains(searchQuery, ignoreCase = true) ||
                    fb.message.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesRating && matchesSearch
        }
    }

    // Compute Analytics
    val analytics = remember(feedbackList) {
        val total = feedbackList.size
        val avgRating = if (total > 0) feedbackList.map { it.rating }.average() else 0.0
        val bugs = feedbackList.count { it.category.equals("Bug Report", ignoreCase = true) }
        val features = feedbackList.count { it.category.equals("Feature Request", ignoreCase = true) }
        val resolved = feedbackList.count { it.status.equals("Resolved", ignoreCase = true) }
        val pending = feedbackList.count { it.status.equals("Pending", ignoreCase = true) }
        
        AdminFeedbackAnalytics(total, avgRating, bugs, features, resolved, pending)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Feedback Console",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Admin Management Portal",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (feedbackList.isEmpty()) {
                                Toast.makeText(context, "No feedback data to export", Toast.LENGTH_SHORT).show()
                            } else {
                                val csvContent = buildCsvContent(feedbackList)
                                clipboardManager.setText(AnnotatedString(csvContent))
                                triggerCsvShare(context, csvContent)
                            }
                        },
                        modifier = Modifier.testTag("admin_export_csv_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                )
            )
        }
    ) { innerPadding ->
        PremiumGradientBackground {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ANALYTICS CARDS (Horizontal Scroll for Responsive Design)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        AnalyticsCard(
                            title = "Total Feedback",
                            value = analytics.totalFeedback.toString(),
                            icon = Icons.Default.Feedback,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "Average Rating",
                            value = String.format(Locale.US, "%.1f ★", analytics.averageRating),
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFB300)
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "Bug Reports",
                            value = analytics.totalBugs.toString(),
                            icon = Icons.Default.BugReport,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "Feature Requests",
                            value = analytics.totalFeatures.toString(),
                            icon = Icons.Default.NewReleases,
                            color = Color(0xFF00A2FF)
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "Pending Reports",
                            value = analytics.totalPending.toString(),
                            icon = Icons.Default.HourglassEmpty,
                            color = Color(0xFFFF9100)
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "Resolved Tickets",
                            value = analytics.totalResolved.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF00C853)
                        )
                    }
                }

                // SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, email, or content...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_bar")
                )

                // FILTERS (Category Tabs & Rating Chips)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Filter Categories",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("admin_cat_chip_${cat.replace(" ", "_").lowercase()}")
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ratingFilters) { rating ->
                            val isSelected = selectedRatingFilter == rating
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRatingFilter = rating },
                                label = { Text(rating, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("admin_rating_chip_${rating.replace(" ", "_").lowercase()}")
                            )
                        }
                    }
                }

                // FEEDBACK ITEMS LIST
                Text(
                    text = "Feedback Records (${filteredFeedback.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (filteredFeedback.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        title = "No Feedback Matches",
                        description = "Try adjusting your search query, rating filters, or selected feedback category.",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("admin_feedback_list")
                    ) {
                        items(filteredFeedback, key = { it.id }) { item ->
                            AdminFeedbackCard(
                                item = item,
                                onStatusChange = { newStatus ->
                                    viewModel.updateFeedbackStatus(item.id, newStatus)
                                    Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    feedbackToDelete = item
                                },
                                onZoomScreenshot = { url ->
                                    screenshotToZoom = url
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // DELETE CONFIRMATION DIALOG
    feedbackToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { feedbackToDelete = null },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Feedback?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete this feedback from ${item.userName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFeedbackById(item.id)
                        feedbackToDelete = null
                        Toast.makeText(context, "Feedback deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { feedbackToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ZOOM SCREENSHOT DIALOG
    screenshotToZoom?.let { url ->
        Dialog(onDismissRequest = { screenshotToZoom = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Zoomed Screenshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { screenshotToZoom = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
            .width(135.dp)
            .height(95.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
fun AdminFeedbackCard(
    item: FeedbackItem,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onZoomScreenshot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.createdAt) { sdf.format(Date(item.createdAt)) }

    val categoryColor = when (item.category.lowercase()) {
        "bug report" -> Color(0xFFE53935)
        "feature request" -> Color(0xFF1E88E5)
        "performance" -> Color(0xFF8E24AA)
        "ui/ux" -> Color(0xFF00ACC1)
        "general feedback" -> Color(0xFF43A047)
        else -> Color(0xFF757575)
    }

    val statusColor = when (item.status.lowercase()) {
        "pending" -> Color(0xFFFFB300)
        "reviewed" -> Color(0xFF8E24AA)
        "resolved" -> Color(0xFF00C853)
        else -> Color(0xFF757575)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_fb_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // USER INFO ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.profilePhoto.ifBlank { "https://picsum.photos/seed/${item.uid}/200/200" },
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.email,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // LOGIN PROVIDER CHIP
                if (item.loginProvider.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = item.loginProvider,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CATEGORY, RATING, AND STATUS BADGES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Rating stars
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= item.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FEEDBACK MESSAGE
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // SCREENSHOT PREVIEW (IF AVAILABLE)
            if (item.screenshotUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { onZoomScreenshot(item.screenshotUrl) }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.screenshotUrl,
                            contentDescription = "Feedback Screenshot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.15f))
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zoom Screenshot",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SYSTEM/METADATA ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device & OS Metadata
                Text(
                    text = "${item.deviceModel} (Android ${item.androidVersion}) • v${item.appVersion}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                // Date/Time
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // ADMIN CONTROL ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Update Status:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusButton(
                        text = "Pending",
                        isSelected = item.status.lowercase() == "pending",
                        color = Color(0xFFFFB300),
                        onClick = { onStatusChange("Pending") },
                        modifier = Modifier.testTag("status_pending_${item.id}")
                    )
                    StatusButton(
                        text = "Reviewed",
                        isSelected = item.status.lowercase() == "reviewed",
                        color = Color(0xFF8E24AA),
                        onClick = { onStatusChange("Reviewed") },
                        modifier = Modifier.testTag("status_reviewed_${item.id}")
                    )
                    StatusButton(
                        text = "Resolved",
                        isSelected = item.status.lowercase() == "resolved",
                        color = Color(0xFF00C853),
                        onClick = { onStatusChange("Resolved") },
                        modifier = Modifier.testTag("status_resolved_${item.id}")
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("admin_delete_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusButton(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = modifier.height(28.dp)
    ) {
        Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

data class AdminFeedbackAnalytics(
    val totalFeedback: Int,
    val averageRating: Double,
    val totalBugs: Int,
    val totalFeatures: Int,
    val totalResolved: Int,
    val totalPending: Int
)

fun buildCsvContent(feedbackList: List<FeedbackItem>): String {
    val builder = StringBuilder()
    builder.append("ID,UID,User,Email,Rating,Category,Message,Status,Device,Android,AppVersion,Timestamp\n")
    for (fb in feedbackList) {
        val safeUser = fb.userName.replace(",", " ").replace("\n", " ").trim()
        val safeEmail = fb.email.replace(",", " ").replace("\n", " ").trim()
        val safeMessage = fb.message.replace(",", " ").replace("\n", " ").trim()
        val safeDevice = fb.deviceModel.replace(",", " ").replace("\n", " ").trim()
        builder.append("${fb.id},${fb.uid},$safeUser,$safeEmail,${fb.rating},${fb.category},$safeMessage,${fb.status},$safeDevice,${fb.androidVersion},${fb.appVersion},${fb.createdAt}\n")
    }
    return builder.toString()
}

fun triggerCsvShare(context: Context, csvContent: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Feedback Export CSV")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        context.startActivity(Intent.createChooser(intent, "Share CSV Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share CSV: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
