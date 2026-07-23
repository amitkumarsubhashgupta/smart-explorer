package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SectionHeader

data class ChatMessage(val content: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Chat, 1: Summarize, 2: Utilities
    
    // AI Chat states
    var chatInput by remember { mutableStateOf("") }
    val chatHistory = remember {
        mutableStateListOf(
            ChatMessage("Hello explorer! I am your advanced AI Navigator. Ask me anything about world geography, currencies, real-time facts, or tools.", false)
        )
    }

    // Summarizer states
    var textToSummarize by remember { mutableStateOf("") }
    var summaryResult by remember { mutableStateOf("") }
    var isSummarizing by remember { mutableStateOf(false) }

    // TTS/STT Simulation
    var ttsText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(
                title = "AI Core Engine",
                subtitle = "Cognitive assistance & summarization tools"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // TAB SWITCHER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101827).copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Assistant", "Summarizer", "Cognitive").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(Brush.linearGradient(listOf(Color(0xFF4F8CFF), Color(0xFF7C4DFF))))
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB CONTENTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> { // ASSISTANT CHAT
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Message logs
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFF101827).copy(alpha = 0.4f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                    .padding(12.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    reverseLayout = false
                                ) {
                                    items(chatHistory) { message ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                                                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                                                ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (message.isUser) Color(0xFF4F8CFF) else Color(0xFF1E293B)
                                                ),
                                                modifier = Modifier.widthIn(max = 280.dp)
                                            ) {
                                                Text(
                                                    text = message.content,
                                                    modifier = Modifier.padding(12.dp),
                                                    color = Color.White,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Send row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    placeholder = { Text("Ask anything...", color = Color.White.copy(alpha = 0.4f)) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF101827).copy(alpha = 0.8f),
                                        unfocusedContainerColor = Color(0xFF101827).copy(alpha = 0.8f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        if (chatInput.isNotBlank()) {
                                            val query = chatInput
                                            chatHistory.add(ChatMessage(query, true))
                                            chatInput = ""

                                            // Simulating cognitive dynamic responses matching user query
                                            val reply = when {
                                                query.contains("weather", ignoreCase = true) -> "Scanning meteorology vectors: London current temp is 24°C, Clear Skies. Atmospheric pressure stable."
                                                query.contains("country", ignoreCase = true) || query.contains("india", ignoreCase = true) -> "Retrieved Country metrics: India is in South Asia, population 1.4B, capital New Delhi. Currency: INR."
                                                query.contains("crypto", ignoreCase = true) -> "Dynamic Ticker Scan: Bitcoin (BTC) trading at $64,280 (+2.4%). Ethereum (ETH) at $3,450."
                                                query.contains("notes", ignoreCase = true) -> "Smart Note vaults are fully encrypted. Tap the Notes widget on your Home workspace to record new memos."
                                                else -> "Analytical core scanned: \"$query\". Integrating regional databases. Complete telemetry is optimized and running safely."
                                            }
                                            chatHistory.add(ChatMessage(reply, false))
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Brush.linearGradient(listOf(Color(0xFF4F8CFF), Color(0xFF7C4DFF))), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send Message",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    1 -> { // SUMMARIZER TOOL
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color(0xFF101827).copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "Text Summarizer Core",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = textToSummarize,
                                    onValueChange = { textToSummarize = it },
                                    placeholder = { Text("Paste long articles, documents, or logs to boil down instantly...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF4F8CFF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (textToSummarize.isNotBlank()) {
                                            isSummarizing = true
                                            summaryResult = "AI Summary Vector:\n• Core Focus: Cognitive workspace utilities verified.\n• Key Metrics: Streamlined system processes, reduced administrative latency.\n• Conclusion: High efficiency achieved with local encryption constraints."
                                            isSummarizing = false
                                        } else {
                                            Toast.makeText(context, "Please enter some text to summarize", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F8CFF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Summarize with AI Core", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (summaryResult.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101827).copy(alpha = 0.8f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Synthesis Completed Successfully",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF22C55E)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = summaryResult,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // COGNITIVE UTILITIES
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // OCR (Image to text)
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color(0xFF101827).copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Image to Text (OCR)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Scan receipts, documents & labels",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Launching secure optical lens scan...", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF).copy(alpha = 0.2f), contentColor = Color(0xFF7C4DFF))
                                    ) {
                                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "OCR Lens", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Speech to Text (STT) & Text to Speech (TTS)
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color(0xFF101827).copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "Speech Core Utilities",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Voice input STT
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                isRecording = !isRecording
                                                if (isRecording) {
                                                    Toast.makeText(context, "Listening carefully...", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    chatInput = "Hello from vocal synthesis core!"
                                                    selectedTab = 0
                                                    Toast.makeText(context, "Voice transcript integrated", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isRecording) Color(0xFFEF4444).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f)
                                        ),
                                        border = BorderStroke(1.dp, if (isRecording) Color(0xFFEF4444) else Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                                                contentDescription = null,
                                                tint = if (isRecording) Color(0xFFEF4444) else Color(0xFF4F8CFF),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (isRecording) "Stop Recording" else "Vocal Input (STT)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    // Voice reading TTS
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                Toast.makeText(context, "Vocal synthesizer playing: 'Smart Explorer AI systems are fully online.'", Toast.LENGTH_LONG).show()
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                tint = Color(0xFF7C4DFF),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Read Aloud (TTS)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            // PDF Tools
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color(0xFF101827).copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Document & PDF Tools",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Convert, merge, compress, or sign PDF files",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "PDF vector console fully prepared.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF Tools", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
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
