package com.volatility.predict.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.volatility.predict.data.model.StockItem
import com.volatility.predict.data.repository.VolatilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VolatilityUiState(
    val isLoading: Boolean = true,
    val stocks: List<StockItem> = emptyList(),
    val selectedStock: StockItem? = null,
    val lastUpdated: String = "",
    val isFromFirestore: Boolean = false,
    val errorMessage: String? = null
)

class VolatilityViewModel(
    private val repository: VolatilityRepository = VolatilityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VolatilityUiState())
    val uiState: StateFlow<VolatilityUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getLatestForecast().collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                stocks = data.stocks,
                                lastUpdated = data.updatedAt,
                                isFromFirestore = data.isFromFirestore,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.localizedMessage ?: "Failed to fetch volatility data"
                            )
                        }
                    }
                )
            }
        }
    }

    fun selectStock(symbol: String) {
        val found = _uiState.value.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
        _uiState.update { it.copy(selectedStock = found) }
    }

    fun clearSelectedStock() {
        _uiState.update { it.copy(selectedStock = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VolatilityViewModel() as T
            }
        }
    }
}
