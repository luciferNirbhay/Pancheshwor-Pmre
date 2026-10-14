package com.example.model

enum class PropertyType(val displayNameEn: String, val displayNameNe: String) {
    LAND("Land", "जग्गा"),
    HOUSE("House", "घर"),
    APARTMENT("Apartment", "अपार्टमेन्ट"),
    COMMERCIAL("Commercial", "व्यापारिक")
}

enum class PropertyStatus(val displayNameEn: String, val displayNameNe: String) {
    AVAILABLE("Available", "उपलब्ध"),
    BOOKED("Booked", "बुकिङ भएको"),
    SOLD("Sold", "बिक्री भइसकेको")
}

data class PropertyPhoto(
    val id: String,
    val url: String, // Can be res drawable name or image url
    val caption: String = "",
    val isCover: Boolean = false,
    val order: Int = 0
)

data class Property(
    val id: String,
    val title: String,
    val titleNe: String = "",
    val type: PropertyType,
    val status: PropertyStatus = PropertyStatus.AVAILABLE,
    val priceNpr: Long, // Price in whole NPR
    val areaM2: Double, // Area stored strictly in m²
    val ward: Int = 4, // Ward number in Bhimdatta Municipality (1-19)
    val tole: String = "Bhimdatta Bazaar",
    val toleNe: String = "भीमदत्त बजार",
    val locationLat: Double = 28.9645,
    val locationLng: Double = 80.1798,
    val roadAccess: String = "20 ft Pitched Road",
    val roadAccessNe: String = "२० फिट कालोपत्रे सडक",
    val facing: String = "East",
    val facingNe: String = "पूर्व",
    // Residential / Commercial fields
    val floors: Int? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val builtUpAreaM2: Double? = null,
    val yearBuilt: Int? = null,
    val parking: String? = null,
    val parkingNe: String? = null,
    val waterSource: String? = "Municipality + Boring",
    val waterSourceNe: String? = "खानेपानी धारा + बोरिङ",
    val description: String = "",
    val descriptionNe: String = "",
    val developerId: String = "dev_1",
    val developerName: String = "Pancheshwor Approved Lister",
    val developerPhone: String = "+977 9858712345",
    // Verification details (Admin & verification badge)
    val kittaNumber: String? = "402/12",
    val lalpurjaVerified: Boolean = true,
    val photos: List<PropertyPhoto> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val approved: Boolean = true,
    val approvalReason: String? = null
)
