package com.volatility.predict.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Locale

/**
 * Domain model representing a stock's latest pricing, volatility forecast,
 * and 365-day quantitative return statistics.
 */
data class StockItem(
    val symbol: String = "",
    val name: String = "",
    val currentPrice: Double = 0.0,
    val currency: String = "USD",
    val latestImpliedVolatility: Double = 0.0,
    val predictedVolatility: Double = 0.0,
    val updatedAt: String = "",
    val stats: StockStats? = null
) {
    val isImpliedLowerThanPredicted: Boolean
        get() = latestImpliedVolatility < predictedVolatility

    val formattedPrice: String
        get() = String.format(Locale.US, "$%.2f", currentPrice)

    val formattedImpliedVol: String
        get() = String.format(Locale.US, "%.1f%%", latestImpliedVolatility * 100)

    val formattedPredictedVol: String
        get() = String.format(Locale.US, "%.1f%%", predictedVolatility * 100)

    val volSpreadPercent: Double
        get() = (predictedVolatility - latestImpliedVolatility) * 100

    val formattedSpread: String
        get() = String.format(Locale.US, "%+.1f%%", volSpreadPercent)
}

/**
 * 365-Day quantitative return and multi-horizon movement statistics.
 */
data class StockStats(
    val daysAnalyzed: Int = 252,
    val dailyMedianReturn: Double = 0.0,
    val medianGain: Double = 0.0,
    val medianLoss: Double = 0.0,
    val greenDayProbability: Double = 50.0,
    val consecutive2DayGainProbability: Double = 25.0,
    val consecutive2DayLossProbability: Double = 25.0,
    val horizon5d: HorizonMove = HorizonMove("5 Days", 0.0, 0.0),
    val horizon30d: HorizonMove = HorizonMove("30 Days", 0.0, 0.0),
    val horizon90d: HorizonMove = HorizonMove("90 Days", 0.0, 0.0),
    val weekdayStats: List<WeekdayStat> = emptyList()
) {
    val formattedDailyMedian: String
        get() = String.format(Locale.US, "%+.2f%%", dailyMedianReturn)

    val formattedMedianGain: String
        get() = String.format(Locale.US, "+%.2f%%", medianGain)

    val formattedMedianLoss: String
        get() = String.format(Locale.US, "%.2f%%", medianLoss)

    val formattedGreenProb: String
        get() = String.format(Locale.US, "%.1f%%", greenDayProbability)

    val formattedConsecGainProb: String
        get() = String.format(Locale.US, "%.1f%%", consecutive2DayGainProbability)

    val formattedConsecLossProb: String
        get() = String.format(Locale.US, "%.1f%%", consecutive2DayLossProbability)
}

data class HorizonMove(
    val horizon: String = "",
    val maxGain: Double = 0.0,
    val maxLoss: Double = 0.0
) {
    val formattedMaxGain: String
        get() = String.format(Locale.US, "+%.1f%%", maxGain)

    val formattedMaxLoss: String
        get() = String.format(Locale.US, "%.1f%%", maxLoss)
}

data class WeekdayStat(
    val day: String = "",
    val dayName: String = "",
    val avgReturn: Double = 0.0,
    val greenProb: Double = 50.0
) {
    val formattedAvgReturn: String
        get() = String.format(Locale.US, "%+.2f%%", avgReturn)

    val formattedGreenProb: String
        get() = String.format(Locale.US, "%.1f%%", greenProb)
}

/**
 * DTO for Firestore serialization matching the single document schema `market_data/latest_forecast`.
 */
data class ForecastDocumentDto(
    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Any? = null,

    @get:PropertyName("symbols")
    @set:PropertyName("symbols")
    var symbols: Map<String, StockItemDto> = emptyMap()
)

data class StockItemDto(
    @get:PropertyName("symbol")
    @set:PropertyName("symbol")
    var symbol: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("currentPrice")
    @set:PropertyName("currentPrice")
    var currentPrice: Double = 0.0,

    @get:PropertyName("currency")
    @set:PropertyName("currency")
    var currency: String = "USD",

    @get:PropertyName("latestImpliedVolatility")
    @set:PropertyName("latestImpliedVolatility")
    var latestImpliedVolatility: Double = 0.0,

    @get:PropertyName("predictedVolatility")
    @set:PropertyName("predictedVolatility")
    var predictedVolatility: Double = 0.0,

    @get:PropertyName("stats")
    @set:PropertyName("stats")
    var stats: Map<String, Any>? = null
)
