package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherData
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGradientBackground
import com.example.ui.components.SmartSearchBar
import com.example.ui.components.ErrorState
import com.example.ui.components.ShimmerCardList
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: ExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var cityQuery by remember { mutableStateOf("London") }
    val weatherState by viewModel.weatherState.collectAsState()

    val weatherUnit by viewModel.weatherUnitState.collectAsState()

    val formatTemp = remember(weatherUnit) {
        { tempCelsius: Double ->
            if (weatherUnit == "Fahrenheit") {
                val f = (tempCelsius * 9 / 5) + 32
                String.format(java.util.Locale.ROOT, "%.1f°F", f)
            } else {
                String.format(java.util.Locale.ROOT, "%.1f°C", tempCelsius)
            }
        }
    }

    val formatWind = remember(weatherUnit) {
        { speedKmh: Double ->
            if (weatherUnit == "Fahrenheit") {
                val mph = speedKmh * 0.621371
                String.format(java.util.Locale.ROOT, "%.1f mph", mph)
            } else {
                String.format(java.util.Locale.ROOT, "%.1f km/h", speedKmh)
            }
        }
    }

    // Query London by default on enter
    LaunchedEffect(Unit) {
        viewModel.searchWeather("London")
    }

    // Reactively observe favorite status of current city
    val currentCity = (weatherState as? UiState.Success)?.data?.city ?: ""
    val isFav by viewModel.isItemFavoriteFlow("WEATHER", currentCity).collectAsState(initial = false)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("weather_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("weather_menu_drawer_button")) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Text(
                    text = "Weather Radar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = {
                        if (currentCity.isNotEmpty()) {
                            val data = (weatherState as UiState.Success).data
                            viewModel.toggleFavorite(
                                type = "WEATHER",
                                key = data.city,
                                title = "${data.city} Weather",
                                subtitle = "${formatTemp(data.temperature)}, ${data.condition}",
                                content = "${formatTemp(data.temperature)} | Humid: ${data.humidity}%"
                            )
                            Toast.makeText(
                                context,
                                if (isFav) "Removed from Favorites" else "Saved to Favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = weatherState is UiState.Success,
                    modifier = Modifier.testTag("weather_fav_button")
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
                query = cityQuery,
                onQueryChange = {
                    cityQuery = it
                    viewModel.searchWeather(it)
                },
                placeholder = "Search city (e.g. New York, Tokyo...)"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RENDER BASED ON STATE
            when (val state = weatherState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShimmerCardList(count = 2)
                    }
                }
                is UiState.Success -> {
                    val weather = state.data
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // CURRENT TEMP DISPLAY
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = weather.city,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (weather.condition) {
                                            "Sunny" -> Icons.Default.WbSunny
                                            "Partly Cloudy" -> Icons.Default.CloudQueue
                                            "Cloudy" -> Icons.Default.Cloud
                                            "Rainy" -> Icons.Default.WaterDrop
                                            else -> Icons.Default.Grain
                                        },
                                        contentDescription = weather.condition,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = weather.condition,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = formatTemp(weather.temperature),
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Feels like ${formatTemp(weather.feelsLike)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // WEATHER DETAILS GRID
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WeatherDetailCell(
                                icon = Icons.Default.Opacity,
                                label = "Humidity",
                                value = "${weather.humidity}%",
                                modifier = Modifier.weight(1f)
                            )
                            WeatherDetailCell(
                                icon = Icons.Default.Air,
                                label = "Wind",
                                value = formatWind(weather.windSpeed),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WeatherDetailCell(
                                icon = Icons.Default.Compress,
                                label = "Pressure",
                                value = "${weather.pressure} hPa",
                                modifier = Modifier.weight(1f)
                            )
                            WeatherDetailCell(
                                icon = Icons.Default.DeviceThermostat,
                                label = "UV Index",
                                value = "${weather.uvIndex}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 5-DAY FORECAST SECTION
                        Text(
                            text = "5-Day Forecast",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(weather.forecast) { day ->
                                ForecastCell(day = day, weatherUnit = weatherUnit)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        modifier = Modifier.weight(1f),
                        onRetry = {
                            val city = cityQuery.ifBlank { "New York" }
                            viewModel.searchWeather(city)
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun WeatherDetailCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ForecastCell(
    day: com.example.data.model.ForecastDay,
    weatherUnit: String,
    modifier: Modifier = Modifier
) {
    val tempText = remember(day.temperature, weatherUnit) {
        if (weatherUnit == "Fahrenheit") {
            val f = (day.temperature * 9 / 5) + 32
            String.format(java.util.Locale.ROOT, "%.0f°F", f)
        } else {
            String.format(java.util.Locale.ROOT, "%.0f°C", day.temperature)
        }
    }

    Card(
        modifier = modifier
            .width(100.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = day.day,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = when (day.condition) {
                    "Sunny" -> Icons.Default.WbSunny
                    "Partly Cloudy" -> Icons.Default.CloudQueue
                    "Cloudy" -> Icons.Default.Cloud
                    "Rainy" -> Icons.Default.WaterDrop
                    else -> Icons.Default.Grain
                },
                contentDescription = day.condition,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = tempText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
