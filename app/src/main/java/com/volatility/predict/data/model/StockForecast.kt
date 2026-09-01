package com.volatility.predict.data.model

import com.google.firebase.firestore.PropertyName
import java.text.NumberFormat
import java.util.Locale

/**
 * Domain model representing a stock's latest pricing and volatility forecast.
 */
data class StockItem(
    val symbol: String = "",
    val name: String = "",
    val currentPrice: Double = 0.0,
    val currency: String = "USD",
    val latestImpliedVolatility: Double = 0.0,
    val predictedVolatility: Double = 0.0,
    val updatedAt: String = ""
) {
    /**
     * Requirement: If latest implied volatility is smaller than predicted value,
     * the tile should be yellow(ish), otherwise green.
     */
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
    var predictedVolatility: Double = 0.0
)
