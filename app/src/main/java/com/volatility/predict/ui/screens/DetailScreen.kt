package com.volatility.predict.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volatility.predict.data.model.HorizonMove
import com.volatility.predict.data.model.StockItem
import com.volatility.predict.data.model.StockStats
import com.volatility.predict.data.model.WeekdayStat
import com.volatility.predict.ui.theme.AccentCyan
import com.volatility.predict.ui.theme.DarkBackground
import com.volatility.predict.ui.theme.DarkSurface
import com.volatility.predict.ui.theme.DarkSurfaceElevated
import com.volatility.predict.ui.theme.TextMuted
import com.volatility.predict.ui.theme.TextPrimary
import com.volatility.predict.ui.theme.TextSecondary
import com.volatility.predict.ui.theme.VolatilityGreenBorder
import com.volatility.predict.ui.theme.VolatilityGreenText
import com.volatility.predict.ui.theme.VolatilityYellowBorder
import com.volatility.predict.ui.theme.VolatilityYellowText

private val ColorGain = Color(0xFF10B981)
private val ColorLoss = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    stock: StockItem?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (stock == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No stock selected", color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text(text = "Back", color = DarkBackground)
                }
            }
        }
        return
    }

    val stats = stock.stats ?: StockStats()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stock.symbol,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header Block: Title & exact requested subtitle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stock.symbol,
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stock.name,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Current Price",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = stock.formattedPrice,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Subtitle specified by user
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "daily stats based on last 365",
                                color = AccentCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Multi-Horizon Max Movement (5, 30, 90 Days in both directions)
                Text(
                    text = "MAX MOVEMENT HORIZONS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        HorizonRow(stats.horizon5d)
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizonRow(stats.horizon30d)
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizonRow(stats.horizon90d)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Daily Gain / Loss Profile
                Text(
                    text = "DAILY RETURN PROFILE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Daily Median",
                        value = stats.formattedDailyMedian,
                        subtitle = "All trading days",
                        valueColor = if (stats.dailyMedianReturn >= 0) ColorGain else ColorLoss,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Median Gain",
                        value = stats.formattedMedianGain,
                        subtitle = "On positive days",
                        valueColor = ColorGain,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Median Loss",
                        value = stats.formattedMedianLoss,
                        subtitle = "On negative days",
                        valueColor = ColorLoss,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Probabilities & Streaks
                Text(
                    text = "DIRECTIONAL PROBABILITIES",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProbabilityRow(
                            label = "Prob. to Close Green (vs Prev Day)",
                            percentage = stats.greenDayProbability,
                            color = ColorGain
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProbabilityRow(
                            label = "Prob. of 2 Consecutive Gain Days",
                            percentage = stats.consecutive2DayGainProbability,
                            color = ColorGain
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProbabilityRow(
                            label = "Prob. of 2 Consecutive Loss Days",
                            percentage = stats.consecutive2DayLossProbability,
                            color = ColorLoss
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Weekday-by-Weekday Breakdown
                Text(
                    text = "WEEKDAY PERFORMANCE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        stats.weekdayStats.forEachIndexed { index, w ->
                            WeekdayItemRow(w)
                            if (index < stats.weekdayStats.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Prominent "Back" Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = DarkBackground
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HorizonRow(move: HorizonMove) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = move.horizon,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Max rolling window",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Max Gain", color = TextMuted, fontSize = 10.sp)
                Text(
                    text = move.formattedMaxGain,
                    color = ColorGain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Max Loss", color = TextMuted, fontSize = 10.sp)
                Text(
                    text = move.formattedMaxLoss,
                    color = ColorLoss,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ProbabilityRow(label: String, percentage: Double, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextSecondary, fontSize = 12.sp)
            Text(
                text = String.format("%.1f%%", percentage),
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0x22FFFFFF),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun WeekdayItemRow(w: WeekdayStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x3338BDF8),
                modifier = Modifier.size(36.dp, 24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = w.day,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = w.dayName, color = TextPrimary, fontSize = 13.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Avg: ${w.formattedAvgReturn}",
                color = if (w.avgReturn >= 0) ColorGain else ColorLoss,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Win: ${w.formattedGreenProb}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
