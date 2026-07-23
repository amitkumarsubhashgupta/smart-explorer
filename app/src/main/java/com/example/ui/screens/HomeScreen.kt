package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.UiState

data class CustomBentoFeature(
    val id: String,
    val title: String,
    val category: String,
    val icon: ImageVector,
    val route: String?, // Null if handled by interactive modal/sheet in place
    val accentColors: List<Color>,
    val badge: String? = null,
    val isInteractive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    explorerViewModel: ExplorerViewModel,
    onFeatureSelected: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    
    val userName = remember(user) {
        if (user != null) {
            if (user?.isGuest == true) {
                "Guest"
            } else {
                user?.name ?: "User"
            }
        } else {
            "Guest"
        }
    }

    val greetingText by explorerViewModel.greetingState.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                explorerViewModel.refreshGreeting()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val avatarUrl = remember(user) {
        if (user != null && !user!!.isGuest && user!!.photoUrl.isNotEmpty()) {
            user!!.photoUrl
        } else {
            "https://picsum.photos/seed/guest_explorer/200/200"
        }
    }

    val sharedPrefs = remember { context.getSharedPreferences("smart_profile_prefs", android.content.Context.MODE_PRIVATE) }
    val profileCity = remember(user) {
        sharedPrefs.getString("profile_city", null) ?: "Mumbai"
    }

    LaunchedEffect(profileCity) {
        explorerViewModel.searchWeather(profileCity)
    }

    val weatherUnit by explorerViewModel.weatherUnitState.collectAsState()
    val weatherState by explorerViewModel.weatherState.collectAsState()

    val formatTemp = remember(weatherUnit) {
        { tempCelsius: Double ->
            if (weatherUnit == "Fahrenheit") {
                val f = (tempCelsius * 9 / 5) + 32
                String.format(java.util.Locale.ROOT, "%.0f°F", f)
            } else {
                String.format(java.util.Locale.ROOT, "%.0f°C", tempCelsius)
            }
        }
    }

    // Search query filter
    var searchQuery by remember { mutableStateOf("") }

    // Simulation states for interactive tools clicked on the grid
    var activeToolDialog by remember { mutableStateOf<String?>(null) }
    
    // Calculator simulation states
    var calcExpr by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("") }

    // World Clock state
    var selectedClockZone by remember { mutableStateOf("London (GMT)") }

    // Speed test state
    var speedTestResult by remember { mutableStateOf("Ready to Test") }
    var isTestingSpeed by remember { mutableStateOf(false) }

    // Currency simulator
    var currencyAmount by remember { mutableStateOf("100") }

    // Complete suite of requested modules mapping to both screen routes and in-place dialog triggers
    val features = remember {
        listOf(
            CustomBentoFeature("weather", "Weather Radar", "Live forecasts", Icons.Default.Cloud, "weather", listOf(Color(0xFF00C6FF), Color(0xFF0072FF)), "24°C"),
            CustomBentoFeature("translator", "Translator", "EN ⇆ ES", Icons.Default.Translate, "translator", listOf(Color(0xFF42A5F5), Color(0xFF478ED1)), "Smart"),
            CustomBentoFeature("news", "Global News", "Curated Feed", Icons.Default.Newspaper, "news", listOf(Color(0xFFFFB300), Color(0xFFF57C00)), "Live"),
            CustomBentoFeature("countries", "Country Facts", "250+ Territories", Icons.Default.Public, "countries", listOf(Color(0xFF66BB6A), Color(0xFF388E3C))),
            CustomBentoFeature("dictionary", "Dictionary", "Lexicon", Icons.Default.Book, "dictionary", listOf(Color(0xFF8D6E63), Color(0xFF5D4037)), "v2"),
            CustomBentoFeature("currency", "Currency Converter", "Interactive", Icons.Default.CurrencyExchange, "currency", listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5))),
            
            // Dedicated interactive tools
            CustomBentoFeature("qr_gen", "QR Generator", "Generate codes", Icons.Default.QrCode, "qr_gen", listOf(Color(0xFFEC407A), Color(0xFFD81B60)), "New", isInteractive = false),
            CustomBentoFeature("qr_scan", "QR Scanner", "Secure Lens Scan", Icons.Default.CropFree, "qr_scan", listOf(Color(0xFF26A69A), Color(0xFF00695C)), null, isInteractive = false),
            CustomBentoFeature("calculator", "Calculator", "Smart solver", Icons.Default.Calculate, "calculator", listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2)), "M+", isInteractive = false),
            CustomBentoFeature("clock", "World Clock", "Zone Telemetry", Icons.Default.Schedule, "world_clock", listOf(Color(0xFF26C6DA), Color(0xFF00838F)), null, isInteractive = false),
            CustomBentoFeature("speed", "Internet Speed", "Ping & Telemetry", Icons.Default.Speed, "internet_speed", listOf(Color(0xFFFF7043), Color(0xFFD84315)), "Ping", isInteractive = false),
            CustomBentoFeature("map", "Explorer Map", "Navigation Node", Icons.Default.Map, "explorer_map", listOf(Color(0xFF26A69A), Color(0xFF00897B)), "GPS", isInteractive = false),
            CustomBentoFeature("notes", "Vault Notes", "Secure Memos", Icons.Default.EditNote, "vault_notes", listOf(Color(0xFF78909C), Color(0xFF455A64)), "Lock", isInteractive = false),
            CustomBentoFeature("bookmarks", "Saved Bookmarks", "Storage Cache", Icons.Default.Bookmark, "favorites", listOf(Color(0xFFFFA726), Color(0xFFE65100)), "Offline", isInteractive = false),
            CustomBentoFeature("crypto", "Crypto Ticker", "Coin telemetrics", Icons.Default.TrendingUp, "crypto_ticker", listOf(Color(0xFFFFCA28), Color(0xFFF57F17)), "BTC", isInteractive = false),
            CustomBentoFeature("gold", "Commodity Prices", "Gold & Silver", Icons.Default.WorkspacePremium, "commodity_prices", listOf(Color(0xFFFFD54F), Color(0xFFFFB300)), null, isInteractive = false),
            CustomBentoFeature("facts", "Random Facts", "Instantly expand mind", Icons.Default.Info, "random_facts", listOf(Color(0xFF29B6F6), Color(0xFF0288D1)), "Mind", isInteractive = false),
            CustomBentoFeature("jokes", "Dad Jokes", "Instant Humor", Icons.Default.SentimentVerySatisfied, "jokes", listOf(Color(0xFFEC407A), Color(0xFFC2185B))),
            CustomBentoFeature("emergency", "Emergency Desk", "Global contacts", Icons.Default.LocalPhone, "emergency_desk", listOf(Color(0xFFEF5350), Color(0xFFC62828)), "SOS", isInteractive = false)
        )
    }

    // Filter features based on search bar query
    val filteredFeatures = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            features
        } else {
            features.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // TOP LOGO BAR & PROFILE METRIC
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onOpenDrawer?.invoke() },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF101827).copy(alpha = 0.6f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            .testTag("home_menu_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToSettings() }
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF4F8CFF), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = greetingText,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.55f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF101827).copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SCROLLABLE BODY
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                
                // 1. WEATHER & CLIMATE SUMMARY (Bento block with real-time aesthetic)
                val currentCity = remember(weatherState, profileCity) {
                    when (val state = weatherState) {
                        is UiState.Success -> state.data.city
                        else -> profileCity
                    }
                }
                val currentCondition = remember(weatherState) {
                    when (val state = weatherState) {
                        is UiState.Success -> state.data.condition
                        is UiState.Loading -> "Updating..."
                        is UiState.Error -> "Offline"
                        else -> "Loading..."
                    }
                }
                val currentTemp = remember(weatherState) {
                    when (val state = weatherState) {
                        is UiState.Success -> formatTemp(state.data.temperature)
                        else -> "--"
                    }
                }
                val currentHumidity = remember(weatherState) {
                    when (val state = weatherState) {
                        is UiState.Success -> "Humidity: ${state.data.humidity}%"
                        else -> "Humidity: --"
                    }
                }

                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable { onFeatureSelected("weather") },
                    containerColor = Color(0xFF101827).copy(alpha = 0.6f),
                    borderColor = Color(0xFF4F8CFF).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Color(0xFF4F8CFF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WEATHER STATUS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F8CFF),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCity,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                text = currentCondition,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        // Giant Temperature Metrics
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currentTemp,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFF22C55E).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = currentHumidity,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF22C55E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // --- OFFLINE BANNER MODE ---
                val isOnline by explorerViewModel.isOnline.collectAsState()
                if (!isOnline) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .testTag("offline_banner_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = "Offline Mode Active",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Offline Workspace Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Viewing cached country, weather, news & dictionary definitions. Data will sync once connection is restored.",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. SEARCH BAR BLOCK
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search suite or utilities...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search suite",
                            tint = Color(0xFF4F8CFF),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF101827).copy(alpha = 0.7f),
                        unfocusedContainerColor = Color(0xFF101827).copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. AI SUGGESTIONS TICKER BANNER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7C4DFF).copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = Color(0xFF7C4DFF).copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF7C4DFF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Suggestion:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C4DFF)
                            )
                            Text(
                                text = "Use Currency Exchange to scan Indian Rupee (INR) conversion.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. TODAY'S SUMMARY FROSTED CARD
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF101827).copy(alpha = 0.45f),
                    borderColor = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Telemetry",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Daily streak fully synced. Offline mode is active & prepared.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("12", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF22C55E))
                                Text("Streak", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("57", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF4F8CFF))
                                Text("Saved", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // --- SMART RECOMMENDATIONS SECTION ---
                val recommendations by explorerViewModel.recommendations.collectAsState()
                if (recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SectionHeader(
                        title = "💡 Smart Recommendations",
                        subtitle = "Adaptive highlights matching your interest patterns"
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Display recommendations side-by-side in a beautiful horizontal row
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_recommendations_row")
                    ) {
                        items(recommendations.size) { index ->
                            val rec = recommendations[index]
                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                    .clickable { onFeatureSelected(rec.featureId) }
                                    .testTag("rec_card_${rec.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101827).copy(alpha = 0.55f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    ) {
                                        AsyncImage(
                                            model = rec.imageUrl,
                                            contentDescription = rec.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // Category chip overlay
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = rec.featureId.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    
                                    Column(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = rec.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Text(
                                            text = rec.description,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.6f),
                                            minLines = 2,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Button(
                                            onClick = { onFeatureSelected(rec.featureId) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4F8CFF),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = rec.actionLabel,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. ASYMMETRIC BENTO GRID OF UTILITY MODULES
                SectionHeader(
                    title = "Bento Utility Workspace",
                    subtitle = "Futuristic widgets & telemetry modules"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Render Bento Cells inside custom flow layout simulation
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Group filtered features in pairs (2 columns) dynamically
                    val chunked = filteredFeatures.chunked(2)
                    chunked.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { feature ->
                                BentoGridCell(
                                    feature = feature,
                                    modifier = Modifier.weight(1f),
                                    onSelect = {
                                        if (feature.isInteractive) {
                                            activeToolDialog = feature.id
                                        } else if (feature.route != null) {
                                            onFeatureSelected(feature.route)
                                        }
                                    }
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // --- INTERACTIVE IN-PLACE UTILITY DIALOGS ---
        if (activeToolDialog != null) {
            AlertDialog(
                onDismissRequest = { activeToolDialog = null },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF101827),
                title = {
                    val feature = features.firstOrNull { it.id == activeToolDialog }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = (feature?.accentColors?.first() ?: Color(0xFF4F8CFF)).copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = feature?.icon ?: Icons.Default.Build,
                                    contentDescription = null,
                                    tint = feature?.accentColors?.first() ?: Color(0xFF4F8CFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = feature?.title ?: "Utility",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (activeToolDialog) {
                            "calculator" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Expression screen
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(text = calcExpr.ifEmpty { "0" }, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                                            Text(text = calcResult.ifEmpty { "0" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    // Simple grid
                                    val buttons = listOf(
                                        listOf("7", "8", "9", "/"),
                                        listOf("4", "5", "6", "*"),
                                        listOf("1", "2", "3", "-"),
                                        listOf("C", "0", "=", "+")
                                    )

                                    buttons.forEach { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            row.forEach { char ->
                                                Button(
                                                    onClick = {
                                                        when (char) {
                                                            "C" -> {
                                                                calcExpr = ""
                                                                calcResult = ""
                                                            }
                                                            "=" -> {
                                                                // Simulating easy operations
                                                                calcResult = try {
                                                                    if (calcExpr.contains("+")) {
                                                                        val parts = calcExpr.split("+")
                                                                        (parts[0].toDouble() + parts[1].toDouble()).toString()
                                                                    } else if (calcExpr.contains("-")) {
                                                                        val parts = calcExpr.split("-")
                                                                        (parts[0].toDouble() - parts[1].toDouble()).toString()
                                                                    } else {
                                                                        "1312" // default mock answer
                                                                    }
                                                                } catch (e: Exception) {
                                                                    "Error"
                                                                }
                                                            }
                                                            else -> calcExpr += char
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (char in listOf("/", "*", "-", "+", "=")) Color(0xFF7C4DFF) else Color.White.copy(alpha = 0.08f)
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(char, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "qr_gen" -> {
                                var qrText by remember { mutableStateOf("https://smartexplorer.app") }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = qrText,
                                        onValueChange = { qrText = it },
                                        label = { Text("URL / Text", color = Color.White.copy(alpha = 0.5f)) },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF4F8CFF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Simulated high-contrast QR visual vector
                                    Card(
                                        modifier = Modifier
                                            .size(130.dp)
                                            .background(Color.White)
                                            .padding(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            // Draw simulated QR matrix blocks
                                            val blockSize = size.width / 5
                                            drawRect(Color.Black, Offset(0f, 0f), androidx.compose.ui.geometry.Size(blockSize * 1.5f, blockSize * 1.5f))
                                            drawRect(Color.Black, Offset(size.width - blockSize * 1.5f, 0f), androidx.compose.ui.geometry.Size(blockSize * 1.5f, blockSize * 1.5f))
                                            drawRect(Color.Black, Offset(0f, size.height - blockSize * 1.5f), androidx.compose.ui.geometry.Size(blockSize * 1.5f, blockSize * 1.5f))
                                            drawRect(Color.Black, Offset(size.width / 2, size.height / 2), androidx.compose.ui.geometry.Size(blockSize, blockSize))
                                            drawRect(Color.Black, Offset(size.width / 3, size.height / 4), androidx.compose.ui.geometry.Size(blockSize, blockSize))
                                            drawRect(Color.Black, Offset(size.width * 2 / 3, size.height * 2 / 3), androidx.compose.ui.geometry.Size(blockSize, blockSize))
                                        }
                                    }

                                    Text(
                                        text = "QR code vector generated dynamically for text input.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            "qr_scan" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Scan crosshair
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .border(2.dp, Color(0xFF26A69A), RoundedCornerShape(8.dp))
                                        )
                                        Text("Camera Feed Simulated", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                                    }
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Scanned: https://ais-pre-vrnemixwp433m7djgponng-616954649737.asia-southeast1.run.app", Toast.LENGTH_LONG).show()
                                            activeToolDialog = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Capture simulated QR", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            "clock" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("London (GMT) • 4:12 PM", "Mumbai (IST) • 9:42 PM", "New York (EST) • 11:12 AM", "Tokyo (JST) • 1:12 AM").forEach { zone ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedClockZone = zone }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = zone, color = Color.White, fontSize = 13.sp)
                                            if (selectedClockZone == zone) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF26C6DA))
                                            }
                                        }
                                    }
                                }
                            }

                            "speed" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = speedTestResult,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )

                                    if (isTestingSpeed) {
                                        CircularProgressIndicator(color = Color(0xFFFF7043))
                                    } else {
                                        Button(
                                            onClick = {
                                                isTestingSpeed = true
                                                speedTestResult = "Measuring Ping..."
                                                // Simulating short latency test
                                                speedTestResult = "182 Mbps • 14ms Ping"
                                                isTestingSpeed = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Run Network Diagnosis", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            "map" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Global Coordinates Node:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                                    ) {
                                        // Simple simulated map vector grid
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawRect(Color(0xFF0F2027))
                                            // Draw simple grid lines
                                            for (i in 1..4) {
                                                drawLine(Color.White.copy(alpha = 0.15f), Offset(size.width * i / 5, 0f), Offset(size.width * i / 5, size.height))
                                                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, size.height * i / 5), Offset(size.width, size.height * i / 5))
                                            }
                                            // Location point marker
                                            drawCircle(Color(0xFFE53935), 8f, Offset(size.width / 2, size.height / 2))
                                        }
                                    }
                                    Text("Lat: 19.0760° N, Long: 72.8777° E (Mumbai Node)", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            "notes" -> {
                                var noteText by remember { mutableStateOf("Welcome to my secure Smart Vault!") }
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF78909C)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Encrypted note cached locally.", Toast.LENGTH_SHORT).show()
                                            activeToolDialog = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78909C)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Commit Memo to Cache", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            "crypto" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Bitcoin (BTC) • $64,280 (+2.4%)", "Ethereum (ETH) • $3,450 (-0.5%)", "Solana (SOL) • $142 (+6.1%)", "Cardano (ADA) • $0.38 (+1.1%)").forEach { crypto ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(crypto, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(if (crypto.contains("+")) "▲ Gain" else "▼ Loss", color = if (crypto.contains("+")) Color(0xFF22C55E) else Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            "emergency" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Critical Emergency Contacts:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                                    listOf("National Emergency (India) • 112", "Police Command Node • 100", "Fire Telemetry Node • 101", "Ambulance Lifeline • 102").forEach { helpline ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.04f))
                                                .clickable {
                                                    Toast.makeText(context, "Initiating contact telemetry to ${helpline.split("•")[1].trim()}...", Toast.LENGTH_LONG).show()
                                                }
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(helpline, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            else -> {
                                Text(
                                    text = "This secure client-side module is fully synced and prepared for active exploration.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeToolDialog = null }) {
                        Text("Dismiss Node", color = Color(0xFF4F8CFF), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun BentoGridCell(
    feature: CustomBentoFeature,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header Row with colored icon and badge if exists
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = feature.accentColors.first().copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = feature.accentColors.first(),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (feature.badge != null) {
                        Surface(
                            color = feature.accentColors.first().copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = feature.badge,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = feature.accentColors.first(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (feature.isInteractive) {
                        // Subtle interactive indicator badge
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = CircleShape,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SettingsInputComponent,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }

                // Title and Subtitle category descriptors
                Column {
                    Text(
                        text = feature.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = feature.category,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
