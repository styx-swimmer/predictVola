package com.volatility.predict.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volatility.predict.data.model.StockItem
import com.volatility.predict.ui.theme.TextMuted
import com.volatility.predict.ui.theme.TextPrimary
import com.volatility.predict.ui.theme.TextSecondary
import com.volatility.predict.ui.theme.VolatilityGreenBg
import com.volatility.predict.ui.theme.VolatilityGreenBorder
import com.volatility.predict.ui.theme.VolatilityGreenText
import com.volatility.predict.ui.theme.VolatilityYellowBg
import com.volatility.predict.ui.theme.VolatilityYellowBorder
import com.volatility.predict.ui.theme.VolatilityYellowText

@Composable
fun StockTile(
    stock: StockItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Condition: yellow(ish) if implied < predicted, otherwise green(ish)
    val isYellowish = stock.isImpliedLowerThanPredicted

    val targetBgColor = if (isYellowish) VolatilityYellowBg else VolatilityGreenBg
    val targetBorderColor = if (isYellowish) VolatilityYellowBorder else VolatilityGreenBorder
    val targetAccentColor = if (isYellowish) VolatilityYellowText else VolatilityGreenText

    val backgroundColor by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(400), label = "bg")
    val borderColor by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(400), label = "border")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = targetAccentColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Symbol & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stock.symbol,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = stock.name,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price Tag
                Text(
                    text = stock.formattedPrice,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volatility Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Implied Vol
                Column {
                    Text(
                        text = "Implied IV",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stock.formattedImpliedVol,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Predicted 5D Vol
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Predicted 5D",
                        color = targetAccentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stock.formattedPredictedVol,
                        color = targetAccentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer indicator badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isYellowish) Color(0x33EAB308) else Color(0x3310B981),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isYellowish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = targetAccentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isYellowish) "IV < Predicted" else "IV ≥ Predicted",
                            color = targetAccentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Details",
                        tint = TextSecondary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
