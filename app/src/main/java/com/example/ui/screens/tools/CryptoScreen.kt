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

data class CryptoTickerItem(
    val symbol: String,
    val name: String,
    val price: Double,
    val pctChange: Double,
    val graphSeeds: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }

    // Generates static random trend lines for aesthetic visual appeal
    val coins = remember(isRefreshing) {
        listOf(
            CryptoTickerItem("BTC", "Bitcoin", 64280.0 + Random.nextDouble(-100.0, 300.0), 2.45, listOf(12f, 18f, 15f, 24f, 21f, 30f)),
            CryptoTickerItem("ETH", "Ethereum", 3450.0 + Random.nextDouble(-10.0, 30.0), -0.52, listOf(22f, 20f, 18f, 19f, 16f, 15f)),
            CryptoTickerItem("SOL", "Solana", 142.5 + Random.nextDouble(-2.0, 5.0), 6.12, listOf(5f, 11f, 14f, 19f, 18f, 26f)),
            CryptoTickerItem("ADA", "Cardano", 0.38 + Random.nextDouble(-0.01, 0.02), 1.15, listOf(8f, 9f, 8f, 10f, 9f, 11f)),
            CryptoTickerItem("XRP", "Ripple", 0.58 + Random.nextDouble(-0.02, 0.01), -1.82, listOf(18f, 17f, 15f, 14f, 15f, 13f))
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("crypto_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Crypto Tickers",
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
                    modifier = Modifier.testTag("crypto_refresh")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Feed",
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
                items(coins) { coin ->
                    val isFav by viewModel.isItemFavoriteFlow("CRYPTO", coin.symbol).collectAsState(initial = false)

                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(0.35f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = coin.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = coin.symbol,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Dynamic sparkline canvas
                            Box(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .height(36.dp)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val points = coin.graphSeeds
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
                                            color = if (coin.pctChange >= 0) Color(0xFF00E676) else Color(0xFFFF1744),
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
                                    text = String.format("$%,.2f", coin.price),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (coin.pctChange >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (coin.pctChange >= 0) Color(0xFF00E676) else Color(0xFFFF1744),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = String.format("%+.2f%%", coin.pctChange),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (coin.pctChange >= 0) Color(0xFF00E676) else Color(0xFFFF1744)
                                    )

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleFavorite(
                                                type = "CRYPTO",
                                                key = coin.symbol,
                                                title = "Crypto Asset",
                                                subtitle = "${coin.name} (${coin.symbol})",
                                                content = "Locked rate: $${String.format("%,.2f", coin.price)}"
                                            )
                                            Toast.makeText(
                                                context,
                                                if (isFav) "Removed crypto preference" else "Saved crypto preference to Board",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.size(24.dp).testTag("crypto_fav_${coin.symbol}")
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
