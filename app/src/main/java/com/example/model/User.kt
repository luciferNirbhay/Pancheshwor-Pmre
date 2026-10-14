package com.example.model

enum class UserRole(val titleEn: String, val titleNe: String) {
    GUEST("Guest", "अतिथि"),
    BUYER("Buyer", "खरिदकर्ता"),
    DEVELOPER("Developer (Seller Member)", "डेभलपर / बिक्रेता सदस्य"),
    ADMIN("Admin", "प्रशासक")
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    NEPALI("ne", "नेपाली")
}

enum class ThemeModePreference {
    SYSTEM, LIGHT, DARK
}

data class UserProfile(
    val id: String = "guest_user",
    val name: String = "Guest Visitor",
    val phone: String = "",
    val role: UserRole = UserRole.BUYER,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeModePreference = ThemeModePreference.SYSTEM,
    val nepaliDigits: Boolean = false,
    val dataSaverMode: Boolean = false,
    val activeMembershipPlanId: String? = "plan_standard",
    val membershipStatus: String = "Active", // Active, Expired, None
    val membershipEndsAt: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000) // 90 days from now
)
