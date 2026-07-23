package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var queryCity by remember { mutableStateOf("Mumbai, India") }
    var currentCoords by remember { mutableStateOf("19.0760° N, 72.8777° E") }
    var signalStrength by remember { mutableStateOf("94% GPS Accuracy") }

    val isFav by viewModel.isItemFavoriteFlow("MAP", queryCity).collectAsState(initial = false)

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("map_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Explorer Map",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(
                            type = "MAP",
                            key = queryCity,
                            title = "Coordinates",
                            subtitle = queryCity,
                            content = currentCoords
                        )
                        Toast.makeText(
                            context,
                            if (isFav) "Removed destination" else "Saved destination to Discovery Board",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("map_fav_button")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFFFC107) else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search field
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = queryCity,
                            onValueChange = { queryCity = it },
                            placeholder = { Text("Search location coordinates...", color = Color.White.copy(alpha = 0.4f)) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("map_search_input")
                        )

                        IconButton(
                            onClick = {
                                val normalized = queryCity.trim().lowercase()
                                currentCoords = when {
                                    normalized.contains("london") -> "51.5074° N, 0.1278° W"
                                    normalized.contains("new york") -> "40.7128° N, 74.0060° W"
                                    normalized.contains("tokyo") -> "35.6762° N, 139.6503° E"
                                    normalized.contains("paris") -> "48.8566° N, 2.3522° E"
                                    else -> "${(10..60).random()}.${(1000..9999).random()}° N, ${(60..140).random()}.${(1000..9999).random()}° E"
                                }
                                signalStrength = "${(85..99).random()}% GPS Accuracy"
                                Toast.makeText(context, "Locking GPS node...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("map_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Simulated Map Canvas Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .background(Color(0xFF0D1117)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerW = size.width / 2f
                        val centerH = size.height / 2f

                        // Draw background grid lines (cyber style)
                        val gridSpace = 40.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height))
                            x += gridSpace
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(Color.White.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y))
                            y += gridSpace
                        }

                        // Draw concentric locator radar rings
                        drawCircle(
                            color = Color(0xFF2196F3).copy(alpha = 0.15f),
                            radius = 120.dp.toPx(),
                            center = Offset(centerW, centerH),
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = Color(0xFF2196F3).copy(alpha = 0.08f),
                            radius = 180.dp.toPx(),
                            center = Offset(centerW, centerH),
                            style = Stroke(width = 1.5f)
                        )

                        // Draw simulated shoreline vectors or nodes
                        drawCircle(
                            color = Color(0xFF00E676).copy(alpha = 0.3f),
                            radius = 12.dp.toPx(),
                            center = Offset(centerW - 50.dp.toPx(), centerH + 40.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            radius = 24.dp.toPx(),
                            center = Offset(centerW + 80.dp.toPx(), centerH - 70.dp.toPx())
                        )

                        // Central Pin crosshair
                        drawLine(Color(0xFF2196F3), Offset(centerW - 30.dp.toPx(), centerH), Offset(centerW + 30.dp.toPx(), centerH), strokeWidth = 3f)
                        drawLine(Color(0xFF2196F3), Offset(centerW, centerH - 30.dp.toPx()), Offset(centerW, centerH + 30.dp.toPx()), strokeWidth = 3f)
                        drawCircle(Color(0xFFEF5350), radius = 6.dp.toPx(), center = Offset(centerW, centerH))
                    }

                    // Radar Overlay Text
                    Text(
                        text = "SECURE POSITIONING GRID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3).copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )

                    Text(
                        text = "NODE TELEMETRY SECURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676).copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }

                // Telemetry facts card
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ACTIVE COORDINATES", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(currentCoords, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = signalStrength,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
