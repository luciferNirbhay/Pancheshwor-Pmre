package com.example.util

import java.util.Locale
import kotlin.math.floor
import kotlin.math.round

data class TeraiArea(
    val bigha: Long,
    val kattha: Long,
    val dhur: Double,
    val totalDhur: Double,
    val totalKattha: Double,
    val totalBigha: Double,
    val totalSqFt: Double,
    val totalM2: Double
) {
    fun toFormattedString(isNepali: Boolean = false): String {
        val parts = mutableListOf<String>()
        if (bigha > 0) {
            val unit = if (isNepali) "बिघा" else "Bigha"
            parts.add("$bigha $unit")
        }
        if (kattha > 0 || bigha > 0) {
            val unit = if (isNepali) "कठ्ठा" else "Kattha"
            parts.add("$kattha $unit")
        }
        val dhurStr = String.format(Locale.US, "%.2f", dhur)
        val dhurUnit = if (isNepali) "धुर" else "Dhur"
        parts.add("$dhurStr $dhurUnit")

        val result = parts.joinToString(" ")
        return if (isNepali) NumberLocalizationUtil.toNepaliDigits(result) else result
    }
}

data class HillArea(
    val ropani: Long,
    val aana: Long,
    val paisa: Long,
    val daam: Double,
    val totalM2: Double
) {
    fun toFormattedString(isNepali: Boolean = false): String {
        val rUnit = if (isNepali) "रोपनी" else "Ropani"
        val aUnit = if (isNepali) "आना" else "Aana"
        val pUnit = if (isNepali) "पैसा" else "Paisa"
        val dUnit = if (isNepali) "दाम" else "Daam"
        val daamStr = String.format(Locale.US, "%.2f", daam)
        val text = "$ropani $rUnit $aana $aUnit $paisa $pUnit $daamStr $dUnit"
        return if (isNepali) NumberLocalizationUtil.toNepaliDigits(text) else text
    }
}

object AreaConverterUtil {
    // 1 Dhur = 182.25 sq ft * 0.09290304 m²/sq ft = 16.931579 m²
    const val DHUR_IN_M2 = 16.931579
    const val DHUR_IN_SQFT = 182.25
    const val M2_TO_SQFT = 10.7639104

    // Hill units
    const val ROPANI_IN_M2 = 508.737047
    const val AANA_IN_M2 = ROPANI_IN_M2 / 16.0
    const val PAISA_IN_M2 = AANA_IN_M2 / 4.0
    const val DAAM_IN_M2 = PAISA_IN_M2 / 4.0

    /**
     * Converts m² to Terai land units (Bigha, Kattha, Dhur) with carry logic.
     */
    fun m2ToTerai(m2: Double): TeraiArea {
        if (m2 <= 0.0) {
            return TeraiArea(0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val totalDhur = m2 / DHUR_IN_M2
        var bigha = floor(totalDhur / 400.0).toLong()
        val remAfterBigha = totalDhur - (bigha * 400.0)
        var kattha = floor(remAfterBigha / 20.0).toLong()
        var dhur = remAfterBigha - (kattha * 20.0)

        // Round Dhur to 2 decimal places
        var roundedDhur = round(dhur * 100.0) / 100.0

        // Carry rule: If dhur rounds to 20.00, carry to kattha
        if (roundedDhur >= 19.995) {
            roundedDhur = 0.0
            kattha += 1
        }
        // Carry rule: If kattha reaches 20, carry to bigha
        if (kattha >= 20) {
            kattha -= 20
            bigha += 1
        }

        val totalKattha = totalDhur / 20.0
        val totalBigha = totalDhur / 400.0
        val totalSqFt = m2 * M2_TO_SQFT

        return TeraiArea(
            bigha = bigha,
            kattha = kattha,
            dhur = roundedDhur,
            totalDhur = totalDhur,
            totalKattha = totalKattha,
            totalBigha = totalBigha,
            totalSqFt = totalSqFt,
            totalM2 = m2
        )
    }

    /**
     * Converts Bigha, Kattha, Dhur to m²
     */
    fun teraiToM2(bigha: Double, kattha: Double, dhur: Double): Double {
        val totalDhur = (bigha * 400.0) + (kattha * 20.0) + dhur
        return totalDhur * DHUR_IN_M2
    }

    /**
     * Converts m² to Hill units (Ropani, Aana, Paisa, Daam)
     */
    fun m2ToHill(m2: Double): HillArea {
        if (m2 <= 0.0) return HillArea(0, 0, 0, 0.0, 0.0)
        var ropani = floor(m2 / ROPANI_IN_M2).toLong()
        val remR = m2 - (ropani * ROPANI_IN_M2)

        var aana = floor(remR / AANA_IN_M2).toLong()
        val remA = remR - (aana * AANA_IN_M2)

        var paisa = floor(remA / PAISA_IN_M2).toLong()
        var daam = (remA - (paisa * PAISA_IN_M2)) / DAAM_IN_M2

        var roundedDaam = round(daam * 100.0) / 100.0
        if (roundedDaam >= 3.995) {
            roundedDaam = 0.0
            paisa += 1
        }
        if (paisa >= 4) {
            paisa -= 4
            aana += 1
        }
        if (aana >= 16) {
            aana -= 16
            ropani += 1
        }

        return HillArea(ropani, aana, paisa, roundedDaam, m2)
    }

    /**
     * Calculate price per Kattha given price in NPR and area in m²
     */
    fun calculatePricePerKattha(priceNpr: Long, areaM2: Double): Long {
        if (areaM2 <= 0.0) return 0L
        val totalKattha = (areaM2 / DHUR_IN_M2) / 20.0
        if (totalKattha <= 0.0) return 0L
        return (priceNpr / totalKattha).toLong()
    }

    /**
     * Calculate price per m²
     */
    fun calculatePricePerM2(priceNpr: Long, areaM2: Double): Long {
        if (areaM2 <= 0.0) return 0L
        return (priceNpr / areaM2).toLong()
    }
}
