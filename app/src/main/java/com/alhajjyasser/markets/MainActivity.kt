package com.alhajjyasser.markets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Navy = Color(0xFF081A33)
private val Gold = Color(0xFFD4A72C)
private val AnalyticalBlue = Color(0xFF1E5AA8)
private val Positive = Color(0xFF14804A)
private val Surface = Color(0xFFF5F7FA)
private val CardGradient = Brush.linearGradient(listOf(Color(0xFF112240), Color(0xFF081A33)))

private enum class AppTab(val label: String) { HOME("الرئيسية"), SOURCES("المصادر"), SETTINGS("الإعدادات") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MarketsApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MarketsApp() {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var snapshot by remember { mutableStateOf<MarketSnapshot?>(null) }
    var loading by remember { mutableStateOf(false) }
    val repository = remember { LiveMarketRepository() }
    val scope = rememberCoroutineScope()
    suspend fun load() {
        loading = true
        snapshot = withContext(Dispatchers.IO) { repository.refresh() }
        loading = false
    }
    LaunchedEffect(Unit) { load() }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = Gold, surface = Surface, background = Surface)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("الحاج ياسر للأسواق", fontWeight = FontWeight.Bold, color = Color.White) },
                    actions = {
                        if (currentTab == AppTab.HOME) {
                            IconButton(onClick = { if (!loading) { scope.launch { load() } } }) {
                                val rotation = rememberInfiniteTransition(label = "spin").animateFloat(
                                    initialValue = 0f, targetValue = if (loading) 360f else 0f,
                                    animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart), label = "spin"
                                )
                                Icon(Icons.Default.Refresh, contentDescription = "تحديث مباشر", tint = Color.White, modifier = Modifier.graphicsLayer { rotationZ = rotation.value })
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Navy),
                )
            },
            bottomBar = { MarketsBottomBar(currentTab, onTabSelected = { currentTab = it }) },
        ) { innerPadding ->
            when (currentTab) {
                AppTab.HOME -> DashboardScreen(innerPadding, snapshot, loading, onRefresh = { if (!loading) scope.launch { load() } })
                AppTab.SOURCES -> SourcesScreen(innerPadding)
                AppTab.SETTINGS -> SettingsScreen(innerPadding)
            }
        }
    }
}

@Composable
private fun MarketsBottomBar(selected: AppTab, onTabSelected: (AppTab) -> Unit) {
    NavigationBar(containerColor = Navy) {
        listOf(AppTab.HOME, AppTab.SOURCES, AppTab.SETTINGS).forEach { tab ->
            val icon = when (tab) { AppTab.HOME -> Icons.Default.Home; AppTab.SOURCES -> Icons.Default.AccountBalance; AppTab.SETTINGS -> Icons.Default.Settings }
            NavigationBarItem(selected = tab == selected, onClick = { onTabSelected(tab) }, icon = { Icon(icon, contentDescription = tab.label) }, label = { Text(tab.label) })
        }
    }
}

@Composable
private fun DashboardScreen(padding: PaddingValues, snapshot: MarketSnapshot?, loading: Boolean, onRefresh: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface).padding(padding),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("لقطة السوق الحية", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(4.dp))
            Text("تُعرض القيم والرسوم فقط عند نجاح استجابة المصدر المباشر.", color = Color(0xFF516175))
        }
        item {
            Hero3DCard(
                title = "ذهب عيار 24 — تقديري / جرام",
                value = "${MarketFormatting.money(snapshot?.goldGramEgp)} ج.م",
                subtitle = "محسوب من ذهب فوري بالدولار وسعر USD/EGP الحيين",
                onSource = { openUrl(context, SourceCatalog.goldApiUrl) }
            )
        }
        snapshot?.series?.forEach { series ->
            item { AnimatedChartCard(series) }
        }
        snapshot?.notes?.takeIf { it.isNotEmpty() }?.let { notes -> item { StatusCard(notes) } }
        item {
            val updated = snapshot?.updatedAt?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("dd MMM، HH:mm", Locale("ar", "EG"))) ?: "لم يكتمل التحديث"
            Text("آخر تحديث: $updated", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF516175), fontSize = 12.sp)
        }
        item {
            Button(onClick = onRefresh, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (loading) "يتم التحديث…" else "تحديث الأسعار")
            }
        }
    }
}

@Composable
private fun Hero3DCard(title: String, value: String, subtitle: String, onSource: () -> Unit) {
    val tilt = rememberInfiniteTransition(label = "tilt").animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "tilt"
    )
    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { rotationX = tilt.value; rotationY = tilt.value * 0.5f; cameraDistance = 12f * density },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(Modifier.fillMaxWidth().background(CardGradient).padding(24.dp)) {
            Column {
                Text(title, color = Color(0xAAFFFFFF), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(value, color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(subtitle, color = Color(0x88FFFFFF), fontSize = 12.sp)
            }
            IconButton(onClick = onSource, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.OpenInNew, contentDescription = "فتح المصدر", tint = Color(0xAAFFFFFF))
            }
        }
    }
}

@Composable
private fun AnimatedChartCard(series: MarketSeries) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(series) { progress.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing)) }
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(series.instrument.name, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${series.instrument.symbol} • ${series.instrument.category}", color = Color(0xFF68788A), fontSize = 12.sp)
                }
                Text("${MarketFormatting.money(series.points.lastOrNull()?.close)} ${series.instrument.currency}", color = AnalyticalBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                if (series.points.size < 2) return@Canvas
                val min = series.points.minOf { it.close }
                val max = series.points.maxOf { it.close }
                val range = (max - min).coerceAtLeast(0.01)
                val stepX = size.width / (series.points.size - 1)
                val path = Path()
                series.points.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = size.height - ((point.close - min) / range * size.height).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                clipRect(right = size.width * progress.value) {
                    drawPath(path, color = AnalyticalBlue, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }
}

@Composable
private fun StatusCard(notes: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E3)), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("حالة المصادر", fontWeight = FontWeight.Bold, color = Navy)
            notes.forEach { Text("• $it", color = Color(0xFF5E4B15), modifier = Modifier.padding(top = 5.dp)) }
        }
    }
}

@Composable
private fun SourcesScreen(padding: PaddingValues) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize().background(Surface).padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("مصادر البيانات", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy); Spacer(Modifier.height(4.dp)); Text("يمكن فتح كل مصدر مباشرة لمراجعة البيانات وشروط الاستخدام.", color = Color(0xFF516175)) }
        items(SourceCatalog.all) { source ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(source.label, fontWeight = FontWeight.Bold, color = Navy); Spacer(Modifier.height(4.dp)); Text(source.description, color = Color(0xFF516175), fontSize = 13.sp) }
                    IconButton(onClick = { openUrl(context, source.url) }) { Icon(Icons.Default.OpenInNew, contentDescription = "فتح ${source.label}", tint = AnalyticalBlue) }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Surface).padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("الإعدادات", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy) }
        item { Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("شفافية البيانات", fontWeight = FontWeight.Bold, color = Navy); Spacer(Modifier.height(7.dp)); Text("لا يخزّن التطبيق أرقام أسعار ثابتة. عند تعذر المصدر، يعرض الحالة «غير متاح» بدلاً من بيانات قديمة أو مُختلقة.", color = Color(0xFF516175)) } } }
        item { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E3)), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("إخلاء مسؤولية", fontWeight = FontWeight.Bold, color = Navy); Spacer(Modifier.height(7.dp)); Text("هذه المعلومات للمتابعة والبحث فقط وليست نصيحة استثمارية شخصية أو دعوة للشراء أو البيع.", color = Color(0xFF5E4B15)) } } }
    }
}

private fun openUrl(context: android.content.Context, url: String) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
