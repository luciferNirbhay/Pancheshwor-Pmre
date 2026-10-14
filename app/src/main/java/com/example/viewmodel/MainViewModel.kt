package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PancheshworRepository
import com.example.model.*
import com.example.util.AreaConverterUtil
import com.example.util.HillArea
import com.example.util.TeraiArea
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    WELCOME,
    HOME,
    SEARCH_FILTER,
    PROPERTY_DETAIL,
    PHOTO_VIEWER,
    AREA_CONVERTER,
    FAVORITES,
    SIGN_IN,
    CONTACT_US,
    MEMBERSHIP_CHECKOUT,
    MY_LISTINGS,
    CREATE_EDIT_LISTING,
    PAYMENTS_RECEIPTS,
    SETTINGS,
    ADMIN_APPROVALS,
    ADMIN_MANAGEMENT
}

enum class SortOption {
    NEWEST,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    AREA_HIGH_LOW
}

class MainViewModel(
    val repository: PancheshworRepository = PancheshworRepository()
) : ViewModel() {

    private val _screenStack = MutableStateFlow(listOf(AppScreen.HOME))
    val currentScreen: StateFlow<AppScreen> = _screenStack.map { it.lastOrNull() ?: AppScreen.HOME }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppScreen.HOME)

    val currentUser = repository.currentUser
    val companyInfo = repository.companyInfo
    val membershipPlans = repository.membershipPlans
    val payments = repository.payments
    val favorites = repository.favorites
    val inquiries = repository.inquiries
    private val rawProperties = repository.properties

    // Navigation
    fun navigateTo(screen: AppScreen) {
        if (_screenStack.value.lastOrNull() == screen) return
        _screenStack.value = _screenStack.value + screen
    }

    fun navigateBack(): Boolean {
        if (_screenStack.value.size > 1) {
            _screenStack.value = _screenStack.value.dropLast(1)
            return true
        }
        return false
    }

    // Property selection for detail & full-screen photo viewer
    private val _selectedPropertyId = MutableStateFlow<String?>("prop_1")
    val selectedProperty: StateFlow<Property?> = combine(rawProperties, _selectedPropertyId) { props, id ->
        props.firstOrNull { it.id == id } ?: props.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun selectProperty(property: Property) {
        _selectedPropertyId.value = property.id
        _selectedPhotoIndex.value = 0
    }

    // Photo viewer
    private val _selectedPhotoIndex = MutableStateFlow(0)
    val selectedPhotoIndex: StateFlow<Int> = _selectedPhotoIndex.asStateFlow()

    private val _photoInfoPanelVisible = MutableStateFlow(true)
    val photoInfoPanelVisible: StateFlow<Boolean> = _photoInfoPanelVisible.asStateFlow()

    fun setSelectedPhotoIndex(index: Int) {
        _selectedPhotoIndex.value = index
    }

    fun togglePhotoInfoPanel() {
        _photoInfoPanelVisible.value = !_photoInfoPanelVisible.value
    }

    // Filters and Search
    val searchKeyword = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<PropertyType?>(null)
    val selectedWard = MutableStateFlow<Int?>(null)
    val selectedTole = MutableStateFlow<String?>(null)
    val minPrice = MutableStateFlow<Long>(0L)
    val maxPrice = MutableStateFlow<Long>(50000000L) // 5 Crore
    val selectedStatus = MutableStateFlow<PropertyStatus?>(null)
    val sortOption = MutableStateFlow(SortOption.NEWEST)

    // Top Bar Search & Filter UI state
    val isTopBarSearchExpanded = MutableStateFlow(false)
    val isFilterSheetVisible = MutableStateFlow(false)
    val filterSheetInitialTab = MutableStateFlow(0) // 0: Price Range, 1: Location Area

    val activeFilterCount: StateFlow<Int> = combine(
        minPrice,
        maxPrice,
        selectedWard,
        selectedTole,
        selectedCategory,
        selectedStatus
    ) { args: Array<Any?> ->
        var count = 0
        val minP = args[0] as Long
        val maxP = args[1] as Long
        val ward = args[2] as Int?
        val tole = args[3] as String?
        val cat = args[4] as PropertyType?
        val status = args[5] as PropertyStatus?

        if (minP > 0L || maxP < 50000000L) count++
        if (ward != null || tole != null) count++
        if (cat != null) count++
        if (status != null) count++
        count
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val availableToles: StateFlow<List<String>> = rawProperties.map { props ->
        props.map { it.tole.substringBefore(",") }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredProperties: StateFlow<List<Property>> = combine(
        rawProperties,
        searchKeyword,
        selectedCategory,
        selectedWard,
        selectedTole,
        minPrice,
        maxPrice,
        selectedStatus,
        sortOption
    ) { args: Array<Any?> ->
        val props = args[0] as List<Property>
        val keyword = (args[1] as String).trim().lowercase()
        val cat = args[2] as PropertyType?
        val ward = args[3] as Int?
        val tole = args[4] as String?
        val minP = args[5] as Long
        val maxP = args[6] as Long
        val status = args[7] as PropertyStatus?
        val sort = args[8] as SortOption

        props.filter { p ->
            // Role filter: guests & buyers only see approved listings
            val role = currentUser.value.role
            val canSee = p.approved || role == UserRole.ADMIN || (role == UserRole.DEVELOPER && p.developerId == currentUser.value.id)
            if (!canSee) return@filter false

            // Keyword match
            val matchesKeyword = keyword.isEmpty() ||
                    p.title.lowercase().contains(keyword) ||
                    p.titleNe.lowercase().contains(keyword) ||
                    p.tole.lowercase().contains(keyword) ||
                    p.toleNe.lowercase().contains(keyword) ||
                    p.description.lowercase().contains(keyword) ||
                    keyword.replace("ward", "").trim().toIntOrNull() == p.ward

            val matchesCat = cat == null || p.type == cat
            val matchesWard = ward == null || p.ward == ward
            val matchesTole = tole == null || p.tole.contains(tole, ignoreCase = true) || p.toleNe.contains(tole, ignoreCase = true)
            val matchesPrice = p.priceNpr in minP..maxP
            val matchesStatus = status == null || p.status == status

            matchesKeyword && matchesCat && matchesWard && matchesTole && matchesPrice && matchesStatus
        }.let { list ->
            when (sort) {
                SortOption.NEWEST -> list.sortedByDescending { it.createdAt }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.priceNpr }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.priceNpr }
                SortOption.AREA_HIGH_LOW -> list.sortedByDescending { it.areaM2 }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun clearPriceFilter() {
        minPrice.value = 0L
        maxPrice.value = 50000000L
    }

    fun clearLocationFilter() {
        selectedWard.value = null
        selectedTole.value = null
    }

    fun resetFilters() {
        searchKeyword.value = ""
        selectedCategory.value = null
        selectedWard.value = null
        selectedTole.value = null
        minPrice.value = 0L
        maxPrice.value = 50000000L
        selectedStatus.value = null
        sortOption.value = SortOption.NEWEST
        isTopBarSearchExpanded.value = false
    }

    fun openFilterSheet(initialTab: Int = 0) {
        filterSheetInitialTab.value = initialTab
        isFilterSheetVisible.value = true
    }

    fun closeFilterSheet() {
        isFilterSheetVisible.value = false
    }

    // Area Converter state & live conversions
    val converterM2Input = MutableStateFlow("1000")
    val converterBighaInput = MutableStateFlow("0")
    val converterKatthaInput = MutableStateFlow("2")
    val converterDhurInput = MutableStateFlow("19.06")
    val converterShowHillUnits = MutableStateFlow(false)

    val teraiResult: StateFlow<TeraiArea> = converterM2Input.map { input ->
        val m2 = input.toDoubleOrNull() ?: 0.0
        AreaConverterUtil.m2ToTerai(m2)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AreaConverterUtil.m2ToTerai(1000.0))

    val hillResult: StateFlow<HillArea> = converterM2Input.map { input ->
        val m2 = input.toDoubleOrNull() ?: 0.0
        AreaConverterUtil.m2ToHill(m2)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AreaConverterUtil.m2ToHill(1000.0))

    fun updateConverterFromM2(m2Str: String) {
        converterM2Input.value = m2Str
        val m2 = m2Str.toDoubleOrNull() ?: 0.0
        val t = AreaConverterUtil.m2ToTerai(m2)
        converterBighaInput.value = t.bigha.toString()
        converterKatthaInput.value = t.kattha.toString()
        converterDhurInput.value = String.format(java.util.Locale.US, "%.2f", t.dhur)
    }

    fun updateConverterFromTerai(bighaStr: String, katthaStr: String, dhurStr: String) {
        converterBighaInput.value = bighaStr
        converterKatthaInput.value = katthaStr
        converterDhurInput.value = dhurStr
        val b = bighaStr.toDoubleOrNull() ?: 0.0
        val k = katthaStr.toDoubleOrNull() ?: 0.0
        val d = dhurStr.toDoubleOrNull() ?: 0.0
        val m2 = AreaConverterUtil.teraiToM2(b, k, d)
        converterM2Input.value = String.format(java.util.Locale.US, "%.2f", m2)
    }

    fun setConverterPreset(m2: Double) {
        updateConverterFromM2(String.format(java.util.Locale.US, "%.2f", m2))
    }

    // Role switching
    fun switchRole(role: UserRole) {
        repository.setRole(role)
        showSnackbar("Switched role to: ${role.titleEn}")
    }

    fun updateLanguage(lang: AppLanguage) {
        repository.updateLanguage(lang)
    }

    fun updateTheme(themeMode: ThemeModePreference) {
        repository.updateTheme(themeMode)
    }

    fun updateNepaliDigits(enabled: Boolean) {
        repository.updateNepaliDigits(enabled)
    }

    fun updateDataSaver(enabled: Boolean) {
        repository.updateDataSaver(enabled)
    }

    fun toggleFavorite(propertyId: String) {
        repository.toggleFavorite(propertyId)
        val isFav = repository.favorites.value.contains(propertyId)
        showSnackbar(if (isFav) "Added to Favorites" else "Removed from Favorites")
    }

    // Inquiries
    fun submitInquiry(
        propertyId: String,
        propertyTitle: String,
        name: String,
        phone: String,
        message: String,
        type: InquiryType = InquiryType.GENERAL,
        preferredDate: String? = null,
        preferredTime: String? = null
    ) {
        repository.addInquiry(
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            buyerName = name,
            buyerPhone = phone,
            message = message,
            type = type,
            preferredDate = preferredDate,
            preferredTime = preferredTime
        )
        val msg = if (type == InquiryType.SITE_VISIT) "Site visit request booked!" else "Inquiry sent to seller & admin!"
        showSnackbar(msg)
    }

    // Listings Creation / Edit
    val editingProperty = MutableStateFlow<Property?>(null)

    fun startCreateListing() {
        val newProp = Property(
            id = "prop_" + System.currentTimeMillis().toString().takeLast(6),
            title = "",
            titleNe = "",
            type = PropertyType.LAND,
            status = PropertyStatus.AVAILABLE,
            priceNpr = 0L,
            areaM2 = 0.0,
            ward = 4,
            tole = "Bhimdatta",
            developerId = currentUser.value.id,
            developerName = currentUser.value.name,
            developerPhone = currentUser.value.phone.ifBlank { "+977 9858712345" },
            approved = false // Requires admin approval!
        )
        editingProperty.value = newProp
        navigateTo(AppScreen.CREATE_EDIT_LISTING)
    }

    fun startEditListing(property: Property) {
        editingProperty.value = property
        navigateTo(AppScreen.CREATE_EDIT_LISTING)
    }

    fun saveEditingProperty(property: Property, isSubmitForApproval: Boolean) {
        val toSave = property.copy(
            approved = if (currentUser.value.role == UserRole.ADMIN) true else !isSubmitForApproval.not() && property.approved,
            approvalReason = if (isSubmitForApproval) "Submitted for Admin Approval" else property.approvalReason
        )
        repository.createOrUpdateProperty(toSave)
        showSnackbar(if (isSubmitForApproval) "Submitted for Admin approval!" else "Draft saved successfully!")
        editingProperty.value = null
        navigateBack()
    }

    fun markPropertyStatus(propertyId: String, status: PropertyStatus) {
        repository.markStatus(propertyId, status)
        showSnackbar("Status updated to ${status.displayNameEn}")
    }

    fun deleteProperty(propertyId: String) {
        repository.deleteProperty(propertyId)
        showSnackbar("Listing deleted.")
    }

    // Admin actions
    fun approveProperty(propertyId: String) {
        repository.approveProperty(propertyId)
        showSnackbar("Listing approved & published!")
    }

    fun rejectProperty(propertyId: String, reason: String) {
        repository.rejectProperty(propertyId, reason)
        showSnackbar("Listing sent back to lister with feedback.")
    }

    fun refundPayment(paymentId: String) {
        repository.refundPayment(paymentId)
        showSnackbar("Payment refunded.")
    }

    fun updateCompanyInfo(companyInfo: CompanyInfo) {
        repository.updateCompanyInfo(companyInfo)
        showSnackbar("Company contact info updated.")
    }

    // Membership Checkout
    val selectedCheckoutPlan = MutableStateFlow<MembershipPlan?>(null)
    val selectedPaymentProvider = MutableStateFlow(PaymentProvider.ESEWA)
    val isPaymentProcessing = MutableStateFlow(false)
    val lastCompletedPayment = MutableStateFlow<PaymentRecord?>(null)

    fun startCheckout(plan: MembershipPlan) {
        selectedCheckoutPlan.value = plan
        navigateTo(AppScreen.MEMBERSHIP_CHECKOUT)
    }

    fun processPayment() {
        val plan = selectedCheckoutPlan.value ?: return
        isPaymentProcessing.value = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // Simulate gateway call & status check
            repository.executePayment(
                planId = plan.id,
                provider = selectedPaymentProvider.value
            ) { record ->
                isPaymentProcessing.value = false
                lastCompletedPayment.value = record
                showSnackbar("Payment confirmed! You are now an active Developer.")
                navigateTo(AppScreen.PAYMENTS_RECEIPTS)
            }
        }
    }

    // UI Feedback
    val snackbarMessage = MutableStateFlow<String?>(null)

    fun showSnackbar(message: String) {
        snackbarMessage.value = message
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }
}
