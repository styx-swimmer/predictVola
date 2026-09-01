package com.volatility.predict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.volatility.predict.ui.screens.DashboardScreen
import com.volatility.predict.ui.screens.DetailScreen
import com.volatility.predict.ui.theme.DarkBackground
import com.volatility.predict.ui.theme.PredictVolaTheme
import com.volatility.predict.ui.viewmodel.VolatilityViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VolatilityViewModel by viewModels { VolatilityViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PredictVolaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    PredictVolaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PredictVolaApp(
    viewModel: VolatilityViewModel
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable(
            route = "dashboard",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            }
        ) {
            DashboardScreen(
                uiState = uiState,
                onStockClick = { stock ->
                    viewModel.selectStock(stock.symbol)
                    navController.navigate("detail/${stock.symbol}")
                },
                onRefresh = { viewModel.loadData() }
            )
        }

        composable(
            route = "detail/{symbol}",
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol")
            val stock = uiState.stocks.find { it.symbol.equals(symbol, ignoreCase = true) }
                ?: uiState.selectedStock

            DetailScreen(
                stock = stock,
                onBack = {
                    viewModel.clearSelectedStock()
                    navController.popBackStack()
                }
            )
        }
    }
}
