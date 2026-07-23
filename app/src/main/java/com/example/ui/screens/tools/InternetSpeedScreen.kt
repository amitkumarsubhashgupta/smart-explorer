package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.viewmodel.ExplorerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternetSpeedScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isTesting by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(0.0) }
    var pingValue by remember { mutableStateOf(12) }
    var downloadSpeed by remember { mutableStateOf(0.0) }
    var uploadSpeed by remember { mutableStateOf(0.0) }
    var testComplete by remember { mutableStateOf(false) }

    val isFav by viewModel.isItemFavoriteFlow("SPEED", "$downloadSpeed Mbps").collectAsState(initial = false)

    // Animated speedometer needle progress
    val needleProgress by animateFloatAsState(
        targetValue = if (isTesting) currentSpeed.toFloat() else downloadSpeed.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "needle"
    )

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("speed_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Internet Speed Test",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        if (testComplete) {
                            viewModel.toggleFavorite(
                                type = "SPEED",
                                key = "$downloadSpeed Mbps",
                                title = "Speed Metrics",
                                subtitle = "Download: $downloadSpeed Mbps • Upload: $uploadSpeed Mbps",
                                content = "Tested ISP server: Jio GigaFiber Mumbai Node"
                            )
                            Toast.makeText(
                                context,
                                if (isFav) "Removed speed record" else "Saved metrics to discovery board",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = testComplete,
                    modifier = Modifier.testTag("speed_fav_button")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFFFC107) else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // DIAL CONTAINER
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(3.dp, Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFF00BCD4))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format("%.1f", needleProgress),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Mbps",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // SUBMETRICS CARD
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PING", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            Text("${pingValue} ms", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DOWNLOAD", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", downloadSpeed)} Mbps", fontSize = 18.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UPLOAD", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", uploadSpeed)} Mbps", fontSize = 18.sp, color = Color(0xFF29B6F6), fontWeight = FontWeight.Black)
                        }
                    }
                }

                // ISP Server Info
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = Color(0xFF29B6F6),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Connected ISP Server Node",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                "Jio GigaFiber Broadband • Mumbai",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        isTesting = true
                        testComplete = false
                        downloadSpeed = 0.0
                        uploadSpeed = 0.0
                        pingValue = (8..18).random()

                        // Simulate speed testing stages
                        kotlin.concurrent.thread {
                            // Phase 1: Download
                            for (i in 0..10) {
                                Thread.sleep(150)
                                currentSpeed = 50.0 + (0..150).random().toDouble()
                            }
                            downloadSpeed = currentSpeed

                            // Phase 2: Upload
                            for (i in 0..10) {
                                Thread.sleep(150)
                                currentSpeed = 10.0 + (0..60).random().toDouble()
                            }
                            uploadSpeed = currentSpeed
                            currentSpeed = 0.0
                            isTesting = false
                            testComplete = true
                        }
                    },
                    enabled = !isTesting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("speed_test_button")
                ) {
                    Text(
                        text = if (isTesting) "Running Speed Diagnostics..." else "Initialize Telemetry Test",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
