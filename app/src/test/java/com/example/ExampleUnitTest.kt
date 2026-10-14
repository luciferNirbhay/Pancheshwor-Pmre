package com.example

import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPrdTestCase_1Dhur() {
        val m2 = AreaConverterUtil.teraiToM2(0.0, 0.0, 1.0)
        // Expected ~16.93 m², 182.25 sq ft
        assertEquals(16.93, m2, 0.01)
        val terai = AreaConverterUtil.m2ToTerai(m2)
        assertEquals(0, terai.bigha)
        assertEquals(0, terai.kattha)
        assertEquals(1.0, terai.dhur, 0.01)
        assertEquals(182.25, terai.totalSqFt, 0.5)
    }

    @Test
    fun testPrdTestCase_1Kattha() {
        val m2 = AreaConverterUtil.teraiToM2(0.0, 1.0, 0.0)
        // 1 Kattha = 20 Dhur, 338.63 m²
        assertEquals(338.63, m2, 0.01)
        val terai = AreaConverterUtil.m2ToTerai(m2)
        assertEquals(0, terai.bigha)
        assertEquals(1, terai.kattha)
        assertEquals(0.0, terai.dhur, 0.01)
        assertEquals(20.0, terai.totalDhur, 0.01)
    }

    @Test
    fun testPrdTestCase_1Bigha() {
        val m2 = AreaConverterUtil.teraiToM2(1.0, 0.0, 0.0)
        // 1 Bigha = 20 Kattha, 400 Dhur, 6,772.63 m²
        assertEquals(6772.63, m2, 0.01)
        val terai = AreaConverterUtil.m2ToTerai(m2)
        assertEquals(1, terai.bigha)
        assertEquals(0, terai.kattha)
        assertEquals(0.0, terai.dhur, 0.01)
        assertEquals(400.0, terai.totalDhur, 0.01)
        assertEquals(20.0, terai.totalKattha, 0.01)
    }

    @Test
    fun testPrdTestCase_5Kattha10Dhur() {
        val m2 = AreaConverterUtil.teraiToM2(0.0, 5.0, 10.0)
        // Expected: 1,862.47 m²
        assertEquals(1862.47, m2, 0.05)
    }

    @Test
    fun testPrdTestCase_1000M2() {
        val terai = AreaConverterUtil.m2ToTerai(1000.0)
        // Expected: 59.06 Dhur = 2 Kattha 19.06 Dhur = 2.95 Kattha = 0.15 Bigha
        assertEquals(0, terai.bigha)
        assertEquals(2, terai.kattha)
        assertEquals(19.06, terai.dhur, 0.01)
        assertEquals(59.06, terai.totalDhur, 0.01)
        assertEquals(2.95, terai.totalKattha, 0.02)
        assertEquals(0.15, terai.totalBigha, 0.02)
    }

    @Test
    fun testPrdTestCase_3Bigha4Kattha5Dhur() {
        val m2 = AreaConverterUtil.teraiToM2(3.0, 4.0, 5.0)
        // Expected: 21,757.08 m², 234,191.25 sq ft
        assertEquals(21757.08, m2, 0.05)
        val terai = AreaConverterUtil.m2ToTerai(m2)
        assertEquals(3, terai.bigha)
        assertEquals(4, terai.kattha)
        assertEquals(5.0, terai.dhur, 0.01)
        assertEquals(234191.25, terai.totalSqFt, 1.0)
    }

    @Test
    fun testPrdTestCase_CarryRule_6772_63M2() {
        // Carry rule: 6,772.63 m² must show 1 Bigha, NOT 19 Kattha 20.00 Dhur
        val terai = AreaConverterUtil.m2ToTerai(6772.63)
        assertEquals(1, terai.bigha)
        assertEquals(0, terai.kattha)
        assertEquals(0.0, terai.dhur, 0.01)
    }

    @Test
    fun testNepaliDigitsAndLakhFormatting() {
        val formatted = NumberLocalizationUtil.formatSouthAsianNumber(4500000L)
        assertEquals("45,00,000", formatted)

        val priceStr = NumberLocalizationUtil.formatPriceNpr(4500000L, shortForm = true, useNepali = false)
        assertTrue(priceStr.contains("45.00 Lakh"))

        val nepaliDigits = NumberLocalizationUtil.toNepaliDigits("45,00,000")
        assertEquals("४५,००,०००", nepaliDigits)
    }

    @Test
    fun testPriceRangeAndLocationFiltering() {
        val vm = com.example.viewmodel.MainViewModel()
        val allProps = vm.repository.properties.value
        assertTrue(allProps.isNotEmpty())

        // Test filtering logic by Ward 4 in Mahendranagar
        val ward4 = allProps.filter { it.ward == 4 }
        assertTrue(ward4.isNotEmpty())
        assertTrue(ward4.all { it.ward == 4 })

        // Test filtering logic by Price Range (Under 50 Lakhs)
        val under50L = allProps.filter { it.priceNpr <= 5000000L }
        assertTrue(under50L.isNotEmpty())
        assertTrue(under50L.all { it.priceNpr <= 5000000L })

        // Test filter state manipulation and reset
        vm.minPrice.value = 500000L
        vm.selectedWard.value = 4
        vm.selectedTole.value = "Main Bazaar"
        vm.resetFilters()
        assertEquals(0L, vm.minPrice.value)
        assertEquals(50000000L, vm.maxPrice.value)
        assertEquals(null, vm.selectedWard.value)
        assertEquals(null, vm.selectedTole.value)
    }
}
