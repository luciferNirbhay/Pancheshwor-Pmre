package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PropertyStatus
import com.example.model.PropertyType
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedWard by viewModel.selectedWard.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var tempKeyword by remember { mutableStateOf(searchKeyword) }
    var tempMinPrice by remember { mutableFloatStateOf(minPrice.toFloat()) }
    var tempMaxPrice by remember { mutableFloatStateOf(maxPrice.toFloat()) }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetFilters()
                            tempKeyword = ""
                            tempMinPrice = 0f
                            tempMaxPrice = 50000000f
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(StringsRepo.get("reset_filters", lang))
                    }

                    Button(
                        onClick = {
                            viewModel.searchKeyword.value = tempKeyword
                            viewModel.minPrice.value = tempMinPrice.toLong()
                            viewModel.maxPrice.value = tempMaxPrice.toLong()
                            viewModel.navigateBack()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("apply_filters_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(StringsRepo.get("apply_filters", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = tempKeyword,
                    onValueChange = { tempKeyword = it },
                    label = { Text(if (isNe) "खोज्नुहोस् (स्थान, टोल वा शब्द)" else "Search (Tole, Location, Title)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (tempKeyword.isNotBlank()) {
                            IconButton(onClick = { tempKeyword = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_filter_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Property Type
            item {
                Column {
                    Text(
                        text = if (isNe) "सम्पत्तिको प्रकार" else "Property Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { viewModel.selectedCategory.value = null },
                                label = { Text(StringsRepo.get("all", lang)) }
                            )
                        }
                        items(PropertyType.values()) { type ->
                            FilterChip(
                                selected = selectedCategory == type,
                                onClick = {
                                    viewModel.selectedCategory.value = if (selectedCategory == type) null else type
                                },
                                label = { Text(if (isNe) type.displayNameNe else type.displayNameEn) }
                            )
                        }
                    }
                }
            }

            // Price Range Slider
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = StringsRepo.get("price", lang) + " Range (NPR)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val minPriceStr = NumberLocalizationUtil.formatPriceNpr(
                            tempMinPrice.toLong(),
                            shortForm = true,
                            useNepali = isNe,
                            useNepaliDigits = useNepaliDigits || isNe
                        )
                        val maxPriceStr = NumberLocalizationUtil.formatPriceNpr(
                            tempMaxPrice.toLong(),
                            shortForm = true,
                            useNepali = isNe,
                            useNepaliDigits = useNepaliDigits || isNe
                        )

                        Text(
                            text = "$minPriceStr  –  $maxPriceStr",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        RangeSlider(
                            value = tempMinPrice..tempMaxPrice,
                            onValueChange = { range ->
                                tempMinPrice = range.start
                                tempMaxPrice = range.endInclusive
                            },
                            valueRange = 0f..50000000f,
                            steps = 49,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Ward Selector (Bhimdatta Municipality Wards 1 to 19)
            item {
                Column {
                    Text(
                        text = "${StringsRepo.get("ward", lang)} (Bhimdatta Municipality)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedWard == null,
                                onClick = { viewModel.selectedWard.value = null },
                                label = { Text(StringsRepo.get("all", lang)) }
                            )
                        }
                        items((1..19).toList()) { wardNum ->
                            val label = "${StringsRepo.get("ward", lang)} $wardNum"
                            FilterChip(
                                selected = selectedWard == wardNum,
                                onClick = {
                                    viewModel.selectedWard.value = if (selectedWard == wardNum) null else wardNum
                                },
                                label = {
                                    Text(if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(label) else label)
                                }
                            )
                        }
                    }
                }
            }

            // Status Filter
            item {
                Column {
                    Text(
                        text = if (isNe) "स्थिति" else "Listing Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { viewModel.selectedStatus.value = null },
                            label = { Text(StringsRepo.get("all", lang)) }
                        )
                        PropertyStatus.values().forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = {
                                    viewModel.selectedStatus.value = if (selectedStatus == st) null else st
                                },
                                label = { Text(if (isNe) st.displayNameNe else st.displayNameEn) }
                            )
                        }
                    }
                }
            }

            // Sort By Options
            item {
                Column {
                    Text(
                        text = StringsRepo.get("sort_by", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SortOption.values().forEach { opt ->
                            Surface(
                                onClick = { viewModel.sortOption.value = opt },
                                shape = RoundedCornerShape(10.dp),
                                color = if (sortOption == opt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    RadioButton(
                                        selected = sortOption == opt,
                                        onClick = { viewModel.sortOption.value = opt }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (opt) {
                                            SortOption.NEWEST -> StringsRepo.get("sort_newest", lang)
                                            SortOption.PRICE_LOW_HIGH -> StringsRepo.get("sort_price_asc", lang)
                                            SortOption.PRICE_HIGH_LOW -> StringsRepo.get("sort_price_desc", lang)
                                            SortOption.AREA_HIGH_LOW -> StringsRepo.get("sort_area", lang)
                                        },
                                        fontWeight = if (sortOption == opt) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
