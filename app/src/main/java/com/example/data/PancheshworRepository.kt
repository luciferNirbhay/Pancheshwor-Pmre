package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PancheshworRepository {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_buyer_1",
            name = "Nirbhay Chand",
            phone = "+977 9848712345",
            role = UserRole.BUYER,
            language = AppLanguage.ENGLISH,
            themeMode = ThemeModePreference.SYSTEM,
            nepaliDigits = false,
            dataSaverMode = false,
            activeMembershipPlanId = "plan_standard",
            membershipStatus = "Active"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _companyInfo = MutableStateFlow(CompanyInfo())
    val companyInfo: StateFlow<CompanyInfo> = _companyInfo.asStateFlow()

    private val _membershipPlans = MutableStateFlow(
        listOf(
            MembershipPlan(
                id = "plan_starter",
                nameEn = "Starter Seller",
                nameNe = "सुरुआती बिक्रेता",
                durationDays = 30,
                priceNpr = 1500,
                maxActiveListings = 3,
                maxPhotosPerListing = 8,
                badge = "Popular",
                featuresEn = listOf(
                    "3 Active Property Listings",
                    "Up to 8 High-Res Photos per property",
                    "Direct Buyer Inquiries & WhatsApp leads",
                    "30 Days validity with 7-day grace period"
                ),
                featuresNe = listOf(
                    "३ वटा सक्रिय सम्पत्ति सूची",
                    "प्रति सम्पत्ति ८ वटा सम्म तस्बिर",
                    "प्रत्यक्ष ग्राहक सोधपुछ र ह्वाट्सएप सम्पर्क",
                    "३० दिनको म्याद र थप ७ दिनको छुट"
                )
            ),
            MembershipPlan(
                id = "plan_standard",
                nameEn = "Standard Pro",
                nameNe = "स्ट्यान्डर्ड प्रो",
                durationDays = 90,
                priceNpr = 3500,
                maxActiveListings = 10,
                maxPhotosPerListing = 15,
                badge = "Best Value",
                featuresEn = listOf(
                    "10 Active Property Listings",
                    "Up to 15 High-Res Photos per listing",
                    "Lalpurja Verified Badge eligibility",
                    "Featured placement in Bhimdatta Home feed",
                    "90 Days validity with renewal alerts"
                ),
                featuresNe = listOf(
                    "१० वटा सक्रिय सम्पत्ति सूची",
                    "प्रति सम्पत्ति १५ वटा सम्म तस्बिर",
                    "लालपुर्जा प्रमाणित ब्याजको सुविधा",
                    "गृहपृष्ठमा विशेष स्थान",
                    "९० दिनको म्याद र नवीकरण सूचना"
                )
            ),
            MembershipPlan(
                id = "plan_premium",
                nameEn = "Premium Agency",
                nameNe = "प्रिमियम एजेन्सी",
                durationDays = 365,
                priceNpr = 8000,
                maxActiveListings = 30,
                maxPhotosPerListing = 20,
                badge = "Agency Choice",
                featuresEn = listOf(
                    "30 Active Listings simultaneously",
                    "Full 20 Photos gallery per listing",
                    "Top Priority in Search & Category filters",
                    "Dedicated Account Manager at Pancheshwor RE",
                    "365 Days annual membership"
                ),
                featuresNe = listOf(
                    "३० वटा सक्रिय सम्पत्ति सूची",
                    "प्रति सम्पत्ति २० वटै तस्बिर राख्ने सुविधा",
                    "खोजी र वर्गमा पहिलो प्राथमिकता",
                    "पञ्चेश्वर रियल इस्टेटबाट विशेष सहयोग",
                    "३६५ दिन (१ वर्ष) को पूर्ण सदस्यता"
                )
            )
        )
    )
    val membershipPlans: StateFlow<List<MembershipPlan>> = _membershipPlans.asStateFlow()

    private val _payments = MutableStateFlow(
        listOf(
            PaymentRecord(
                id = "TXN_784912",
                userId = "user_buyer_1",
                userName = "Nirbhay Chand",
                userPhone = "+977 9848712345",
                planId = "plan_standard",
                planName = "Standard Pro (90 Days)",
                provider = PaymentProvider.ESEWA,
                amountNpr = 3500,
                providerReference = "ESEWA_9921401827",
                status = PaymentStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000),
                verifiedAt = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000)
            ),
            PaymentRecord(
                id = "TXN_652190",
                userId = "dev_2",
                userName = "Bhimdatta Real Estate Group",
                userPhone = "+977 9858799887",
                planId = "plan_premium",
                planName = "Premium Agency (365 Days)",
                provider = PaymentProvider.KHALTI,
                amountNpr = 8000,
                providerReference = "KHALTI_KP82736192",
                status = PaymentStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - (20L * 24 * 60 * 60 * 1000),
                verifiedAt = System.currentTimeMillis() - (20L * 24 * 60 * 60 * 1000)
            )
        )
    )
    val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(setOf("prop_1", "prop_2"))
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _inquiries = MutableStateFlow(
        listOf(
            Inquiry(
                id = "inq_1",
                propertyId = "prop_1",
                propertyTitle = "Residential Land Plot, Ward 4 Kaluwapur Road",
                buyerName = "Ganesh Bhatta",
                buyerPhone = "+977 9848765432",
                message = "I am interested in this plot. Is the price negotiable per Kattha?",
                inquiryType = InquiryType.GENERAL,
                createdAt = System.currentTimeMillis() - (2L * 3600 * 1000)
            ),
            Inquiry(
                id = "inq_2",
                propertyId = "prop_2",
                propertyTitle = "Modern 2.5 Storey Villa in Bhimdatta-18",
                buyerName = "Sunita Joshi",
                buyerPhone = "+977 9868712398",
                message = "Would love to see the compound and road access in person.",
                inquiryType = InquiryType.SITE_VISIT,
                preferredDate = "18 Oct 2026",
                preferredTime = "11:00 AM",
                createdAt = System.currentTimeMillis() - (5L * 3600 * 1000)
            )
        )
    )
    val inquiries: StateFlow<List<Inquiry>> = _inquiries.asStateFlow()

    private val _properties = MutableStateFlow(createInitialProperties())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private fun createInitialProperties(): List<Property> {
        return listOf(
            Property(
                id = "prop_1",
                title = "Prime Residential Land Plot, Ward 4 Kaluwapur Road",
                titleNe = "आकर्षक आवासीय जग्गा, वडा नं. ४ कालुवापुर सडक",
                type = PropertyType.LAND,
                status = PropertyStatus.AVAILABLE,
                priceNpr = 4500000L, // 45 Lakhs
                areaM2 = 507.95, // Exactly 1 Kattha 10 Dhur (30 Dhur * 16.931579)
                ward = 4,
                tole = "Kaluwapur Road, Ward 4",
                toleNe = "कालुवापुर सडक, वडा नं. ४",
                locationLat = 28.9680,
                locationLng = 80.1820,
                roadAccess = "22 ft Gravel-Pitched Road",
                roadAccessNe = "२२ फिट ग्राभेल कालोपत्रे सडक",
                facing = "East-Facing",
                facingNe = "पूर्व मोहोडा",
                description = "Rectangular flat land plot ready for immediate house construction. Close to main highway and school with continuous electricity and water line available. Perfect Lalpurja clean title.",
                descriptionNe = "तत्काल घर बनाउन योग्य आयताकार समथर जग्गा। मुख्य राजमार्ग र विद्यालय नजिकै, बिजुली र पानीको सुविधा। सफा लालपुर्जा।",
                developerId = "user_buyer_1",
                developerName = "Pancheshwor Approved Lister",
                developerPhone = "+977 9858712345",
                kittaNumber = "512/04",
                lalpurjaVerified = true,
                photos = listOf(
                    PropertyPhoto("ph_1_1", "prop_terai_land_1791944158134", "Plot front view along 22 ft road", isCover = true, order = 1),
                    PropertyPhoto("ph_1_2", "prop_house_mahendra_1791944139592", "Surrounding peaceful residential neighborhood", isCover = false, order = 2),
                    PropertyPhoto("ph_1_3", "prop_commercial_1791944180176", "Just 5 minutes to Bhimdatta Main Bazaar", isCover = false, order = 3)
                ),
                approved = true
            ),
            Property(
                id = "prop_2",
                title = "Modern 2.5 Storey Luxury Villa, Ward 18 (Suda)",
                titleNe = "आधुनिक २.५ तले सुविधासम्पन्न घर, वडा नं. १८ (सुडा)",
                type = PropertyType.HOUSE,
                status = PropertyStatus.AVAILABLE,
                priceNpr = 18500000L, // 1 Crore 85 Lakhs
                areaM2 = 338.63, // 1 Kattha (20 Dhur)
                ward = 18,
                tole = "Suda Chowk, Ward 18",
                toleNe = "सुडा चोक, वडा नं. १८",
                locationLat = 28.9810,
                locationLng = 80.1650,
                roadAccess = "20 ft Blacktopped Road",
                roadAccessNe = "२० फिट कालोपत्रे सडक",
                facing = "North-East",
                facingNe = "उत्तर-पूर्व",
                floors = 3,
                bedrooms = 5,
                bathrooms = 4,
                builtUpAreaM2 = 260.0,
                yearBuilt = 2024,
                parking = "2 Cars + 4 Motorbikes",
                parkingNe = "२ कार र ४ मोटरसाइकल",
                waterSource = "Deep Boring + Filter System",
                waterSourceNe = "डिप बोरिङ + फिल्टर प्रणाली",
                description = "Brand new earthquake resistant engineered villa. Master bedrooms with attached bathrooms, modular kitchen, open rooftop terrace with Himalayan view and fully secured boundary wall with automated gate.",
                descriptionNe = "भूकम्प प्रतिरोधी इन्जिनियरिङ डिजाइनमा निर्मित नयाँ आकर्षक घर। जोडिएका बाथरुम सहितका कोठा, मोड्युलर किचेन र छतबाट मनोरम दृश्य।",
                developerId = "user_buyer_1",
                developerName = "Pancheshwor Approved Lister",
                developerPhone = "+977 9858712345",
                kittaNumber = "88/19",
                lalpurjaVerified = true,
                photos = listOf(
                    PropertyPhoto("ph_2_1", "prop_house_mahendra_1791944139592", "Front villa exterior with parking courtyard", isCover = true, order = 1),
                    PropertyPhoto("ph_2_2", "prop_terai_land_1791944158134", "Surrounding open green area and lawn", isCover = false, order = 2),
                    PropertyPhoto("ph_2_3", "prop_commercial_1791944180176", "Near to main Mahendranagar route", isCover = false, order = 3)
                ),
                approved = true
            ),
            Property(
                id = "prop_3",
                title = "3-Storey Commercial Showroom Building, Bhimdatta Main Bazaar",
                titleNe = "३ तले व्यावसायिक भवन, भीमदत्त मुख्य बजार",
                type = PropertyType.COMMERCIAL,
                status = PropertyStatus.AVAILABLE,
                priceNpr = 35000000L, // 3.5 Crore
                areaM2 = 677.26, // 2 Kattha (40 Dhur)
                ward = 4,
                tole = "Bazaar Main Chowk, Ward 4",
                toleNe = "मुख्य बजार चोक, वडा नं. ४",
                locationLat = 28.9610,
                locationLng = 80.1780,
                roadAccess = "40 ft Main Highway / Market Road",
                roadAccessNe = "४० फिट मुख्य बजार राजमार्ग",
                facing = "South",
                facingNe = "दक्षिण",
                floors = 3,
                builtUpAreaM2 = 550.0,
                parking = "Ample Customer Parking",
                parkingNe = "पर्याप्त ग्राहक पार्किङ",
                waterSource = "Municipal Supply",
                waterSourceNe = "नगरपालिका खानेपानी",
                description = "High rental yield commercial property located in the heart of Mahendranagar trade zone. Ground floor rented to bank branch, upper floors suitable for showrooms or corporate offices.",
                descriptionNe = "महेन्द्रनगरको व्यापारिक केन्द्रमा अवस्थित उच्च भाडा आम्दानी दिने व्यावसायिक भवन। बैंक तथा कर्पोरेट कार्यालयका लागि उपयुक्त।",
                developerId = "dev_2",
                developerName = "Bhimdatta Real Estate Group",
                developerPhone = "+977 9858799887",
                kittaNumber = "312/08",
                lalpurjaVerified = true,
                photos = listOf(
                    PropertyPhoto("ph_3_1", "prop_commercial_1791944180176", "Main bazaar frontage with continuous footfall", isCover = true, order = 1),
                    PropertyPhoto("ph_3_2", "prop_house_mahendra_1791944139592", "Building upper elevation", isCover = false, order = 2)
                ),
                approved = true
            ),
            Property(
                id = "prop_4",
                title = "Fertile Agricultural & Farm Land, Mahakali Irrigation Zone",
                titleNe = "उर्वर कृषि तथा फार्म जग्गा, महाकाली सिँचाइ क्षेत्र",
                type = PropertyType.LAND,
                status = PropertyStatus.AVAILABLE,
                priceNpr = 24000000L, // 2 Crore 40 Lakhs
                areaM2 = 20317.89, // Exactly 3 Bigha (1200 Dhur)
                ward = 9,
                tole = "Brahmadev Road, Ward 9",
                toleNe = "ब्रह्मदेव सडक, वडा नं. ९",
                locationLat = 28.9950,
                locationLng = 80.1450,
                roadAccess = "24 ft Agricultural Feeder Road",
                roadAccessNe = "२४ फिट कृषि फिडर सडक",
                facing = "East",
                facingNe = "पूर्व",
                description = "Spacious 3 Bigha Terai agricultural plot with year-round canal irrigation from Mahakali project. Highly fertile soil suited for paddy, mustard, sugarcane or organic agro-farm venture.",
                descriptionNe = "महाकाली सिँचाइको नियमित पानी सुविधा भएको ३ बिघा उर्वर कृषि जग्गा। धान, तोरी, उखु तथा कृषि फार्मका लागि सर्वोत्तम।",
                developerId = "dev_2",
                developerName = "Bhimdatta Real Estate Group",
                developerPhone = "+977 9858799887",
                kittaNumber = "104/55",
                lalpurjaVerified = true,
                photos = listOf(
                    PropertyPhoto("ph_4_1", "prop_terai_land_1791944158134", "Panoramic view of 3 Bigha farm fields", isCover = true, order = 1),
                    PropertyPhoto("ph_4_2", "prop_house_mahendra_1791944139592", "Access road from highway", isCover = false, order = 2)
                ),
                approved = true
            ),
            Property(
                id = "prop_5",
                title = "Ready-to-Move 2-Storey House, Traffic Chowk, Ward 6",
                titleNe = "तयारी २-तले पक्की घर, ट्राफिक चोक, वडा नं. ६",
                type = PropertyType.HOUSE,
                status = PropertyStatus.AVAILABLE,
                priceNpr = 12000000L, // 1 Crore 20 Lakhs
                areaM2 = 253.97, // 15 Dhur (0.75 Kattha)
                ward = 6,
                tole = "Traffic Chowk, Ward 6",
                toleNe = "ट्राफिक चोक, वडा नं. ६",
                locationLat = 28.9660,
                locationLng = 80.1740,
                roadAccess = "16 ft Pitched Road",
                roadAccessNe = "१६ फिट कालोपत्रे सडक",
                facing = "West",
                facingNe = "पश्चिम",
                floors = 2,
                bedrooms = 4,
                bathrooms = 2,
                builtUpAreaM2 = 180.0,
                yearBuilt = 2022,
                parking = "1 Car Garage",
                parkingNe = "१ कार ग्यारेज",
                description = "Cozy and well-maintained modern residence just 200m from Traffic Chowk. Solid RCC frame pillar structure with separate living, kitchen and spacious sunny balcony.",
                descriptionNe = "ट्राफिक चोकबाट मात्र २०० मिटरको दूरीमा रहेको आकर्षक आवासीय घर। पिल्लर प्रणालीमा निर्मित, घमाइलो बाल्कोनी।",
                developerId = "user_buyer_1",
                developerName = "Pancheshwor Approved Lister",
                developerPhone = "+977 9858712345",
                kittaNumber = "440/16",
                lalpurjaVerified = true,
                photos = listOf(
                    PropertyPhoto("ph_5_1", "prop_house_mahendra_1791944139592", "Main house view and entrance gate", isCover = true, order = 1),
                    PropertyPhoto("ph_5_2", "prop_terai_land_1791944158134", "Courtyard garden area", isCover = false, order = 2)
                ),
                approved = true
            ),
            Property(
                id = "prop_6",
                title = "3BHK Semi-Furnished Modern Apartment, Ward 3",
                titleNe = "३ कोठाको अर्ध-सुसज्जित अपार्टमेन्ट, वडा नं. ३",
                type = PropertyType.APARTMENT,
                status = PropertyStatus.BOOKED,
                priceNpr = 7800000L, // 78 Lakhs
                areaM2 = 135.45, // 8 Dhur
                ward = 3,
                tole = "Hospital Road, Ward 3",
                toleNe = "अस्पताल सडक, वडा नं. ३",
                locationLat = 28.9620,
                locationLng = 80.1760,
                roadAccess = "30 ft Double-lane Road",
                roadAccessNe = "३० फिट दोहोरो सडक",
                facing = "South-East",
                facingNe = "दक्षिण-पूर्व",
                floors = 4,
                bedrooms = 3,
                bathrooms = 2,
                builtUpAreaM2 = 135.45,
                yearBuilt = 2025,
                parking = "Designated Basement Parking",
                parkingNe = "भूमिगत पार्किङ",
                description = "Modern city apartment with lift, backup generator, 24/7 security guard, and water filtration. Booked by client.",
                descriptionNe = "लिफ्ट, ब्याकअप जेनेरेटर र २४ सै घण्टा सुरक्षा गार्ड सहितको सुविधासम्पन्न अपार्टमेन्ट।",
                developerId = "dev_2",
                developerName = "Bhimdatta Real Estate Group",
                developerPhone = "+977 9858799887",
                kittaNumber = "A-302",
                lalpurjaVerified = true,
                photos = listOf(
                    PropertyPhoto("ph_6_1", "prop_commercial_1791944180176", "Complex exterior architecture", isCover = true, order = 1),
                    PropertyPhoto("ph_6_2", "prop_house_mahendra_1791944139592", "Balcony view", isCover = false, order = 2)
                ),
                approved = true
            )
        )
    }

    // Role switcher
    fun setRole(role: UserRole) {
        _currentUser.value = _currentUser.value.copy(
            role = role,
            name = when (role) {
                UserRole.GUEST -> "Guest Visitor"
                UserRole.BUYER -> "Nirbhay Chand (Buyer)"
                UserRole.DEVELOPER -> "Nirbhay Chand (Seller Member)"
                UserRole.ADMIN -> "Pancheshwor Admin"
            }
        )
    }

    fun updateLanguage(lang: AppLanguage) {
        _currentUser.value = _currentUser.value.copy(language = lang)
    }

    fun updateTheme(themeMode: ThemeModePreference) {
        _currentUser.value = _currentUser.value.copy(themeMode = themeMode)
    }

    fun updateNepaliDigits(enabled: Boolean) {
        _currentUser.value = _currentUser.value.copy(nepaliDigits = enabled)
    }

    fun updateDataSaver(enabled: Boolean) {
        _currentUser.value = _currentUser.value.copy(dataSaverMode = enabled)
    }

    fun updateCompanyInfo(info: CompanyInfo) {
        _companyInfo.value = info
    }

    fun toggleFavorite(propertyId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(propertyId)) {
            current.remove(propertyId)
        } else {
            current.add(propertyId)
        }
        _favorites.value = current
    }

    fun addInquiry(
        propertyId: String,
        propertyTitle: String,
        buyerName: String,
        buyerPhone: String,
        message: String,
        type: InquiryType = InquiryType.GENERAL,
        preferredDate: String? = null,
        preferredTime: String? = null
    ) {
        val newInq = Inquiry(
            id = "inq_" + UUID.randomUUID().toString().take(6),
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            buyerName = buyerName.ifBlank { _currentUser.value.name },
            buyerPhone = buyerPhone.ifBlank { _currentUser.value.phone },
            message = message,
            inquiryType = type,
            preferredDate = preferredDate,
            preferredTime = preferredTime,
            createdAt = System.currentTimeMillis()
        )
        _inquiries.value = listOf(newInq) + _inquiries.value
    }

    fun createOrUpdateProperty(property: Property) {
        val list = _properties.value.toMutableList()
        val index = list.indexOfFirst { it.id == property.id }
        if (index >= 0) {
            list[index] = property
        } else {
            list.add(0, property)
        }
        _properties.value = list
    }

    fun approveProperty(propertyId: String) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(approved = true, approvalReason = "Approved by Company Management") else it
        }
    }

    fun rejectProperty(propertyId: String, reason: String) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(approved = false, approvalReason = reason) else it
        }
    }

    fun markStatus(propertyId: String, status: PropertyStatus) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(status = status) else it
        }
    }

    fun deleteProperty(propertyId: String) {
        _properties.value = _properties.value.filter { it.id != propertyId }
    }

    fun executePayment(
        planId: String,
        provider: PaymentProvider,
        onComplete: (PaymentRecord) -> Unit
    ) {
        val plan = _membershipPlans.value.firstOrNull { it.id == planId } ?: return
        val txnId = "TXN_" + (100000 + (Math.random() * 900000).toInt())
        val providerRef = "${provider.name}_" + UUID.randomUUID().toString().replace("-", "").take(10).uppercase()

        val record = PaymentRecord(
            id = txnId,
            userId = _currentUser.value.id,
            userName = _currentUser.value.name,
            userPhone = _currentUser.value.phone.ifBlank { "+977 9848712345" },
            planId = plan.id,
            planName = "${plan.nameEn} (${plan.durationDays} Days)",
            provider = provider,
            amountNpr = plan.priceNpr,
            providerReference = providerRef,
            status = PaymentStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            verifiedAt = System.currentTimeMillis()
        )

        _payments.value = listOf(record) + _payments.value

        // Activate membership for user
        val addedMillis = plan.durationDays.toLong() * 24 * 60 * 60 * 1000L
        _currentUser.value = _currentUser.value.copy(
            role = UserRole.DEVELOPER,
            activeMembershipPlanId = plan.id,
            membershipStatus = "Active",
            membershipEndsAt = System.currentTimeMillis() + addedMillis
        )

        onComplete(record)
    }

    fun refundPayment(paymentId: String) {
        _payments.value = _payments.value.map {
            if (it.id == paymentId) it.copy(status = PaymentStatus.REFUNDED) else it
        }
    }
}
