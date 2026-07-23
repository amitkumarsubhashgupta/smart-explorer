package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.viewmodel.ExplorerViewModel
import java.text.SimpleDateFormat
import java.util.*

data class WorldClockItem(
    val cityName: String,
    val timeZoneId: String,
    val flag: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var timeTicks by remember { mutableStateOf(System.currentTimeMillis()) }

    // Tick time every second
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            timeTicks = System.currentTimeMillis()
        }
    }

    val locations = remember {
        listOf(
            WorldClockItem("Mumbai", "Asia/Kolkata", "🇮🇳", "IST (UTC+5:30)"),
            WorldClockItem("London", "Europe/London", "🇬🇧", "GMT (UTC+0:00)"),
            WorldClockItem("New York", "America/New_York", "🇺🇸", "EST (UTC-5:00)"),
            WorldClockItem("Tokyo", "Asia/Tokyo", "🇯🇵", "JST (UTC+9:00)"),
            WorldClockItem("Sydney", "Australia/Sydney", "🇦🇺", "AEST (UTC+10:00)"),
            WorldClockItem("Zurich", "Europe/Zurich", "🇨🇭", "CET (UTC+1:00)")
        )
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("clock_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "World Clock",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(modifier = Modifier.size(48.dp)) // empty align placeholder
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(locations) { loc ->
                    val isFav by viewModel.isItemFavoriteFlow("WORLD_CLOCK", loc.cityName).collectAsState(initial = false)

                    // Formatter for timezone
                    val sdf = remember {
                        SimpleDateFormat("hh:mm:ss a", Locale.ROOT).apply {
                            timeZone = TimeZone.getTimeZone(loc.timeZoneId)
                        }
                    }
                    val formattedTime = sdf.format(Date(timeTicks))

                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(loc.flag, fontSize = 24.sp)
                                    Text(
                                        text = loc.cityName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = loc.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                IconButton(
                                    onClick = {
                                        viewModel.toggleFavorite(
                                            type = "WORLD_CLOCK",
                                            key = loc.cityName,
                                            title = "World Clock",
                                            subtitle = "${loc.flag} ${loc.cityName}",
                                            content = "Timezone: ${loc.timeZoneId}"
                                        )
                                        Toast.makeText(
                                            context,
                                            if (isFav) "Removed ${loc.cityName} Favorite" else "Saved ${loc.cityName} to Discovery Board",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.testTag("clock_fav_${loc.cityName}")
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFFFC107) else Color.White.copy(alpha = 0.4f)
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
