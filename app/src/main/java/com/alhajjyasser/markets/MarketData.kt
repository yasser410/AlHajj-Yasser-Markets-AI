package com.alhajjyasser.markets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DataSource(
    val label: String,
    val url: String,
    val description: String,
)

object SourceCatalog {
    const val goldApiUrl = "https://api.gold-api.com/price/XAU"
    const val exchangeRateApiUrl = "https://open.er-api.com/v6/latest/USD"
    const val officialEgxUrl = "https://www.egx.com.eg/en/homepage.aspx"
    const val officialCbeUrl = "https://www.cbe.org.eg/en/economic-research/statistics/cbe-exchange-rates"

    val all = listOf(
        DataSource("Gold API", goldApiUrl, "سعر الذهب الفوري العالمي بالدولار للأونصة."),
        DataSource("ExchangeRate-API", exchangeRateApiUrl, "سعر صرف الدولار مقابل الجنيه المصري."),
        DataSource("البورصة المصرية", officialEgxUrl, "المرجع الرسمي لبيانات البورصة المصرية."),
        DataSource("البنك المركزي المصري", officialCbeUrl, "المرجع الرسمي لأسعار الصرف المعلنة."),
    )

    fun urlFor(label: String): String? = all.firstOrNull { it.label == label }?.url
}

data class GoldRawQuote(
    val ounceUsd: Double,
    val updatedAt: Instant,
)

data class MarketSnapshot(
    val goldGramEgp: Double?,
    val goldOunceUsd: Double?,
    val usdEgp: Double?,
    val updatedAt: Instant?,
    val notes: List<String>,
)

object MarketCalculator {
    private const val gramsPerTroyOunce = 31.1034768

    fun goldGramInEgp(ounceUsd: Double, usdEgp: Double): Double? {
        if (!ounceUsd.isFinite() || !usdEgp.isFinite() || ounceUsd <= 0 || usdEgp <= 0) return null
        return (ounceUsd * usdEgp) / gramsPerTroyOunce
    }
}

class LiveMarketRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(12, TimeUnit.SECONDS)
        .build(),
) {
    suspend fun refresh(): MarketSnapshot = coroutineScope {
        val gold = async { runCatching { fetchGold() } }
        val fx = async { runCatching { fetchUsdEgp() } }
        val goldResult = gold.await()
        val fxResult = fx.await()
        val rawGold = goldResult.getOrNull()
        val usdEgp = fxResult.getOrNull()
        val notes = buildList {
            goldResult.exceptionOrNull()?.let { add("تعذر تحديث سعر الذهب من Gold API.") }
            fxResult.exceptionOrNull()?.let { add("تعذر تحديث سعر الصرف من ExchangeRate-API.") }
            if (rawGold != null && usdEgp != null) add("القيمة المحلية محسوبة من سعر الأونصة وسعر USD/EGP الحيين.")
        }
        MarketSnapshot(
            goldGramEgp = if (rawGold != null && usdEgp != null) MarketCalculator.goldGramInEgp(rawGold.ounceUsd, usdEgp) else null,
            goldOunceUsd = rawGold?.ounceUsd,
            usdEgp = usdEgp,
            updatedAt = rawGold?.updatedAt,
            notes = notes,
        )
    }

    private suspend fun fetchGold(): GoldRawQuote = withContext(Dispatchers.IO) {
        val body = get(SourceCatalog.goldApiUrl)
        val json = JSONObject(body)
        val value = json.getDouble("price")
        require(value > 0) { "سعر الذهب غير صالح." }
        GoldRawQuote(value, Instant.parse(json.getString("updatedAt")))
    }

    private suspend fun fetchUsdEgp(): Double = withContext(Dispatchers.IO) {
        val body = get(SourceCatalog.exchangeRateApiUrl)
        val value = JSONObject(body).getJSONObject("rates").getDouble("EGP")
        require(value > 0) { "سعر الصرف غير صالح." }
        value
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "AlHajjYasserMarkets/1.0")
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            return response.body.string()
        }
    }
}

object MarketFormatting {
    private val arabicLocale = Locale("ar", "EG")

    fun money(value: Double?, fractionDigits: Int = 2): String = value?.let {
        NumberFormat.getNumberInstance(arabicLocale).apply {
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }.format(it)
    } ?: "غير متاح"
}
