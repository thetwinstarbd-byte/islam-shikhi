package com.islamshikhi.app.feature.zakat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZakatCalculatorTest {

    private val silverPrice = 100.0                       // প্রতি গ্রাম
    private val nisabValue = ZakatCalculator.SILVER_NISAB_GRAMS * silverPrice   // 61,236

    @Test fun parse_banglaAndEnglishDigits() {
        assertEquals(1234.5, ZakatCalculator.parse("১,২৩৪.৫"), 0.0001)
        assertEquals(50.0, ZakatCalculator.parse("50"), 0.0001)
    }

    @Test fun parse_invalidOrNegativeIsZero() {
        assertEquals(0.0, ZakatCalculator.parse("abc"), 0.0)
        assertEquals(0.0, ZakatCalculator.parse(""), 0.0)
        assertEquals(0.0, ZakatCalculator.parse("-5"), 0.0)
    }

    @Test fun belowNisab_noZakat() {
        val r = ZakatCalculator.calculate(ZakatInput(cash = nisabValue - 1, silverPricePerGram = silverPrice))
        assertFalse(r.reachesNisab)
        assertEquals(0.0, r.zakatDue, 0.0)
    }

    @Test fun atNisab_twoAndHalfPercent() {
        val r = ZakatCalculator.calculate(ZakatInput(cash = 100000.0, silverPricePerGram = silverPrice))
        assertTrue(r.reachesNisab)
        assertEquals(2500.0, r.zakatDue, 0.0001)
    }

    @Test fun liabilitiesAreSubtracted() {
        val r = ZakatCalculator.calculate(ZakatInput(cash = 100000.0, liabilities = 50000.0, silverPricePerGram = silverPrice))
        assertEquals(50000.0, r.netWealth, 0.0001)
        assertFalse(r.reachesNisab)
    }

    @Test fun liabilitiesLargerThanAssets_netIsZero() {
        val r = ZakatCalculator.calculate(ZakatInput(cash = 1000.0, liabilities = 5000.0, silverPricePerGram = silverPrice))
        assertEquals(0.0, r.netWealth, 0.0)
    }

    @Test fun missingPrice_noDecision() {
        val r = ZakatCalculator.calculate(ZakatInput(cash = 1_000_000.0))
        assertTrue(r.priceMissing)
        assertFalse(r.reachesNisab)
        assertEquals(0.0, r.zakatDue, 0.0)
    }

    @Test fun goldAndSilverCountedAtGivenPrices() {
        val r = ZakatCalculator.calculate(
            ZakatInput(goldGrams = 10.0, goldPricePerGram = 10000.0, silverGrams = 100.0, silverPricePerGram = silverPrice)
        )
        assertEquals(100000.0 + 10000.0, r.totalAssets, 0.0001)
    }

    @Test fun goldBasisUsesGoldNisab() {
        val goldPrice = 10000.0
        val atNisab = ZakatCalculator.GOLD_NISAB_GRAMS * goldPrice
        val r = ZakatCalculator.calculate(ZakatInput(cash = atNisab, goldPricePerGram = goldPrice, basis = NisabBasis.GOLD))
        assertTrue(r.reachesNisab)
    }
}
