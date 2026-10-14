package com.example.model

enum class InquiryType(val titleEn: String, val titleNe: String) {
    GENERAL("Direct Inquiry", "सामान्य सोधपुछ"),
    SITE_VISIT("Site Visit Request", "स्थलगत अवलोकन अनुरोध")
}

data class Inquiry(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val buyerName: String,
    val buyerPhone: String,
    val message: String,
    val inquiryType: InquiryType = InquiryType.GENERAL,
    val preferredDate: String? = null,
    val preferredTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "New" // New, Contacted, Closed
)

data class CompanyInfo(
    val companyName: String = "Pancheshwor Multipurpose and Real Estate Pvt. Ltd.",
    val companyNameNe: String = "पञ्चेश्वर बहुउद्देश्यीय तथा रियल इस्टेट प्रा. लि.",
    val email: String = "info@pancheshworrealestate.com.np",
    val phone: String = "+977-99-520123",
    val whatsAppNumber: String = "+9779858712345",
    val address: String = "Bhimdatta Municipality - 4, Main Bazaar, Kanchanpur, Sudurpashchim, Nepal",
    val addressNe: String = "भीमदत्त नगरपालिका - ४, मुख्य बजार, कञ्चनपुर, सुदूरपश्चिम, नेपाल",
    val openingHours: String = "Sunday - Friday: 9:00 AM - 5:30 PM",
    val openingHoursNe: String = "आइतबार - शुक्रबार: बिहान ९:०० - बेलुकी ५:३०"
)
