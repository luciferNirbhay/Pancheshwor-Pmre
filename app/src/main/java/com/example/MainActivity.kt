package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ThemeModePreference
import com.example.model.UserRole
import com.example.ui.components.PancheshworTopAppBar
import com.example.ui.components.RoleSwitcherBottomSheet
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentUser by viewModel.currentUser.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val snackbarMessage by viewModel.snackbarMessage.collectAsState()

            val isDarkTheme = when (currentUser.themeMode) {
                ThemeModePreference.SYSTEM -> isSystemInDarkTheme()
                ThemeModePreference.LIGHT -> false
                ThemeModePreference.DARK -> true
            }

            var showRoleSheet by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearSnackbar()
                }
            }

            val searchKeyword by viewModel.searchKeyword.collectAsState()
            val isTopBarSearchExpanded by viewModel.isTopBarSearchExpanded.collectAsState()
            val minPrice by viewModel.minPrice.collectAsState()
            val maxPrice by viewModel.maxPrice.collectAsState()
            val selectedWard by viewModel.selectedWard.collectAsState()
            val selectedTole by viewModel.selectedTole.collectAsState()
            val activeFilterCount by viewModel.activeFilterCount.collectAsState()
            val isFilterSheetVisible by viewModel.isFilterSheetVisible.collectAsState()
            val filterSheetInitialTab by viewModel.filterSheetInitialTab.collectAsState()
            val availableToles by viewModel.availableToles.collectAsState()
            val filteredProperties by viewModel.filteredProperties.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val isFullScreenPhoto = currentScreen == AppScreen.PHOTO_VIEWER
                val isWelcome = currentScreen == AppScreen.WELCOME

                Scaffold(
                    topBar = {
                        if (!isFullScreenPhoto && !isWelcome) {
                            val screenTitle = when (currentScreen) {
                                AppScreen.HOME -> StringsRepo.get("app_title", currentUser.language)
                                AppScreen.SEARCH_FILTER -> StringsRepo.get("search", currentUser.language)
                                AppScreen.PROPERTY_DETAIL -> "Property Details"
                                AppScreen.AREA_CONVERTER -> StringsRepo.get("converter", currentUser.language)
                                AppScreen.FAVORITES -> StringsRepo.get("favorites", currentUser.language)
                                AppScreen.SIGN_IN -> StringsRepo.get("sign_in", currentUser.language)
                                AppScreen.CONTACT_US -> StringsRepo.get("contact_us", currentUser.language)
                                AppScreen.MEMBERSHIP_CHECKOUT -> StringsRepo.get("seller_membership", currentUser.language)
                                AppScreen.MY_LISTINGS -> StringsRepo.get("my_listings", currentUser.language)
                                AppScreen.CREATE_EDIT_LISTING -> StringsRepo.get("create_listing", currentUser.language)
                                AppScreen.PAYMENTS_RECEIPTS -> "Receipts & Ledger"
                                AppScreen.SETTINGS -> StringsRepo.get("settings", currentUser.language)
                                AppScreen.ADMIN_APPROVALS -> StringsRepo.get("admin_approvals", currentUser.language)
                                AppScreen.ADMIN_MANAGEMENT -> StringsRepo.get("admin_management", currentUser.language)
                                else -> StringsRepo.get("app_title", currentUser.language)
                            }

                            com.example.ui.components.SearchAndFilterTopAppBar(
                                title = screenTitle,
                                canNavigateBack = currentScreen != AppScreen.HOME,
                                onNavigateBack = { viewModel.navigateBack() },
                                currentRole = currentUser.role,
                                onRoleClick = { showRoleSheet = true },
                                currentTheme = currentUser.themeMode,
                                onToggleTheme = {
                                    val next = when (currentUser.themeMode) {
                                        ThemeModePreference.LIGHT -> ThemeModePreference.DARK
                                        ThemeModePreference.DARK -> ThemeModePreference.SYSTEM
                                        ThemeModePreference.SYSTEM -> ThemeModePreference.LIGHT
                                    }
                                    viewModel.updateTheme(next)
                                },
                                currentLang = currentUser.language,
                                onToggleLang = {
                                    val next = if (currentUser.language == AppLanguage.ENGLISH) AppLanguage.NEPALI else AppLanguage.ENGLISH
                                    viewModel.updateLanguage(next)
                                },
                                isHomeScreen = currentScreen == AppScreen.HOME,
                                searchKeyword = searchKeyword,
                                onSearchKeywordChange = { viewModel.searchKeyword.value = it },
                                isSearchExpanded = isTopBarSearchExpanded,
                                onToggleSearchExpanded = {
                                    viewModel.isTopBarSearchExpanded.value = !viewModel.isTopBarSearchExpanded.value
                                },
                                minPrice = minPrice,
                                maxPrice = maxPrice,
                                selectedWard = selectedWard,
                                selectedTole = selectedTole,
                                activeFilterCount = activeFilterCount,
                                onOpenFilterSheet = { tabIndex -> viewModel.openFilterSheet(tabIndex) },
                                onClearPrice = { viewModel.clearPriceFilter() },
                                onClearLocation = { viewModel.clearLocationFilter() },
                                onResetAll = { viewModel.resetFilters() }
                            )
                        }
                    },
                    bottomBar = {
                        if (!isFullScreenPhoto && !isWelcome) {
                            val lang = currentUser.language
                            NavigationBar(
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                // Home
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.HOME,
                                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = StringsRepo.get("home", lang)
                                        )
                                    },
                                    label = { Text(StringsRepo.get("home", lang), fontSize = 11.sp) }
                                )

                                // Search
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.SEARCH_FILTER,
                                    onClick = { viewModel.navigateTo(AppScreen.SEARCH_FILTER) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.SEARCH_FILTER) Icons.Filled.Search else Icons.Outlined.Search,
                                            contentDescription = StringsRepo.get("search", lang)
                                        )
                                    },
                                    label = { Text(StringsRepo.get("search", lang), fontSize = 11.sp) }
                                )

                                // Area Converter
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.AREA_CONVERTER,
                                    onClick = { viewModel.navigateTo(AppScreen.AREA_CONVERTER) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.AREA_CONVERTER) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                                            contentDescription = StringsRepo.get("converter", lang)
                                        )
                                    },
                                    label = { Text(StringsRepo.get("converter", lang), fontSize = 11.sp) }
                                )

                                // Favorites
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.FAVORITES,
                                    onClick = { viewModel.navigateTo(AppScreen.FAVORITES) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.FAVORITES) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = StringsRepo.get("favorites", lang)
                                        )
                                    },
                                    label = { Text(StringsRepo.get("favorites", lang), fontSize = 11.sp) }
                                )

                                // Settings / Menu
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.SETTINGS,
                                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = StringsRepo.get("settings", lang)
                                        )
                                    },
                                    label = { Text(StringsRepo.get("settings", lang), fontSize = 11.sp) }
                                )
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isFullScreenPhoto) PaddingValues(0.dp) else innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.WELCOME -> WelcomeScreen(viewModel)
                            AppScreen.HOME -> HomeScreen(viewModel)
                            AppScreen.SEARCH_FILTER -> SearchFilterScreen(viewModel)
                            AppScreen.PROPERTY_DETAIL -> PropertyDetailScreen(viewModel)
                            AppScreen.PHOTO_VIEWER -> PhotoViewerScreen(viewModel)
                            AppScreen.AREA_CONVERTER -> AreaConverterScreen(viewModel)
                            AppScreen.FAVORITES -> FavoritesScreen(viewModel)
                            AppScreen.SIGN_IN -> SignInScreen(viewModel)
                            AppScreen.CONTACT_US -> ContactUsScreen(viewModel)
                            AppScreen.MEMBERSHIP_CHECKOUT -> MembershipScreen(viewModel)
                            AppScreen.MY_LISTINGS -> MyListingsScreen(viewModel)
                            AppScreen.CREATE_EDIT_LISTING -> CreateEditListingScreen(viewModel)
                            AppScreen.PAYMENTS_RECEIPTS -> PaymentsHistoryScreen(viewModel)
                            AppScreen.SETTINGS -> SettingsScreen(viewModel)
                            AppScreen.ADMIN_APPROVALS -> AdminApprovalsScreen(viewModel)
                            AppScreen.ADMIN_MANAGEMENT -> AdminManagementScreen(viewModel)
                        }
                    }
                }

                // Testing Role Switcher Sheet
                if (showRoleSheet) {
                    RoleSwitcherBottomSheet(
                        currentRole = currentUser.role,
                        onSelectRole = { newRole ->
                            viewModel.switchRole(newRole)
                        },
                        onDismiss = { showRoleSheet = false }
                    )
                }

                // Property Price & Location Filter Sheet
                if (isFilterSheetVisible) {
                    com.example.ui.components.PropertyFilterBottomSheet(
                        initialTab = filterSheetInitialTab,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        onPriceRangeChange = { min, max ->
                            viewModel.minPrice.value = min
                            viewModel.maxPrice.value = max
                        },
                        selectedWard = selectedWard,
                        onWardChange = { ward ->
                            viewModel.selectedWard.value = ward
                        },
                        selectedTole = selectedTole,
                        onToleChange = { tole ->
                            viewModel.selectedTole.value = tole
                        },
                        availableToles = availableToles,
                        currentLang = currentUser.language,
                        useNepaliDigits = currentUser.nepaliDigits,
                        propertiesCount = filteredProperties.size,
                        onReset = { viewModel.resetFilters() },
                        onDismiss = { viewModel.closeFilterSheet() }
                    )
                }
            }
        }
    }
}
