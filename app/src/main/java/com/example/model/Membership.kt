package com.example.model

enum class PaymentProvider(val displayName: String) {
    ESEWA("eSewa"),
    KHALTI("Khalti"),
    MOBILE_BANKING("Mobile Banking")
}

enum class PaymentStatus(val displayNameEn: String, val displayNameNe: String) {
    INITIATED("Initiated", "सुरु गरिएको"),
    PENDING("Pending", "प्रक्रियामा"),
    COMPLETED("Completed", "सफल"),
    FAILED("Failed", "असफल"),
    REFUNDED("Refunded", "फिर्ता गरिएको")
}

data class MembershipPlan(
    val id: String,
    val nameEn: String,
    val nameNe: String,
    val durationDays: Int,
    val priceNpr: Long,
    val maxActiveListings: Int,
    val maxPhotosPerListing: Int,
    val badge: String = "",
    val featuresEn: List<String>,
    val featuresNe: List<String>,
    val isActive: Boolean = true
)

data class PaymentRecord(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val planId: String,
    val planName: String,
    val provider: PaymentProvider,
    val amountNpr: Long,
    val providerReference: String,
    val status: PaymentStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)
