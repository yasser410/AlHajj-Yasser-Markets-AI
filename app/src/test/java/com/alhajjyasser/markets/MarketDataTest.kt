package com.alhajjyasser.markets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketDataTest {
    @Test
    fun goldGramCalculationUsesTroyOunceConversion() {
        val result = MarketCalculator.goldGramInEgp(3100.0, 50.0)
        assertEquals(4983.365718, result!!, 0.0001)
    }

    @Test
    fun goldGramCalculationRejectsInvalidLiveValues() {
        assertNull(MarketCalculator.goldGramInEgp(-1.0, 50.0))
        assertNull(MarketCalculator.goldGramInEgp(3100.0, 0.0))
        assertNull(MarketCalculator.goldGramInEgp(Double.NaN, 50.0))
    }

    @Test
    fun allSourceActionsResolveToSecureUrls() {
        assertEquals(SourceCatalog.goldApiUrl, SourceCatalog.urlFor("Gold API"))
        assertTrue(SourceCatalog.all.all { it.url.startsWith("https://") })
        assertTrue(SourceCatalog.all.map { it.url }.toSet().size == SourceCatalog.all.size)
    }

    @Test
    fun everyVisibleSourceActionHasAnExpectedSecureDestination() {
        val expectedLabels = setOf(
            "Yahoo Finance",
            "Gold API",
            "ExchangeRate-API",
            "البورصة المصرية",
            "البنك المركزي المصري",
        )
        assertEquals(expectedLabels, SourceCatalog.all.map { it.label }.toSet())
        expectedLabels.forEach { label ->
            val destination = SourceCatalog.urlFor(label)
            assertTrue("رابط $label يجب أن يكون آمناً", destination?.startsWith("https://") == true)
        }
    }
}
