package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Property
import com.example.model.PropertyType
import com.example.model.UserRole
import com.example.ui.components.PropertyCard
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val selectedWard by viewModel.selectedWard.collectAsState()
    val selectedTole by viewModel.selectedTole.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()

    val hasPriceFilter = minPrice > 0L || maxPrice < 50000000L
    val hasLocationFilter = selectedWard != null || selectedTole != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Branding Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StringsRepo.get("app_title", lang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = StringsRepo.get("tagline", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Converter floating shortcut
                        FilledTonalButton(
                            onClick = { viewModel.navigateTo(AppScreen.AREA_CONVERTER) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("home_converter_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNe) "नाप क्यालकुलेटर" else "Converter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Trigger Bar
                    Surface(
                        onClick = { viewModel.isTopBarSearchExpanded.value = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_bar")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (searchKeyword.isNotBlank()) searchKeyword
                                else if (isNe) "स्थान, वडा वा सम्पत्ति खोज्नुहोस्..."
                                else "Search by tole, ward or keywords in Bhimdatta...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (searchKeyword.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.openFilterSheet(0) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (activeFilterCount > 0) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                Text(activeFilterCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filters",
                                        tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Filter Notification Pill (when price or location filters are active)
        if (hasPriceFilter || hasLocationFilter || searchKeyword.isNotBlank()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val filterSummary = buildList {
                                if (hasPriceFilter) {
                                    val minS = if (minPrice > 0) NumberLocalizationUtil.formatPriceNpr(minPrice, true, isNe) else ""
                                    val maxS = if (maxPrice < 50000000L) NumberLocalizationUtil.formatPriceNpr(maxPrice, true, isNe) else ""
                                    when {
                                        minPrice > 0 && maxPrice < 50000000L -> add("Price: $minS–$maxS")
                                        maxPrice < 50000000L -> add("Price: < $maxS")
                                        else -> add("Price: > $minS")
                                    }
                                }
                                if (selectedWard != null) add("Ward $selectedWard")
                                if (selectedTole != null) add(selectedTole!!)
                                if (searchKeyword.isNotBlank()) add("\"$searchKeyword\"")
                            }.joinToString(" • ")

                            Text(
                                text = filterSummary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        TextButton(
                            onClick = { viewModel.resetFilters() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = StringsRepo.get("clear_filters", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Quick Role Navigation Notice for Evaluators / Users
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StringsRepo.get("tap_photo_hint", lang),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Role Quick-Action Shortcuts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentUser.role == UserRole.GUEST || currentUser.role == UserRole.BUYER) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.MEMBERSHIP_CHECKOUT) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("become_seller", lang), fontSize = 12.sp)
                    }
                } else if (currentUser.role == UserRole.DEVELOPER) {
                    Button(
                        onClick = { viewModel.startCreateListing() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("create_listing", lang), fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.MY_LISTINGS) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("my_listings", lang), fontSize = 12.sp)
                    }
                } else if (currentUser.role == UserRole.ADMIN) {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.ADMIN_APPROVALS) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("admin_approvals", lang), fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.ADMIN_MANAGEMENT) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("admin_management", lang), fontSize = 12.sp)
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCat == null,
                            onClick = { viewModel.selectedCategory.value = null },
                            label = { Text(StringsRepo.get("all", lang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                    PropertyType.values().forEach { type ->
                        item {
                            FilterChip(
                                selected = selectedCat == type,
                                onClick = {
                                    viewModel.selectedCategory.value = if (selectedCat == type) null else type
                                },
                                label = {
                                    Text(if (isNe) type.displayNameNe else type.displayNameEn)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            PropertyType.LAND -> Icons.Default.Terrain
                                            PropertyType.HOUSE -> Icons.Default.Villa
                                            PropertyType.APARTMENT -> Icons.Default.Domain
                                            PropertyType.COMMERCIAL -> Icons.Default.Storefront
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Properties List
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = StringsRepo.get("featured_listings", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val countText = "${properties.size} ${if (isNe) "सम्पत्ति" else "Properties"}"
                Text(
                    text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(countText) else countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Listings
        if (properties.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isNe) "कुनै सम्पत्ति फेला परेन" else "No properties match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.resetFilters() }) {
                            Text(StringsRepo.get("reset_filters", lang))
                        }
                    }
                }
            }
        } else {
            items(properties, key = { it.id }) { property ->
                PropertyCard(
                    property = property,
                    isFavorite = favorites.contains(property.id),
                    onToggleFavorite = { viewModel.toggleFavorite(property.id) },
                    onClick = {
                        viewModel.selectProperty(property)
                        viewModel.navigateTo(AppScreen.PROPERTY_DETAIL)
                    },
                    language = lang,
                    useNepaliDigits = useNepaliDigits,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
