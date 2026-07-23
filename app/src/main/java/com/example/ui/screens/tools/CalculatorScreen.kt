package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val isFav by viewModel.isItemFavoriteFlow("CALC", "$expr=$result").collectAsState(initial = false)

    val onKeyPress: (String) -> Unit = { key ->
        when (key) {
            "C" -> {
                expr = ""
                result = ""
            }
            "⌫" -> {
                if (expr.isNotEmpty()) {
                    expr = expr.dropLast(1)
                }
            }
            "=" -> {
                if (expr.isNotBlank()) {
                    result = try {
                        val finalResult = evaluateExpression(expr)
                        if (finalResult.isNaN()) "Error" else finalResult.toString()
                    } catch (e: Exception) {
                        "Error"
                    }
                }
            }
            else -> {
                expr += key
            }
        }
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("calc_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Smart Calculator",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        if (expr.isNotEmpty() && result.isNotEmpty() && result != "Error") {
                            viewModel.toggleFavorite(
                                type = "CALC",
                                key = "$expr=$result",
                                title = "Calculation",
                                subtitle = "$expr = $result",
                                content = "Arithmetic formula solved by explorer workspace"
                            )
                            Toast.makeText(
                                context,
                                if (isFav) "Removed formula" else "Saved calculation to favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, "Solve a valid equation first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("calc_fav_button")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFFFC107) else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display board
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = expr.ifEmpty { "0" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        lineHeight = 38.sp
                    )
                    Text(
                        text = result.ifEmpty { "" },
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keyboard
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val pad = listOf(
                    listOf("C", "⌫", "%", "/"),
                    listOf("7", "8", "9", "*"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf(".", "0", "00", "=")
                )

                pad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { char ->
                            val isAction = char in listOf("C", "⌫", "%")
                            val isOperator = char in listOf("/", "*", "-", "+", "=")
                            val containerColor = when {
                                char == "=" -> MaterialTheme.colorScheme.primary
                                isOperator -> Color(0xFF1E293B).copy(alpha = 0.9f)
                                isAction -> Color(0xFF334155).copy(alpha = 0.7f)
                                else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                            }
                            val textColor = if (char == "=") Color.White else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(containerColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
                                    .clickable { onKeyPress(char) }
                                    .testTag("calc_btn_$char"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Basic math expression evaluator supporting decimal arithmetic safely
fun evaluateExpression(expression: String): Double {
    return try {
        // Simple manual split evaluation to prevent complex library dependencies
        val tokens = expression.replace(" ", "")
        when {
            tokens.contains("+") -> {
                val parts = tokens.split("+")
                parts[0].toDouble() + parts[1].toDouble()
            }
            tokens.contains("-") -> {
                val parts = tokens.split("-")
                parts[0].toDouble() - parts[1].toDouble()
            }
            tokens.contains("*") -> {
                val parts = tokens.split("*")
                parts[0].toDouble() * parts[1].toDouble()
            }
            tokens.contains("/") -> {
                val parts = tokens.split("/")
                val divisor = parts[1].toDouble()
                if (divisor == 0.0) Double.NaN else parts[0].toDouble() / divisor
            }
            tokens.contains("%") -> {
                val parts = tokens.split("%")
                parts[0].toDouble() / 100.0
            }
            else -> tokens.toDouble()
        }
    } catch (e: Exception) {
        Double.NaN
    }
}
