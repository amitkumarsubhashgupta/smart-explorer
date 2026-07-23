package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DictionaryWord
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SmartSearchBar
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var wordQuery by remember { mutableStateOf("explorer") }
    val dictionaryState by viewModel.dictionaryState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.lookupWord("explorer")
    }

    val currentWord = (dictionaryState as? UiState.Success)?.data?.word ?: ""
    val isFav by viewModel.isItemFavoriteFlow("DICTIONARY", currentWord).collectAsState(initial = false)

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("dictionary_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Word Lexicon",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        if (currentWord.isNotEmpty()) {
                            val data = (dictionaryState as UiState.Success).data
                            viewModel.toggleFavorite(
                                type = "DICTIONARY",
                                key = data.word,
                                title = "Word: ${data.word}",
                                subtitle = data.phonetic,
                                content = "Def: ${data.definition}"
                            )
                            Toast.makeText(
                                context,
                                if (isFav) "Removed word" else "Saved word to Favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = dictionaryState is UiState.Success,
                    modifier = Modifier.testTag("dictionary_fav_button")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFFFC107) else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SEARCH BAR
            SmartSearchBar(
                query = wordQuery,
                onQueryChange = {
                    wordQuery = it
                    viewModel.lookupWord(it)
                },
                placeholder = "Search dictionary (e.g. curiosity, cosmic...)"
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (val state = dictionaryState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Success -> {
                    val dict = state.data
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // WORD BANNER CARD WITH PHONETICS
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = dict.word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dict.phonetic,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(dict.partOfSpeech) },
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }

                                FloatingActionButton(
                                    onClick = {
                                        Toast.makeText(context, "Phonetics speech playback simulated.", Toast.LENGTH_SHORT).show()
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.VolumeUp,
                                        contentDescription = "Pronounce",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // DEFINITION CARD
                        Text(
                            text = "Definition",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = dict.definition,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // CONTEXTUAL EXAMPLE BLOCK
                        if (dict.example.isNotEmpty()) {
                            Text(
                                text = "Example Usage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "\"${dict.example}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // SYNONYMS BLOCK
                        if (dict.synonyms.isNotEmpty()) {
                            Text(
                                text = "Synonyms",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dict.synonyms.forEach { syn ->
                                    SuggestionChip(
                                        onClick = {
                                            wordQuery = syn
                                            viewModel.lookupWord(syn)
                                        },
                                        label = { Text(syn) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Word not found in dictionary", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {}
            }
        }
    }
}
