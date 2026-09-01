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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volatility.predict.data.model.StockItem
import com.volatility.predict.ui.components.MetricCard
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
                    Text(text = "Back to Overview", color = DarkBackground)
                }
            }
        }
        return
    }

    val isYellowish = stock.isImpliedLowerThanPredicted
    val statusColor = if (isYellowish) VolatilityYellowBorder else VolatilityGreenBorder
    val statusText = if (isYellowish) "Yellow Condition (IV < Predicted)" else "Green Condition (IV ≥ Predicted)"
    val statusDescription = if (isYellowish) {
        "The latest market implied volatility (${stock.formattedImpliedVol}) is lower than the predicted 5-day volatility (${stock.formattedPredictedVol}). This indicates potential volatility expansion."
    } else {
        "The latest market implied volatility (${stock.formattedImpliedVol}) is higher than or equal to the predicted 5-day volatility (${stock.formattedPredictedVol})."
    }

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
                        fontSize = 20.sp
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
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stock.symbol,
                                color = TextPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stock.name,
                                color = TextSecondary,
                                fontSize = 14.sp
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Volatility Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Latest Implied Volatility",
                        value = stock.formattedImpliedVol,
                        subtitle = "Options Market IV",
                        valueColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Predicted 5-Day Volatility",
                        value = stock.formattedPredictedVol,
                        subtitle = "Forecast Model",
                        valueColor = if (isYellowish) VolatilityYellowText else VolatilityGreenText,
                        borderColor = statusColor.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spread & Delta Metric Card
                MetricCard(
                    title = "Volatility Delta (Predicted − Implied)",
                    value = stock.formattedSpread,
                    subtitle = if (stock.volSpreadPercent > 0) "Predicted volatility exceeds current market IV" else "Market IV exceeds predicted volatility",
                    valueColor = if (stock.volSpreadPercent > 0) VolatilityYellowText else VolatilityGreenText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Analysis & Signal Interpretation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusDescription,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Prominent "Back" Button requested by user
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
