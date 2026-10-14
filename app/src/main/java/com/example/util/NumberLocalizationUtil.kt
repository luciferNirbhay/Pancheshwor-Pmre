package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NumberLocalizationUtil {

    private val nepaliDigitsMap = mapOf(
        '0' to '०',
        '1' to '१',
        '2' to '२',
        '3' to '३',
        '4' to '४',
        '5' to '५',
        '6' to '६',
        '7' to '७',
        '8' to '८',
        '9' to '९'
    )

    fun toNepaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(nepaliDigitsMap[ch] ?: ch)
        }
        return sb.toString()
    }

    /**
     * Formats number in South Asian grouping (Lakhs and Crores).
     * Example: 4500000 -> "45,00,000"
     * Example: 12500000 -> "1,25,00,000"
     */
    fun formatSouthAsianNumber(number: Long): String {
        val s = number.toString()
        if (s.length <= 3) return s
        val last3 = s.substring(s.length - 3)
        var rest = s.substring(0, s.length - 3)
        val parts = mutableListOf<String>()
        while (rest.length > 2) {
            val chunk = rest.substring(rest.length - 2)
            parts.add(0, chunk)
            rest = rest.substring(0, rest.length - 2)
        }
        if (rest.isNotEmpty()) {
            parts.add(0, rest)
        }
        return parts.joinToString(",") + "," + last3
    }

    /**
     * Formats price in NPR with lakh/crore grouping or short words.
     * shortForm: true -> "Rs. 45 Lakh" or "रु ४५ लाख"
     */
    fun formatPriceNpr(
        amount: Long,
        shortForm: Boolean = false,
        useNepali: Boolean = false,
        useNepaliDigits: Boolean = false
    ): String {
        val prefix = if (useNepali) "रु. " else "NPR "

        val formattedText = if (shortForm) {
            when {
                amount >= 10_000_000L -> {
                    val cr = amount / 10_000_000.0
                    val unit = if (useNepali) "करोड" else "Crore"
                    String.format(Locale.US, "%.2f %s", cr, unit)
                }
                amount >= 100_000L -> {
                    val lakh = amount / 100_000.0
                    val unit = if (useNepali) "लाख" else "Lakh"
                    String.format(Locale.US, "%.2f %s", lakh, unit)
                }
                amount >= 1_000L -> {
                    val k = amount / 1_000.0
                    val unit = if (useNepali) "हजार" else "Thousand"
                    String.format(Locale.US, "%.1f %s", k, unit)
                }
                else -> amount.toString()
            }
        } else {
            formatSouthAsianNumber(amount)
        }

        val full = "$prefix$formattedText"
        return if (useNepaliDigits || useNepali) toNepaliDigits(full) else full
    }

    /**
     * Approximate English Date + Bikram Sambat (BS) date display for 2026.
     * BS is approximately 56 years 8.5 months ahead of AD.
     */
    fun formatDateWithBS(timestamp: Long, isNepali: Boolean): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val adStr = sdf.format(date)
        // Approximate Bikram Sambat conversion (+56.7 years)
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        val adYear = cal.get(java.util.Calendar.YEAR)
        val adMonth = cal.get(java.util.Calendar.MONTH) // 0-11
        val bsYear = adYear + 57
        val bsMonthsEn = listOf("Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin", "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra")
        val bsMonthsNe = listOf("वैशाख", "जेठ", "असार", "श्रावण", "भाद्र", "आश्विन", "कार्तिक", "मंसिर", "पौष", "माघ", "फाल्गुन", "चैत्र")
        val bsMonthIndex = (adMonth + 8) % 12
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

        return if (isNepali) {
            val bsText = "${toNepaliDigits(day.toString())} ${bsMonthsNe[bsMonthIndex]} ${toNepaliDigits(bsYear.toString())} (वि.सं.)"
            bsText
        } else {
            "$adStr ($day ${bsMonthsEn[bsMonthIndex]} $bsYear BS)"
        }
    }
}
