package com.volatility.predict.data.repository

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.volatility.predict.data.model.StockItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository providing volatility forecast data.
 * Reads single document `market_data/latest_forecast` from Firestore.
 * Automatically falls back to offline mock dataset if Firebase credentials are not yet configured.
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

                            parsedStocks.add(
                                StockItem(
                                    symbol = sym,
                                    name = name,
                                    currentPrice = price,
                                    currency = currency,
                                    latestImpliedVolatility = implied,
                                    predictedVolatility = predicted
                                )
                            )
                        } else {
                            // If symbol wasn't in backend payload, provide realistic fallback item
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

    private fun getFallbackItemForSymbol(symbol: String): StockItem {
        return getMockForecastStocks().find { it.symbol == symbol } ?: StockItem(
            symbol = symbol,
            name = getFallbackStockName(symbol),
            currentPrice = 100.0,
            latestImpliedVolatility = 0.25,
            predictedVolatility = 0.28
        )
    }

    fun getMockForecastStocks(): List<StockItem> {
        return listOf(
            StockItem(
                symbol = "TSLA",
                name = "Tesla, Inc.",
                currentPrice = 214.20,
                latestImpliedVolatility = 0.521, // 52.1%
                predictedVolatility = 0.584      // 58.4% (Implied < Predicted -> Yellowish)
            ),
            StockItem(
                symbol = "MSFT",
                name = "Microsoft Corp.",
                currentPrice = 418.50,
                latestImpliedVolatility = 0.245, // 24.5%
                predictedVolatility = 0.218      // 21.8% (Implied >= Predicted -> Greenish)
            ),
            StockItem(
                symbol = "NVDA",
                name = "NVIDIA Corp.",
                currentPrice = 126.80,
                latestImpliedVolatility = 0.442, // 44.2%
                predictedVolatility = 0.490      // 49.0% (Implied < Predicted -> Yellowish)
            ),
            StockItem(
                symbol = "AAPL",
                name = "Apple Inc.",
                currentPrice = 227.30,
                latestImpliedVolatility = 0.218, // 21.8%
                predictedVolatility = 0.195      // 19.5% (Implied >= Predicted -> Greenish)
            ),
            StockItem(
                symbol = "AMZN",
                name = "Amazon.com, Inc.",
                currentPrice = 178.60,
                latestImpliedVolatility = 0.312, // 31.2%
                predictedVolatility = 0.355      // 35.5% (Implied < Predicted -> Yellowish)
            ),
            StockItem(
                symbol = "GOOG",
                name = "Alphabet Inc.",
                currentPrice = 164.90,
                latestImpliedVolatility = 0.280, // 28.0%
                predictedVolatility = 0.260      // 26.0% (Implied >= Predicted -> Greenish)
            ),
            StockItem(
                symbol = "META",
                name = "Meta Platforms, Inc.",
                currentPrice = 510.40,
                latestImpliedVolatility = 0.368, // 36.8%
                predictedVolatility = 0.412      // 41.2% (Implied < Predicted -> Yellowish)
            ),
            StockItem(
                symbol = "AVGO",
                name = "Broadcom Inc.",
                currentPrice = 156.70,
                latestImpliedVolatility = 0.395, // 39.5%
                predictedVolatility = 0.365      // 36.5% (Implied >= Predicted -> Greenish)
            ),
            StockItem(
                symbol = "AMD",
                name = "Advanced Micro Devices",
                currentPrice = 148.90,
                latestImpliedVolatility = 0.410, // 41.0%
                predictedVolatility = 0.465      // 46.5% (Implied < Predicted -> Yellowish)
            ),
            StockItem(
                symbol = "LLY",
                name = "Eli Lilly & Co.",
                currentPrice = 945.10,
                latestImpliedVolatility = 0.292, // 29.2%
                predictedVolatility = 0.270      // 27.0% (Implied >= Predicted -> Greenish)
            )
        )
    }
}

data class ForecastDataResult(
    val stocks: List<StockItem>,
    val updatedAt: String,
    val isFromFirestore: Boolean
)
