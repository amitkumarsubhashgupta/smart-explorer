package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyRate
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currenciesState by viewModel.currenciesState.collectAsState()

    val sourceAmount by viewModel.sourceAmount.collectAsState()
    val sourceCurrency by viewModel.sourceCurrency.collectAsState()
    val targetCurrency by viewModel.targetCurrency.collectAsState()
    val convertedAmount by viewModel.convertedAmount.collectAsState()

    var activeDropdown by remember { mutableStateOf<String?>(null) } // "source" or "target" or null

    LaunchedEffect(Unit) {
        viewModel.loadCurrencies()
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("currency_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Currency Exchange",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        // Swap currencies
                        val temp = sourceCurrency
                        viewModel.onSourceCurrencyChanged(targetCurrency)
                        viewModel.onTargetCurrencyChanged(temp)
                        Toast.makeText(context, "Swapped currency pairs", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("currency_swap_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (val state = currenciesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Success -> {
                    val rates = state.data
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // CONVERSION CARD
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // SOURCE AMOUNT INPUT
                                OutlinedTextField(
                                    value = sourceAmount,
                                    onValueChange = { viewModel.onSourceAmountChanged(it) },
                                    label = { Text("Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    leadingIcon = {
                                        Text(
                                            text = rates.find { it.code == sourceCurrency }?.flag ?: "💵",
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        Button(
                                            onClick = { activeDropdown = "source" },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(sourceCurrency, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("source_amount_input")
                                )

                                // EXPOSING SELECTOR AS POPUP
                                if (activeDropdown == "source") {
                                    CurrencySelectorList(
                                        rates = rates,
                                        selected = sourceCurrency,
                                        onSelected = {
                                            viewModel.onSourceCurrencyChanged(it)
                                            activeDropdown = null
                                        }
                                    )
                                }

                                // DIVIDER OR SWAP ICON
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.SwapVert,
                                                contentDescription = "Swap",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // TARGET CONVERTED AMOUNT DISPLAY
                                OutlinedTextField(
                                    value = convertedAmount,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Converted Result") },
                                    leadingIcon = {
                                        Text(
                                            text = rates.find { it.code == targetCurrency }?.flag ?: "💵",
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        Button(
                                            onClick = { activeDropdown = "target" },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(targetCurrency, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("converted_amount_display")
                                )

                                if (activeDropdown == "target") {
                                    CurrencySelectorList(
                                        rates = rates,
                                        selected = targetCurrency,
                                        onSelected = {
                                            viewModel.onTargetCurrencyChanged(it)
                                            activeDropdown = null
                                        }
                                    )
                                }
                            }
                        }

                        // EXCHANGE RATE CARD
                        val sourceSymbol = rates.find { it.code == sourceCurrency }?.flag ?: ""
                        val targetSymbol = rates.find { it.code == targetCurrency }?.flag ?: ""
                        
                        // Computing multiplier
                        val srcRateVal = rates.find { it.code == sourceCurrency }?.rateToUSD ?: 1.0
                        val tgtRateVal = rates.find { it.code == targetCurrency }?.rateToUSD ?: 1.0
                        val exchangeMultiplier = tgtRateVal / srcRateVal

                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Base Converter Reference",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "1 $sourceCurrency = ${String.format(java.util.Locale.ROOT, "%.4f", exchangeMultiplier)} $targetCurrency",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$sourceSymbol ➔ $targetSymbol", fontSize = 16.sp)
                                    }
                                }
                            }
                        }

                        // ALL CURRENT BASE USD EXCHANGE RATES TABLE
                        Text(
                            text = "Reference Rates (Base: 1 USD)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(rates) { rate ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.width(120.dp).padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(rate.flag, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(rate.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${rate.rateToUSD}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Failed to load exchange data", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CurrencySelectorList(
    rates: List<CurrencyRate>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            rates.forEach { rate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(rate.code) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(rate.flag, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "${rate.code} - ${rate.name}", fontWeight = FontWeight.Bold)
                    }
                    if (rate.code == selected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
