package com.volatility.predict.data.repository

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.volatility.predict.data.model.HorizonMove
import com.volatility.predict.data.model.StockItem
import com.volatility.predict.data.model.StockStats
import com.volatility.predict.data.model.WeekdayStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository providing volatility forecast and 365-day quantitative return statistics.
 * Reads single document `market_data/latest_forecast` from Firestore.
 */
class VolatilityRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("VolatilityRepo", "Firebase not initialized, using fallback dataset: ${e.message}")
            null
        }
    }
) {
    // Exact list of 10 tracked symbols requested
    val targetSymbols = listOf(
        "TSLA", "MSFT", "NVDA", "AAPL", "AMZN",
        "GOOG", "META", "AVGO", "AMD", "LLY"
    )

    fun getLatestForecast(): Flow<Result<ForecastDataResult>> = flow {
        val firestore = firestoreProvider()
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("market_data")
                    .document("latest_forecast")
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val rawSymbols = snapshot.get("symbols") as? Map<*, *>
                    val parsedStocks = mutableListOf<StockItem>()

                    targetSymbols.forEach { sym ->
                        val data = rawSymbols?.get(sym) as? Map<*, *>
                        if (data != null) {
                            val price = (data["currentPrice"] as? Number)?.toDouble() ?: 0.0
                            val implied = (data["latestImpliedVolatility"] as? Number)?.toDouble() ?: 0.0
                            val predicted = (data["predictedVolatility"] as? Number)?.toDouble() ?: 0.0
                            val name = (data["name"] as? String) ?: getFallbackStockName(sym)
                            val currency = (data["currency"] as? String) ?: "USD"
                            val stats = parseStats(data["stats"] as? Map<*, *>, sym)

                            parsedStocks.add(
                                StockItem(
                                    symbol = sym,
                                    name = name,
                                    currentPrice = price,
                                    currency = currency,
                                    latestImpliedVolatility = implied,
                                    predictedVolatility = predicted,
                                    stats = stats
                                )
                            )
                        } else {
                            parsedStocks.add(getFallbackItemForSymbol(sym))
                        }
                    }

                    val updatedAtStr = formatTimestamp(snapshot.get("updatedAt"))
                    emit(
                        Result.success(
                            ForecastDataResult(
                                stocks = parsedStocks,
                                updatedAt = updatedAtStr,
                                isFromFirestore = true
                            )
                        )
                    )
                    return@flow
                }
            } catch (e: Exception) {
                Log.w("VolatilityRepo", "Firestore fetch failed: ${e.message}, falling back to mock data")
            }
        }

        // Fallback realistic dataset
        emit(
            Result.success(
                ForecastDataResult(
                    stocks = getMockForecastStocks(),
                    updatedAt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date()),
                    isFromFirestore = false
                )
            )
        )
    }.flowOn(Dispatchers.IO)

    private fun parseStats(raw: Map<*, *>?, symbol: String): StockStats {
        if (raw == null) {
            return getFallbackStatsForSymbol(symbol)
        }
        try {
            val daysAnalyzed = (raw["daysAnalyzed"] as? Number)?.toInt() ?: 252
            val dailyMedianReturn = (raw["dailyMedianReturn"] as? Number)?.toDouble() ?: 0.0
            val medianGain = (raw["medianGain"] as? Number)?.toDouble() ?: 0.0
            val medianLoss = (raw["medianLoss"] as? Number)?.toDouble() ?: 0.0
            val greenDayProbability = (raw["greenDayProbability"] as? Number)?.toDouble() ?: 50.0
            val consecGainProb = (raw["consecutive2DayGainProbability"] as? Number)?.toDouble() ?: 25.0
            val consecLossProb = (raw["consecutive2DayLossProbability"] as? Number)?.toDouble() ?: 25.0

            val rawHorizons = raw["horizonMoves"] as? Map<*, *>
            val d5Raw = rawHorizons?.get("d5") as? Map<*, *>
            val d30Raw = rawHorizons?.get("d30") as? Map<*, *>
            val d90Raw = rawHorizons?.get("d90") as? Map<*, *>

            val h5 = HorizonMove(
                horizon = (d5Raw?.get("horizon") as? String) ?: "5 Days",
                maxGain = (d5Raw?.get("maxGain") as? Number)?.toDouble() ?: 0.0,
                maxLoss = (d5Raw?.get("maxLoss") as? Number)?.toDouble() ?: 0.0
            )
            val h30 = HorizonMove(
                horizon = (d30Raw?.get("horizon") as? String) ?: "30 Days",
                maxGain = (d30Raw?.get("maxGain") as? Number)?.toDouble() ?: 0.0,
                maxLoss = (d30Raw?.get("maxLoss") as? Number)?.toDouble() ?: 0.0
            )
            val h90 = HorizonMove(
                horizon = (d90Raw?.get("horizon") as? String) ?: "90 Days",
                maxGain = (d90Raw?.get("maxGain") as? Number)?.toDouble() ?: 0.0,
                maxLoss = (d90Raw?.get("maxLoss") as? Number)?.toDouble() ?: 0.0
            )

            val rawWeekdays = raw["weekdayStats"] as? List<*>
            val weekdays = mutableListOf<WeekdayStat>()
            rawWeekdays?.forEach { item ->
                val wMap = item as? Map<*, *>
                if (wMap != null) {
                    weekdays.add(
                        WeekdayStat(
                            day = (wMap["day"] as? String) ?: "",
                            dayName = (wMap["dayName"] as? String) ?: "",
                            avgReturn = (wMap["avgReturn"] as? Number)?.toDouble() ?: 0.0,
                            greenProb = (wMap["greenProb"] as? Number)?.toDouble() ?: 50.0
                        )
                    )
                }
            }

            return StockStats(
                daysAnalyzed = daysAnalyzed,
                dailyMedianReturn = dailyMedianReturn,
                medianGain = medianGain,
                medianLoss = medianLoss,
                greenDayProbability = greenDayProbability,
                consecutive2DayGainProbability = consecGainProb,
                consecutive2DayLossProbability = consecLossProb,
                horizon5d = h5,
                horizon30d = h30,
                horizon90d = h90,
                weekdayStats = if (weekdays.isNotEmpty()) weekdays else getDefaultWeekdays()
            )
        } catch (e: Exception) {
            Log.w("VolatilityRepo", "Error parsing stats for $symbol: ${e.message}")
            return getFallbackStatsForSymbol(symbol)
        }
    }

    private fun formatTimestamp(value: Any?): String {
        return when (value) {
            is com.google.firebase.Timestamp -> {
                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(value.toDate())
            }
            is String -> value
            else -> SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
        }
    }

    private fun getFallbackStockName(symbol: String): String = when (symbol) {
        "TSLA" -> "Tesla, Inc."
        "MSFT" -> "Microsoft Corporation"
        "NVDA" -> "NVIDIA Corporation"
        "AAPL" -> "Apple Inc."
        "AMZN" -> "Amazon.com, Inc."
        "GOOG" -> "Alphabet Inc."
        "META" -> "Meta Platforms, Inc."
        "AVGO" -> "Broadcom Inc."
        "AMD" -> "Advanced Micro Devices"
        "LLY" -> "Eli Lilly and Company"
        else -> symbol
    }

    private fun getDefaultWeekdays(): List<WeekdayStat> = listOf(
        WeekdayStat("Mon", "Monday", 0.35, 55.0),
        WeekdayStat("Tue", "Tuesday", 0.15, 52.0),
        WeekdayStat("Wed", "Wednesday", 0.45, 58.0),
        WeekdayStat("Thu", "Thursday", -0.10, 48.0),
        WeekdayStat("Fri", "Friday", 0.05, 52.0)
    )

    private fun getFallbackStatsForSymbol(symbol: String): StockStats = when (symbol) {
        "TSLA" -> StockStats(
            daysAnalyzed = 252, dailyMedianReturn = 0.22, medianGain = 2.65, medianLoss = -2.35,
            greenDayProbability = 52.4, consecutive2DayGainProbability = 28.6, consecutive2DayLossProbability = 23.4,
            horizon5d = HorizonMove("5 Days", 19.8, -15.4),
            horizon30d = HorizonMove("30 Days", 42.1, -28.6),
            horizon90d = HorizonMove("90 Days", 76.5, -41.2),
            weekdayStats = getDefaultWeekdays()
        )
        "NVDA" -> StockStats(
            daysAnalyzed = 252, dailyMedianReturn = 0.35, medianGain = 2.85, medianLoss = -2.40,
            greenDayProbability = 55.6, consecutive2DayGainProbability = 32.4, consecutive2DayLossProbability = 20.8,
            horizon5d = HorizonMove("5 Days", 21.4, -16.8),
            horizon30d = HorizonMove("30 Days", 48.6, -24.2),
            horizon90d = HorizonMove("90 Days", 85.0, -32.5),
            weekdayStats = getDefaultWeekdays()
        )
        else -> StockStats(
            daysAnalyzed = 252, dailyMedianReturn = 0.18, medianGain = 1.65, medianLoss = -1.45,
            greenDayProbability = 53.5, consecutive2DayGainProbability = 29.5, consecutive2DayLossProbability = 22.0,
            horizon5d = HorizonMove("5 Days", 12.5, -9.5),
            horizon30d = HorizonMove("30 Days", 26.0, -16.5),
            horizon90d = HorizonMove("90 Days", 45.0, -22.0),
            weekdayStats = getDefaultWeekdays()
        )
    }

    private fun getFallbackItemForSymbol(symbol: String): StockItem {
        return getMockForecastStocks().find { it.symbol == symbol } ?: StockItem(
            symbol = symbol,
            name = getFallbackStockName(symbol),
            currentPrice = 100.0,
            latestImpliedVolatility = 0.25,
            predictedVolatility = 0.28,
            stats = getFallbackStatsForSymbol(symbol)
        )
    }

    fun getMockForecastStocks(): List<StockItem> {
        return listOf(
            StockItem("TSLA", "Tesla, Inc.", 214.20, "USD", 0.521, 0.584, stats = getFallbackStatsForSymbol("TSLA")),
            StockItem("MSFT", "Microsoft Corp.", 418.50, "USD", 0.245, 0.218, stats = getFallbackStatsForSymbol("MSFT")),
            StockItem("NVDA", "NVIDIA Corp.", 126.80, "USD", 0.442, 0.490, stats = getFallbackStatsForSymbol("NVDA")),
            StockItem("AAPL", "Apple Inc.", 227.30, "USD", 0.218, 0.195, stats = getFallbackStatsForSymbol("AAPL")),
            StockItem("AMZN", "Amazon.com, Inc.", 178.60, "USD", 0.312, 0.355, stats = getFallbackStatsForSymbol("AMZN")),
            StockItem("GOOG", "Alphabet Inc.", 164.90, "USD", 0.280, 0.260, stats = getFallbackStatsForSymbol("GOOG")),
            StockItem("META", "Meta Platforms, Inc.", 510.40, "USD", 0.368, 0.412, stats = getFallbackStatsForSymbol("META")),
            StockItem("AVGO", "Broadcom Inc.", 156.70, "USD", 0.395, 0.365, stats = getFallbackStatsForSymbol("AVGO")),
            StockItem("AMD", "Advanced Micro Devices", 148.90, "USD", 0.410, 0.465, stats = getFallbackStatsForSymbol("AMD")),
            StockItem("LLY", "Eli Lilly & Co.", 945.10, "USD", 0.292, 0.270, stats = getFallbackStatsForSymbol("LLY"))
        )
    }
}

data class ForecastDataResult(
    val stocks: List<StockItem>,
    val updatedAt: String,
    val isFromFirestore: Boolean
)
