package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.viewmodel.ExplorerViewModel
import kotlin.random.Random

data class CommodityItem(
    val id: String,
    val name: String,
    val price: String,
    val change: String,
    val isUp: Boolean,
    val sparkline: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommodityPricesScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }

    val commodities = remember(isRefreshing) {
        listOf(
            CommodityItem("gold", "Gold • 24K (per 10g)", "₹72,450", "+₹420 (+0.58%)", true, listOf(10f, 12f, 15f, 14f, 18f, 21f)),
            CommodityItem("silver", "Silver • 1kg", "₹89,200", "-₹1,150 (-1.27%)", false, listOf(30f, 28f, 24f, 26f, 22f, 20f)),
            CommodityItem("platinum", "Platinum • 1oz", "$985.40", "+$12.50 (+1.28%)", true, listOf(5f, 8f, 11f, 9f, 14f, 16f)),
            CommodityItem("crude_oil", "WTI Crude Oil (barrel)", "$81.45", "-$0.38 (-0.46%)", false, listOf(24f, 23f, 21f, 22f, 21f, 20f)),
            CommodityItem("brent_crude", "Brent Crude (barrel)", "$85.12", "+$0.12 (+0.14%)", true, listOf(12f, 11f, 13f, 14f, 12f, 15f)),
            CommodityItem("natural_gas", "Natural Gas (MMBtu)", "$2.68", "+$0.14 (+5.51%)", true, listOf(2f, 4f, 3f, 6f, 8f, 11f))
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("commodity_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Commodity Prices",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        isRefreshing = true
                        kotlin.concurrent.thread {
                            Thread.sleep(600)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.testTag("commodity_refresh")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(commodities) { item ->
                    val isFav by viewModel.isItemFavoriteFlow("COMMODITY", item.id).collectAsState(initial = false)

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(0.4f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (item.isUp) "▲ Bullish" else "▼ Bearish",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isUp) Color(0xFF00E676) else Color(0xFFFF1744)
                                )
                            }

                            // Dynamic sparkline canvas
                            Box(
                                modifier = Modifier
                                    .weight(0.25f)
                                    .height(30.dp)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val points = item.sparkline
                                    val widthInterval = size.width / (points.size - 1)
                                    val maxVal = points.maxOrNull() ?: 1f
                                    val minVal = points.minOrNull() ?: 0f
                                    val range = maxVal - minVal

                                    for (i in 0 until points.size - 1) {
                                        val startX = i * widthInterval
                                        val startY = size.height - ((points[i] - minVal) / range) * size.height
                                        val endX = (i + 1) * widthInterval
                                        val endY = size.height - ((points[i + 1] - minVal) / range) * size.height

                                        drawLine(
                                            color = if (item.isUp) Color(0xFF00E676) else Color(0xFFFF1744),
                                            start = Offset(startX, startY),
                                            end = Offset(endX, endY),
                                            strokeWidth = 3f,
                                            // styled
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(0.35f),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.price,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.change,
                                        fontSize = 11.sp,
                                        color = if (item.isUp) Color(0xFF00E676) else Color(0xFFFF1744),
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleFavorite(
                                                type = "COMMODITY",
                                                key = item.id,
                                                title = "Commodity Price",
                                                subtitle = item.name,
                                                content = "Rate: ${item.price} (${item.change})"
                                            )
                                            Toast.makeText(
                                                context,
                                                if (isFav) "Removed commodity bookmark" else "Saved commodity bookmark to Board",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.size(24.dp).testTag("commodity_fav_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFav) Color(0xFFFFC107) else Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(16.dp)
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
