package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ThemeModePreference
import com.example.model.UserRole
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    currentRole: UserRole,
    onRoleClick: () -> Unit,
    currentTheme: ThemeModePreference,
    onToggleTheme: () -> Unit,
    currentLang: AppLanguage,
    onToggleLang: () -> Unit,
    isHomeScreen: Boolean,
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    onToggleSearchExpanded: () -> Unit,
    minPrice: Long,
    maxPrice: Long,
    selectedWard: Int?,
    selectedTole: String?,
    activeFilterCount: Int,
    onOpenFilterSheet: (tabIndex: Int) -> Unit,
    onClearPrice: () -> Unit,
    onClearLocation: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNe = currentLang == AppLanguage.NEPALI
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val hasPriceFilter = minPrice > 0L || maxPrice < 50000000L
    val hasLocationFilter = selectedWard != null || selectedTole != null

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Top Bar Row
            AnimatedContent(
                targetState = isSearchExpanded,
                transitionSpec = {
                    fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                },
                label = "top_bar_search_animation"
            ) { searchMode ->
                if (searchMode) {
                    // Expanded Search Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onToggleSearchExpanded()
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.testTag("close_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Search"
                            )
                        }

                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = onSearchKeywordChange,
                            placeholder = {
                                Text(
                                    text = if (isNe) "स्थान, टोल, वडा वा सम्पत्ति खोज्नुहोस्..." else "Search tole, ward or keywords in Bhimdatta...",
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (searchKeyword.isNotBlank()) {
                                    IconButton(onClick = { onSearchKeywordChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .testTag("top_app_bar_search_input")
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        // Filter button in search mode
                        IconButton(
                            onClick = { onOpenFilterSheet(0) },
                            modifier = Modifier.testTag("top_bar_filter_icon_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (activeFilterCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(activeFilterCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filters",
                                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    // Standard Top Bar with Search & Filter action icons
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Bhimdatta (Mahendranagar)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            if (canNavigateBack) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.testTag("nav_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            } else {
                                IconButton(onClick = onRoleClick) {
                                    Icon(
                                        imageVector = Icons.Default.Apartment,
                                        contentDescription = "App Logo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        actions = {
                            // Search Action Icon
                            IconButton(
                                onClick = onToggleSearchExpanded,
                                modifier = Modifier.testTag("top_bar_open_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search properties"
                                )
                            }

                            // Filter Button with Badge
                            IconButton(
                                onClick = { onOpenFilterSheet(0) },
                                modifier = Modifier.testTag("top_bar_filter_btn")
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
                                        contentDescription = "Filter by Price & Location",
                                        tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Role Switcher Chip
                            AssistChip(
                                onClick = onRoleClick,
                                label = {
                                    Text(
                                        text = when (currentRole) {
                                            UserRole.GUEST -> "Guest"
                                            UserRole.BUYER -> "Buyer"
                                            UserRole.DEVELOPER -> "Dev"
                                            UserRole.ADMIN -> "Admin"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (currentRole) {
                                            UserRole.GUEST -> Icons.Outlined.Person
                                            UserRole.BUYER -> Icons.Filled.ShoppingBag
                                            UserRole.DEVELOPER -> Icons.Filled.BusinessCenter
                                            UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .testTag("role_switcher_chip")
                            )

                            // Theme toggle button
                            IconButton(
                                onClick = onToggleTheme,
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = when (currentTheme) {
                                        ThemeModePreference.DARK -> Icons.Default.DarkMode
                                        ThemeModePreference.LIGHT -> Icons.Default.LightMode
                                        ThemeModePreference.SYSTEM -> Icons.Default.BrightnessAuto
                                    },
                                    contentDescription = "Toggle Theme"
                                )
                            }

                            // Language toggle
                            TextButton(
                                onClick = onToggleLang,
                                modifier = Modifier.testTag("lang_toggle_button")
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.ENGLISH) "ने" else "EN",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Interactive Filter Sub-Bar (always accessible on Home or when filters exist)
            if (isHomeScreen || hasPriceFilter || hasLocationFilter) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Price Range Filter Chip
                    val priceLabel = if (hasPriceFilter) {
                        val minS = if (minPrice > 0) NumberLocalizationUtil.formatPriceNpr(minPrice, true, isNe) else ""
                        val maxS = if (maxPrice < 50000000L) NumberLocalizationUtil.formatPriceNpr(maxPrice, true, isNe) else ""
                        when {
                            minPrice > 0 && maxPrice < 50000000L -> "$minS – $maxS"
                            maxPrice < 50000000L -> "< $maxS"
                            else -> "> $minS"
                        }
                    } else {
                        StringsRepo.get("filter_price", currentLang)
                    }

                    FilterChip(
                        selected = hasPriceFilter,
                        onClick = { onOpenFilterSheet(0) },
                        label = {
                            Text(
                                text = "💰 $priceLabel",
                                fontSize = 12.sp,
                                fontWeight = if (hasPriceFilter) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            if (hasPriceFilter) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Price Filter",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onClearPrice() }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("top_bar_price_chip")
                    )

                    // 2. Location Area / Ward Filter Chip
                    val locationLabel = when {
                        selectedWard != null && selectedTole != null -> "${StringsRepo.get("ward", currentLang)} $selectedWard • $selectedTole"
                        selectedWard != null -> "${StringsRepo.get("ward", currentLang)} $selectedWard"
                        selectedTole != null -> selectedTole
                        else -> StringsRepo.get("filter_location", currentLang)
                    }

                    FilterChip(
                        selected = hasLocationFilter,
                        onClick = { onOpenFilterSheet(1) },
                        label = {
                            Text(
                                text = "📍 $locationLabel",
                                fontSize = 12.sp,
                                fontWeight = if (hasLocationFilter) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            if (hasLocationFilter) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Location Filter",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onClearLocation() }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("top_bar_location_chip")
                    )

                    // 3. Clear All Button (when any active filter or search keyword exists)
                    if (hasPriceFilter || hasLocationFilter || searchKeyword.isNotBlank()) {
                        AssistChip(
                            onClick = onResetAll,
                            label = {
                                Text(
                                    text = StringsRepo.get("clear_filters", currentLang),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("top_bar_clear_all_chip")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyFilterBottomSheet(
    initialTab: Int = 0,
    minPrice: Long,
    maxPrice: Long,
    onPriceRangeChange: (Long, Long) -> Unit,
    selectedWard: Int?,
    onWardChange: (Int?) -> Unit,
    selectedTole: String?,
    onToleChange: (String?) -> Unit,
    availableToles: List<String>,
    currentLang: AppLanguage,
    useNepaliDigits: Boolean,
    propertiesCount: Int,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val isNe = currentLang == AppLanguage.NEPALI
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    var tempMinPrice by remember { mutableFloatStateOf(minPrice.toFloat()) }
    var tempMaxPrice by remember { mutableFloatStateOf(maxPrice.toFloat()) }
    var tempWard by remember { mutableStateOf(selectedWard) }
    var tempTole by remember { mutableStateOf(selectedTole) }

    val pricePresets = listOf(
        "Any (सबै)" to (0L to 50000000L),
        "< 50 Lakh" to (0L to 5000000L),
        "50L – 1.5 Cr" to (5000000L to 15000000L),
        "1.5 Cr – 3 Cr" to (15000000L to 30000000L),
        "> 3 Cr" to (30000000L to 50000000L)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .testTag("property_filter_bottom_sheet")
        ) {
            // Header with Title & Reset Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isNe) "सम्पत्ति फिल्टर गर्नुहोस्" else "Filter Listings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        tempMinPrice = 0f
                        tempMaxPrice = 50000000f
                        tempWard = null
                        tempTole = null
                        onReset()
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(StringsRepo.get("reset_filters", currentLang))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs: Price Range & Location Area
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰 ")
                            Text(
                                text = StringsRepo.get("filter_price", currentLang),
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍 ")
                            Text(
                                text = StringsRepo.get("filter_location", currentLang),
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab 0: Price Range Filter
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Current Price Range Display Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = StringsRepo.get("price", currentLang) + " Range (NPR)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val minFormatted = NumberLocalizationUtil.formatPriceNpr(
                                tempMinPrice.toLong(),
                                shortForm = true,
                                useNepali = isNe,
                                useNepaliDigits = useNepaliDigits || isNe
                            )
                            val maxFormatted = if (tempMaxPrice >= 50000000f) {
                                if (isNe) "५ करोड+" else "5 Crore+"
                            } else {
                                NumberLocalizationUtil.formatPriceNpr(
                                    tempMaxPrice.toLong(),
                                    shortForm = true,
                                    useNepali = isNe,
                                    useNepaliDigits = useNepaliDigits || isNe
                                )
                            }

                            Text(
                                text = "$minFormatted  –  $maxFormatted",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Price Presets
                    Text(
                        text = if (isNe) "द्रुत मूल्य छनौट:" else "Quick Price Brackets:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(pricePresets) { (label, range) ->
                            val isSelected = tempMinPrice == range.first.toFloat() && tempMaxPrice == range.second.toFloat()
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    tempMinPrice = range.first.toFloat()
                                    tempMaxPrice = range.second.toFloat()
                                },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Price Slider
                    Column {
                        Text(
                            text = if (isNe) "मूल्य समायोजन गर्नुहोस्:" else "Adjust Price Slider:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        RangeSlider(
                            value = tempMinPrice..tempMaxPrice,
                            onValueChange = { range ->
                                tempMinPrice = range.start
                                tempMaxPrice = range.endInclusive
                            },
                            valueRange = 0f..50000000f,
                            steps = 49,
                            modifier = Modifier.fillMaxWidth().testTag("filter_price_slider")
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rs. 0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rs. 5 Crore", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                // Tab 1: Location Area / Ward Filter
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Bhimdatta Municipality Ward Selector (Wards 1 to 19)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bhimdatta Municipality Wards (१ - १९):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (tempWard != null) {
                                TextButton(
                                    onClick = { tempWard = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Clear Ward", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = tempWard == null,
                                    onClick = { tempWard = null },
                                    label = { Text(StringsRepo.get("all_wards", currentLang), fontSize = 12.sp) }
                                )
                            }
                            items((1..19).toList()) { wardNum ->
                                val isSelected = tempWard == wardNum
                                val wardLabel = "${StringsRepo.get("ward", currentLang)} $wardNum"
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        tempWard = if (tempWard == wardNum) null else wardNum
                                    },
                                    label = {
                                        Text(
                                            text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(wardLabel) else wardLabel,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Tole / Locality Selector in Mahendranagar
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isNe) "महेन्द्रनगरका प्रमुख टोलहरू:" else "Localities / Toles in Mahendranagar:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (tempTole != null) {
                                TextButton(
                                    onClick = { tempTole = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Clear Tole", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val popularToles = listOf(
                            "Main Bazaar",
                            "Kaluwapur Road",
                            "Suda Chowk",
                            "Traffic Chowk",
                            "Hospital Road",
                            "Brahmadev Road"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = tempTole == null,
                                    onClick = { tempTole = null },
                                    label = { Text(StringsRepo.get("all_toles", currentLang), fontSize = 12.sp) }
                                )
                            }
                            items(popularToles) { toleName ->
                                val isSelected = tempTole == toleName
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        tempTole = if (tempTole == toleName) null else toleName
                                    },
                                    label = { Text(toleName, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Apply Action Button with matching count
            Button(
                onClick = {
                    onPriceRangeChange(tempMinPrice.toLong(), tempMaxPrice.toLong())
                    onWardChange(tempWard)
                    onToleChange(tempTole)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_bottom_sheet_filters_btn")
            ) {
                Text(
                    text = "${StringsRepo.get("apply_filters", currentLang)} (${propertiesCount} ${StringsRepo.get("properties_found", currentLang)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
