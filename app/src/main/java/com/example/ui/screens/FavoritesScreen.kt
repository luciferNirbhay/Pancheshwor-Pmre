package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.PropertyCard
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    val favoriteProperties = remember(properties, favorites) {
        properties.filter { favorites.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen")
    ) {
        if (favoriteProperties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isNe) "तपाईंको कुनै मनपर्ने सम्पत्ति छैन" else "No saved favorites yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isNe) "गृहपृष्ठबाट मनपर्ने सम्पत्तिहरू सेभ गर्नुहोस्" else "Tap the heart icon on any property to save it here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Text(if (isNe) "सम्पत्तिहरू खोज्नुहोस्" else "Browse Properties")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "${StringsRepo.get("favorites", lang)} (${favoriteProperties.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(favoriteProperties, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavorite(property.id) },
                        onClick = {
                            viewModel.selectProperty(property)
                            viewModel.navigateTo(AppScreen.PROPERTY_DETAIL)
                        },
                        language = lang,
                        useNepaliDigits = useNepaliDigits
                    )
                }
            }
        }
    }
}
