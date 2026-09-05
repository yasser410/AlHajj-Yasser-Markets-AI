package com.alhajjyasser.markets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DataSource(val label: String, val url: String, val description: String)

object SourceCatalog {
    const val goldApiUrl = "https://api.gold-api.com/price/XAU"
    const val exchangeRateApiUrl = "https://open.er-api.com/v6/latest/USD"
    const val yahooChartUrl = "https://query1.finance.yahoo.com/v8/finance/chart/"
    const val officialEgxUrl = "https://www.egx.com.eg/en/homepage.aspx"
    const val officialCbeUrl = "https://www.cbe.org.eg/en/economic-research/statistics/cbe-exchange-rates"
    val all = listOf(
        DataSource("Yahoo Finance", yahooChartUrl, "سلاسل الأسعار التاريخية العامة للأسهم والسندات والذهب."),
        DataSource("Gold API", goldApiUrl, "سعر الذهب الفوري العالمي بالدولار للأونصة."),
        DataSource("ExchangeRate-API", exchangeRateApiUrl, "سعر صرف الدولار مقابل الجنيه المصري."),
        DataSource("البورصة المصرية", officialEgxUrl, "المرجع الرسمي لبيانات البورصة المصرية."),
        DataSource("البنك المركزي المصري", officialCbeUrl, "المرجع الرسمي لأسعار الصرف المعلنة."),
    )
    fun urlFor(label: String): String? = all.firstOrNull { it.label == label }?.url
}

data class MarketInstrument(val symbol: String, val name: String, val category: String, val currency: String)
object InstrumentCatalog {
    val all = listOf(
        MarketInstrument("COMI.CA", "التجاري الدولي", "أسهم مصرية", "EGP"),
        MarketInstrument("SWDY.CA", "السويدي إليكتريك", "أسهم مصرية", "EGP"),
        MarketInstrument("TMGH.CA", "طلعت مصطفى", "أسهم مصرية", "EGP"),
        MarketInstrument("SHY", "سندات خزانة قصيرة", "سندات", "USD"),
        MarketInstrument("IEF", "سندات خزانة متوسطة", "سندات", "USD"),
        MarketInstrument("GC=F", "ذهب COMEX", "ذهب", "USD"),
    )
}

data class MarketPoint(val time: Long, val close: Double)
data class MarketSeries(val instrument: MarketInstrument, val points: List<MarketPoint>, val updatedAt: Instant)
data class GoldRawQuote(val ounceUsd: Double, val updatedAt: Instant)
data class MarketSnapshot(
    val goldGramEgp: Double?, val goldOunceUsd: Double?, val usdEgp: Double?,
    val updatedAt: Instant?, val series: List<MarketSeries>, val notes: List<String>,
)

object MarketCalculator {
    private const val gramsPerTroyOunce = 31.1034768
    fun goldGramInEgp(ounceUsd: Double, usdEgp: Double): Double? =
        if (!ounceUsd.isFinite() || !usdEgp.isFinite() || ounceUsd <= 0 || usdEgp <= 0) null
        else (ounceUsd * usdEgp) / gramsPerTroyOunce
}

class LiveMarketRepository(
    private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build(),
) {
    suspend fun refresh(): MarketSnapshot = coroutineScope {
        val gold = async { runCatching { fetchGold() } }
        val fx = async { runCatching { fetchUsdEgp() } }
        val seriesResults = InstrumentCatalog.all.map { instrument -> async { instrument to runCatching { fetchSeries(instrument) } } }.awaitAll()
        val rawGold = gold.await().getOrNull()
        val usdEgp = fx.await().getOrNull()
        val notes = buildList {
            gold.await().exceptionOrNull()?.let { add("تعذر تحديث سعر الذهب من Gold API.") }
            fx.await().exceptionOrNull()?.let { add("تعذر تحديث سعر الصرف من ExchangeRate-API.") }
            seriesResults.filter { it.second.isFailure }.forEach { add("تعذر تحديث ${it.first.name} من Yahoo Finance.") }
            if (rawGold != null && usdEgp != null) add("القيمة المحلية محسوبة من السعر الفوري وسعر الصرف الحيين.")
        }
        MarketSnapshot(
            if (rawGold != null && usdEgp != null) MarketCalculator.goldGramInEgp(rawGold.ounceUsd, usdEgp) else null,
            rawGold?.ounceUsd, usdEgp, rawGold?.updatedAt,
            seriesResults.mapNotNull { it.second.getOrNull() }, notes,
        )
    }

    private suspend fun fetchGold() = withContext(Dispatchers.IO) {
        val json = JSONObject(get(SourceCatalog.goldApiUrl))
        GoldRawQuote(json.getDouble("price").also { require(it > 0) }, Instant.parse(json.getString("updatedAt")))
    }
    private suspend fun fetchUsdEgp() = withContext(Dispatchers.IO) {
        JSONObject(get(SourceCatalog.exchangeRateApiUrl)).getJSONObject("rates").getDouble("EGP").also { require(it > 0) }
    }
    private suspend fun fetchSeries(instrument: MarketInstrument) = withContext(Dispatchers.IO) {
        val symbol = URLEncoder.encode(instrument.symbol, "UTF-8")
        val json = JSONObject(get("${SourceCatalog.yahooChartUrl}$symbol?range=1mo&interval=1d&events=history"))
        val result = json.getJSONArray("chart").getJSONObject(0)
        val timestamps = result.getJSONArray("timestamp")
        val closes = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0).getJSONArray("close")
        val points = buildList {
            for (index in 0 until timestamps.length()) {
                if (!closes.isNull(index)) add(MarketPoint(timestamps.getLong(index), closes.getDouble(index)))
            }
        }.filter { it.close.isFinite() && it.close > 0 }
        require(points.isNotEmpty()) { "لا توجد نقاط صالحة" }
        MarketSeries(instrument, points, Instant.ofEpochSecond(points.last().time))
    }
    private fun get(url: String): String {
        val request = Request.Builder().url(url).header("Accept", "application/json").header("User-Agent", "AlHajjYasserMarkets/2.0").build()
        client.newCall(request).execute().use { response -> check(response.isSuccessful) { "HTTP ${response.code}" }; return response.body?.string() ?: error("استجابة المصدر فارغة.") }
    }
}

object MarketFormatting {
    private val arabicLocale = Locale("ar", "EG")
    fun money(value: Double?, fractionDigits: Int = 2): String = value?.let { NumberFormat.getNumberInstance(arabicLocale).apply { minimumFractionDigits = fractionDigits; maximumFractionDigits = fractionDigits }.format(it) } ?: "غير متاح"
}
