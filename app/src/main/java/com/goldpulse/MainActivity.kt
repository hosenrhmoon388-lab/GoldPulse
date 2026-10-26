package com.goldpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldPulseTheme {
                GoldPulseApp()
            }
        }
    }
}

private data class MarketSnapshot(
    val symbol: String = "XAUUSD",
    val bid: Double = 2385.32,
    val ask: Double = 2385.52,
    val mid: Double = 2385.42,
    val spread: Double = 0.20,
    val timestamp: Instant = Instant.now(),
    val isLive: Boolean = true,
    val h1Trend: String = "صاعد",
    val m15Signal: String = "شراء",
    val riskPercent: Double = 1.0,
    val confidence: Int = 82,
    val failureRate: Int = 23,
    val entry: Double = 2385.42
)

private data class ActivityLogEntry(
    val time: String,
    val type: String,
    val detail: String,
    val level: String = "info"
)

private enum class BottomTab(val label: String) {
    MONITOR("المراقبة"),
    NOTIFICATIONS("الإشعارات"),
    ACTIVITY("سجل النشاط")
}

@Composable
fun GoldPulseApp() {
    val market = remember {
        mutableStateOf(
            MarketSnapshot(
                timestamp = Instant.now().minusSeconds(8),
                isLive = true,
                h1Trend = "صاعد",
                m15Signal = "شراء",
                riskPercent = 1.0,
                confidence = 82,
                failureRate = 23,
                entry = 2385.42
            )
        )
    }

    val riskOptions = listOf(1.0, 2.0, 5.0, 10.0)
    var selectedRisk by remember { mutableStateOf(1.0) }
    var selectedSignal by remember { mutableStateOf("شراء") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var reviewDialogVisible by remember { mutableStateOf(false) }

    val activityLog = remember {
        mutableStateListOf(
            ActivityLogEntry(
                time = "06:12",
                type = "إشارة",
                detail = "شراء - H1 صاعد + M15 شراء",
                level = "success"
            ),
            ActivityLogEntry(
                time = "06:14",
                type = "مراجعة يدوي",
                detail = "تمت مراجعة الأمر المعلّق يدويًا",
                level = "info"
            ),
            ActivityLogEntry(
                time = "06:18",
                type = "أخبار",
                detail = "تحذير: الأخبار مرتفعة التأثير",
                level = "warning"
            )
        )
    }

    val statusText = when {
        abs(Instant.now().epochSecond - market.value.timestamp.epochSecond) > 15 -> "متأخر"
        !market.value.isLive -> "غير متصل"
        else -> "مباشر"
    }

    val statusColor = when (statusText) {
        "مباشر" -> GoldPulsePalette.Success
        "متأخر" -> GoldPulsePalette.Warning
        else -> GoldPulsePalette.Error
    }

    val priceValue = market.value.mid
    val entryValue = when (selectedSignal) {
        "شراء" -> priceValue + 4.0
        "بيع" -> priceValue - 4.0
        else -> priceValue
    }

    val slValue = when (selectedSignal) {
        "شراء" -> entryValue - 18.0
        "بيع" -> entryValue + 18.0
        else -> entryValue
    }

    val tp1Value = when (selectedSignal) {
        "شراء" -> entryValue + 30.0
        "بيع" -> entryValue - 30.0
        else -> entryValue
    }

    val tp2Value = when (selectedSignal) {
        "شراء" -> entryValue + 52.0
        "بيع" -> entryValue - 52.0
        else -> entryValue
    }

    val tp3Value = when (selectedSignal) {
        "شراء" -> entryValue + 76.0
        "بيع" -> entryValue - 76.0
        else -> entryValue
    }

    val riskWarning = selectedRisk == 10.0

    fun addActivityEntry(type: String, detail: String, level: String = "info") {
        activityLog.add(
            0,
            ActivityLogEntry(
                time = formattedNow(),
                type = type,
                detail = detail,
                level = level
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GoldPulse",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPulsePalette.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoldPulsePalette.Background,
                    titleContentColor = GoldPulsePalette.TextPrimary
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = GoldPulsePalette.Surface,
                tonalElevation = 4.dp
            ) {
                BottomTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    BottomTab.MONITOR -> Icons.Outlined.TrendingUp
                                    BottomTab.NOTIFICATIONS -> Icons.Filled.Notifications
                                    BottomTab.ACTIVITY -> Icons.Filled.Report
                                },
                                contentDescription = null,
                                tint = if (selectedTab == index) GoldPulsePalette.Gold else GoldPulsePalette.TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (selectedTab == index) GoldPulsePalette.Gold else GoldPulsePalette.TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPulsePalette.Gold,
                            selectedTextColor = GoldPulsePalette.Gold,
                            indicatorColor = GoldPulsePalette.SurfaceHighlight,
                            unselectedIconColor = GoldPulsePalette.TextSecondary,
                            unselectedTextColor = GoldPulsePalette.TextSecondary
                        )
                    )
                }
            }
        },
        containerColor = GoldPulsePalette.Background
    ) { paddingValues ->
        when (selectedTab) {
            0 -> MonitorScreen(
                modifier = Modifier.padding(paddingValues),
                market = market.value,
                statusText = statusText,
                statusColor = statusColor,
                selectedSignal = selectedSignal,
                onSignalSelected = { selectedSignal = it },
                selectedRisk = selectedRisk,
                riskOptions = riskOptions,
                onRiskSelected = { selectedRisk = it },
                riskWarning = riskWarning,
                entryValue = entryValue,
                slValue = slValue,
                tp1Value = tp1Value,
                tp2Value = tp2Value,
                tp3Value = tp3Value,
                confidence = market.value.confidence,
                failureRate = market.value.failureRate,
                onReviewClick = { reviewDialogVisible = true },
                onRiskChanged = {
                    selectedRisk = it
                    addActivityEntry("تغيير المخاطرة", "تم تعديل المخاطرة إلى ${it.toInt()}%", "info")
                },
                addActivityEntry = ::addActivityEntry,
                activityLog = activityLog
            )
            1 -> NotificationsScreen(
                modifier = Modifier.padding(paddingValues),
                market = market.value,
                onNews = { addActivityEntry("أخبار", "تنبيه: الأخبار مرتفعة التأثير", "warning") }
            )
            2 -> ActivityLogScreen(
                modifier = Modifier.padding(paddingValues),
                activity = activityLog
            )
        }

        if (reviewDialogVisible) {
            ManualReviewSheet(
                direction = selectedSignal,
                entry = entryValue,
                stopLoss = slValue,
                tp1 = tp1Value,
                tp2 = tp2Value,
                tp3 = tp3Value,
                riskPercent = selectedRisk,
                onDismiss = { reviewDialogVisible = false },
                onConfirm = {
                    addActivityEntry("مراجعة يدوي", "تمت مراجعة الأمر المعلّق فقط — لم يتم إرسال أي طلب إلى MT5", "success")
                    reviewDialogVisible = false
                }
            )
        }
    }
}

@Composable
private fun MonitorScreen(
    modifier: Modifier = Modifier,
    market: MarketSnapshot,
    statusText: String,
    statusColor: Color,
    selectedSignal: String,
    onSignalSelected: (String) -> Unit,
    selectedRisk: Double,
    riskOptions: List<Double>,
    onRiskSelected: (Double) -> Unit,
    riskWarning: Boolean,
    entryValue: Double,
    slValue: Double,
    tp1Value: Double,
    tp2Value: Double,
    tp3Value: Double,
    confidence: Int,
    failureRate: Int,
    onReviewClick: () -> Unit,
    onRiskChanged: (Double) -> Unit,
    addActivityEntry: (String, String, String) -> Unit,
    activityLog: List<ActivityLogEntry>
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MarketSummaryCard(
                symbol = market.symbol,
                price = market.mid,
                statusText = statusText,
                statusColor = statusColor,
                h1Trend = market.h1Trend,
                m15Signal = market.m15Signal,
                isDelayed = statusText == "متأخر"
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldPulsePalette.Surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اختيار الحالة",
                        color = GoldPulsePalette.TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SignalTypeButton("شراء", selectedSignal == "شراء") { onSignalSelected("شراء") }
                        SignalTypeButton("بيع", selectedSignal == "بيع") { onSignalSelected("بيع") }
                        SignalTypeButton("انتظار", selectedSignal == "انتظار") { onSignalSelected("انتظار") }
                    }

                    Divider(color = GoldPulsePalette.Border)

                    Text(
                        text = "إدارة المخاطر",
                        color = GoldPulsePalette.TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        riskOptions.forEach { option ->
                            RiskOptionButton(
                                percent = option,
                                selected = selectedRisk == option,
                                onClick = {
                                    onRiskSelected(option)
                                    onRiskChanged(option)
                                }
                            )
                        }
                    }

                    if (riskWarning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = GoldPulsePalette.Error.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = GoldPulsePalette.Error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تحذير: مخاطرة 10% عالية وقد تعرض جزءاً كبيراً من الرصيد للخطر.",
                                color = GoldPulsePalette.Error,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricText("سعر دخول مقترح", entryValue)
                        MetricText("Stop Loss", slValue)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricText("TP1", tp1Value)
                        MetricText("TP2", tp2Value)
                        MetricText("TP3", tp3Value)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricText("درجة ثقة", confidence.toDouble(), suffix = "%")
                        MetricText("نسبة فشل", failureRate.toDouble(), suffix = "%")
                    }

                    Text(
                        text = "هذه النسب تقديرية وليست نتائج Backtest ولا ضماناً للربح.",
                        color = GoldPulsePalette.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            NewsImpactCard()
        }

        item {
            Button(
                onClick = onReviewClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPulsePalette.Gold
                )
            ) {
                Text(text = "مراجعة الأمر المعلّق", color = GoldPulsePalette.Background)
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GoldPulsePalette.Surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "الحالة: ${statusText} • الرصيد: 200 USD • مخاطرة: ${selectedRisk.toInt()}%",
                    color = GoldPulsePalette.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            if (statusText == "متأخر") {
                Text(
                    text = "البيانات متأخرة — لا تعتمد عليها لاتخاذ قرار.",
                    color = GoldPulsePalette.Warning,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Text(
                text = "آخر النشاط",
                color = GoldPulsePalette.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }

        items(activityLog.take(5)) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GoldPulsePalette.Surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = entry.type, color = GoldPulsePalette.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = entry.detail, color = GoldPulsePalette.TextSecondary)
                }
                Text(text = entry.time, color = GoldPulsePalette.Gold)
            }
        }
    }
}

@Composable
private fun NotificationsScreen(
    modifier: Modifier = Modifier,
    market: MarketSnapshot,
    onNews: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldPulsePalette.Surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "أخبار مرتفعة التأثير",
                        color = GoldPulsePalette.TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "قد تسبب الأخبار ارتفاع التذبذب واتساع السبريد. راجع البيانات قبل اتخاذ القرار.",
                        color = GoldPulsePalette.TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNews,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPulsePalette.Gold)
                    ) {
                        Text(text = "تسجيل تنبيه الأخبار", color = GoldPulsePalette.Background)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldPulsePalette.SurfaceHighlight),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "حالة السوق",
                        color = GoldPulsePalette.TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "رمز: ${market.symbol}", color = GoldPulsePalette.TextSecondary)
                    Text(text = "السعر الحالي: ${market.mid}", color = GoldPulsePalette.TextSecondary)
                    Text(text = "اتجاه H1: ${market.h1Trend}", color = GoldPulsePalette.TextSecondary)
                    Text(text = "تأكيد M15: ${market.m15Signal}", color = GoldPulsePalette.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ActivityLogScreen(
    modifier: Modifier = Modifier,
    activity: List<ActivityLogEntry>
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(activity) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GoldPulsePalette.Surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.type, color = GoldPulsePalette.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = item.detail, color = GoldPulsePalette.TextSecondary)
                }
                Text(text = item.time, color = GoldPulsePalette.Gold)
            }
        }
    }
}

@Composable
private fun MarketSummaryCard(
    symbol: String,
    price: Double,
    statusText: String,
    statusColor: Color,
    h1Trend: String,
    m15Signal: String,
    isDelayed: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GoldPulsePalette.Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symbol,
                    color = GoldPulsePalette.Gold,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .background(color = statusColor, shape = CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = statusText, color = GoldPulsePalette.Background, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = String.format(Locale("ar"), "%.2f", price),
                color = GoldPulsePalette.TextPrimary,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InformativeField(label = "اتجاه H1", value = h1Trend)
                InformativeField(label = "تأكيد M15", value = m15Signal)
            }

            if (isDelayed) {
                Text(
                    text = "البيانات متأخرة — لا تعتمد عليها لاتخاذ قرار.",
                    color = GoldPulsePalette.Warning,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InformativeField(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .background(
                color = GoldPulsePalette.SurfaceHighlight,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = GoldPulsePalette.TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(text = value, color = GoldPulsePalette.TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SignalTypeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GoldPulsePalette.Gold else GoldPulsePalette.SurfaceHighlight,
            contentColor = if (selected) GoldPulsePalette.Background else GoldPulsePalette.TextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = label)
    }
}

@Composable
private fun RiskOptionButton(percent: Double, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GoldPulsePalette.Gold else GoldPulsePalette.SurfaceHighlight,
            contentColor = if (selected) GoldPulsePalette.Background else GoldPulsePalette.TextPrimary
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = "${percent.toInt()}%")
    }
}

@Composable
private fun MetricText(label: String, value: Double, suffix: String = "") {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .background(
                color = GoldPulsePalette.SurfaceHighlight,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = GoldPulsePalette.TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(
            text = "${String.format(Locale("ar"), "%.2f", value)}$suffix",
            color = GoldPulsePalette.TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NewsImpactCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = GoldPulsePalette.Surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = GoldPulsePalette.Gold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "أخبار مرتفعة التأثير",
                    color = GoldPulsePalette.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "قد تسبب الأخبار ارتفاع التذبذب واتساع السبريد. راجع البيانات قبل اتخاذ القرار.",
                color = GoldPulsePalette.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualReviewSheet(
    direction: String,
    entry: Double,
    stopLoss: Double,
    tp1: Double,
    tp2: Double,
    tp3: Double,
    riskPercent: Double,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var approved by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = GoldPulsePalette.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "مراجعة الأمر المعلّق",
                color = GoldPulsePalette.TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            ReviewField("الاتجاه", direction)
            ReviewField("سعر الدخول", String.format(Locale("ar"), "%.2f", entry))
            ReviewField("Stop Loss", String.format(Locale("ar"), "%.2f", stopLoss))
            ReviewField("TP1", String.format(Locale("ar"), "%.2f", tp1))
            ReviewField("TP2", String.format(Locale("ar"), "%.2f", tp2))
            ReviewField("TP3", String.format(Locale("ar"), "%.2f", tp3))
            ReviewField("نسبة المخاطرة", "${riskPercent.toInt()}%")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(checked = approved, onCheckedChange = { approved = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "أؤكد أنني راجعت الأمر يدويًا فقط.",
                    color = GoldPulsePalette.TextPrimary
                )
            }

            Text(
                text = "لا يتم تنفيذ أي أمر تلقائيًا. لا يتم إرسال صفقة إلى MT5. يتم تسجيل المراجعة فقط.",
                color = GoldPulsePalette.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Button(
                onClick = onConfirm,
                enabled = approved,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPulsePalette.Gold,
                    disabledContainerColor = GoldPulsePalette.SurfaceHighlight
                )
            ) {
                Text(text = "تأكيد المراجعة", color = GoldPulsePalette.Background)
            }
        }
    }
}

@Composable
private fun ReviewField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = GoldPulsePalette.Surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = GoldPulsePalette.TextSecondary)
        Text(text = value, color = GoldPulsePalette.TextPrimary, fontWeight = FontWeight.Bold)
    }
}

private object GoldPulsePalette {
    val Background = Color(0xFF0D1B2A)
    val Surface = Color(0xFF132B3D)
    val SurfaceHighlight = Color(0xFF1F3C52)
    val Border = Color(0xFF2D4A5E)
    val Gold = Color(0xFFD4AF37)
    val TextPrimary = Color(0xFFE6EAF2)
    val TextSecondary = Color(0xFFB7C3CF)
    val Warning = Color(0xFFFFC857)
    val Error = Color(0xFFFF6B6B)
    val Success = Color(0xFF66D9A9)
}

@Composable
private fun GoldPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            background = GoldPulsePalette.Background,
            surface = GoldPulsePalette.Surface,
            primary = GoldPulsePalette.Gold,
            secondary = GoldPulsePalette.SurfaceHighlight,
            tertiary = GoldPulsePalette.Gold
        ),
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

private fun formattedNow(): String {
    return DateTimeFormatter.ofPattern("HH:mm", Locale("ar")).withZone(ZoneId.systemDefault())
        .format(Instant.now())
}

@Preview(showBackground = true)
@Composable
private fun PreviewGoldPulseApp() {
    GoldPulseTheme {
        GoldPulseApp()
    }
}
