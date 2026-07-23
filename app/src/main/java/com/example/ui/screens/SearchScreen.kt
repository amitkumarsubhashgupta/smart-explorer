package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryData
import com.example.data.model.DictionaryWord
import com.example.data.model.WeatherData
import com.example.ui.components.*
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ExplorerViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchUiQuery.collectAsState()
    val searchResults by viewModel.unifiedSearchState.collectAsState()

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "Unified Explorer",
                subtitle = "Query Weather, Countries, and Lexicon in parallel"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // UNIFIED SEARCH INPUT
            SmartSearchBar(
                query = query,
                onQueryChange = { viewModel.performUnifiedSearch(it) },
                placeholder = "Type city, country, or English word..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // RESULTS LAYOUT
            if (query.isBlank()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.YoutubeSearchedFor,
                        title = "Unleash Parallel Queries",
                        description = "Type a term (e.g. 'London', 'Japan', 'Explorer') to see live Weather, geographical data, and dictionary results populate in real-time."
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // WEATHER SECTION MATCH
                    val weatherRes = searchResults["WEATHER"]
                    if (weatherRes is UiState.Success) {
                        val w = weatherRes.data as WeatherData
                        Text("Weather Matches", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(w.city, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${w.temperature}°C - ${w.condition}", style = MaterialTheme.typography.bodySmall)
                                }
                                Icon(
                                    imageVector = when (w.condition) {
                                        "Sunny" -> Icons.Default.WbSunny
                                        "Rainy" -> Icons.Default.WaterDrop
                                        else -> Icons.Default.Cloud
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // COUNTRY SECTION MATCH
                    val countryRes = searchResults["COUNTRY"]
                    if (countryRes is UiState.Success) {
                        val c = countryRes.data as CountryData
                        Text("Country Matches", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${c.flagEmoji} ${c.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Capital: ${c.capital} | Population: ${c.population}", style = MaterialTheme.typography.bodySmall)
                                }
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // DICTIONARY SECTION MATCH
                    val dictRes = searchResults["DICTIONARY"]
                    if (dictRes is UiState.Success) {
                        val d = dictRes.data as DictionaryWord
                        Text("Word Matches", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(d.word, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(d.phonetic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(d.definition, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }

                    // Loading indicator if everything is empty
                    if (searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
