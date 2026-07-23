package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FavoriteItem
import com.example.ui.components.*
import com.example.ui.viewmodel.ExplorerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    viewModel: ExplorerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val favoritesList by viewModel.favorites.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "WEATHER", "COUNTRY", "NEWS", "JOKE", "TRANSLATION", "DICTIONARY")

    val filteredFavorites = remember(favoritesList, selectedTypeFilter) {
        if (selectedTypeFilter == "All") favoritesList
        else favoritesList.filter { it.type == selectedTypeFilter }
    }

    PremiumGradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "Discovery Board",
                subtitle = "Manage your cached tools and favorite findings"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // HORIZONTAL TYPE CHIPS
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { f ->
                    val isSelected = selectedTypeFilter == f
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = f },
                        label = { Text(if (f == "All") "All Saved" else f) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("fav_chip_$f")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FAVORITES CONTAINER
            if (filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.BookmarkBorder,
                        title = "Your board is empty",
                        description = if (selectedTypeFilter == "All") "Browse the Weather, Countries, News, Dictionary, and Translator modules and click the Favorite Star icon to build your custom feed." else "No favorite items saved for the '$selectedTypeFilter' utility yet."
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("favorites_list")
                ) {
                    items(filteredFavorites, key = { it.id }) { fav ->
                        FavoriteItemRow(
                            item = fav,
                            onDelete = {
                                viewModel.removeFavoriteById(fav.id)
                                Toast.makeText(context, "Removed from Discovery Board", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemRow(
    item: FavoriteItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category emblem
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = when (item.type) {
                        "WEATHER" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                        "COUNTRY" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        "NEWS" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        "JOKE" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                        "TRANSLATION" -> Color(0xFF009688).copy(alpha = 0.15f)
                        else -> Color(0xFF795548).copy(alpha = 0.15f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (item.type) {
                                "WEATHER" -> Icons.Default.Cloud
                                "COUNTRY" -> Icons.Default.Public
                                "NEWS" -> Icons.Default.Newspaper
                                "JOKE" -> Icons.Default.SentimentVerySatisfied
                                "TRANSLATION" -> Icons.Default.Translate
                                else -> Icons.Default.Book
                            },
                            contentDescription = item.type,
                            tint = when (item.type) {
                                "WEATHER" -> Color(0xFF2196F3)
                                "COUNTRY" -> Color(0xFF4CAF50)
                                "NEWS" -> Color(0xFFFF9800)
                                "JOKE" -> Color(0xFFE91E63)
                                "TRANSLATION" -> Color(0xFF009688)
                                else -> Color(0xFF795548)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Detail text
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Mini category pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.height(16.dp)
                        ) {
                            Text(
                                text = item.type,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_fav_button_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
