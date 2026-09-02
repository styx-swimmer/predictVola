#!/usr/bin/env python3
"""
Volatility Forecast & 365-Day Statistics Engine:
1. Fits GJR-GARCH(1,1) model for 5-day volatility forecast & ATM Implied Volatility
2. Computes deep 365-day quantitative return statistics:
   - Daily median gain/loss, median positive day gain, median negative day loss
   - Multi-horizon max movement (5-day, 30-day, 90-day max gain & max loss)
   - Probability of green day vs previous day (%)
   - Probability of 2 consecutive green days (%) and 2 consecutive red days (%)
   - Weekday-by-weekday performance (Monday-Friday average return & win rate %)
"""

import logging
import warnings
import numpy as np
import pandas as pd
import yfinance as yf
from arch import arch_model
from scipy.stats import norm
from typing import Dict, Any, Optional, List

warnings.filterwarnings("ignore")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

# 10 Tracked Equities
TARGET_SYMBOLS = {
    "TSLA": "Tesla, Inc.",
    "MSFT": "Microsoft Corporation",
    "NVDA": "NVIDIA Corporation",
    "AAPL": "Apple Inc.",
    "AMZN": "Amazon.com, Inc.",
    "GOOG": "Alphabet Inc.",
    "META": "Meta Platforms, Inc.",
    "AVGO": "Broadcom Inc.",
    "AMD": "Advanced Micro Devices",
    "LLY": "Eli Lilly and Company"
}

def black_scholes_price(S: float, K: float, T: float, r: float, sigma: float, option_type: str = 'call') -> float:
    """Calculates the Black-Scholes price for European options."""
    if T <= 0 or sigma <= 0 or S <= 0 or K <= 0:
        return max(0.0, S - K) if option_type == 'call' else max(0.0, K - S)
    
    d1 = (np.log(S / K) + (r + 0.5 * sigma**2) * T) / (sigma * np.sqrt(T))
    d2 = d1 - sigma * np.sqrt(T)
    
    if option_type == 'call':
        return float(S * norm.cdf(d1) - K * np.exp(-r * T) * norm.cdf(d2))
    else:
        return float(K * np.exp(-r * T) * norm.cdf(-d2) - S * norm.cdf(-d1))

def calculate_365d_stats(hist: pd.DataFrame) -> Dict[str, Any]:
    """
    Computes statistical return metrics and multi-horizon movements over the last 365 days.
    """
    # Filter for the last 365 days (~252 trading sessions)
    cutoff = pd.Timestamp.now(tz=hist.index.tz) - pd.Timedelta(days=365) if hist.index.tz else pd.Timestamp.now() - pd.Timedelta(days=365)
    hist_365 = hist[hist.index >= cutoff]
    if len(hist_365) < 30:
        hist_365 = hist.tail(252) # Fallback to last 252 bars
        
    closes = hist_365['Close']
    pct_returns = (closes.pct_change().dropna()) * 100.0
    
    # 1. Daily Median Gain / Loss
    daily_median_return = float(pct_returns.median()) if not pct_returns.empty else 0.0
    pos_returns = pct_returns[pct_returns > 0]
    neg_returns = pct_returns[pct_returns < 0]
    median_gain = float(pos_returns.median()) if len(pos_returns) > 0 else 0.0
    median_loss = float(neg_returns.median()) if len(neg_returns) > 0 else 0.0
    
    # 2. Probability of Green Day
    total_days = len(pct_returns)
    green_day_prob = float((len(pos_returns) / total_days * 100.0)) if total_days > 0 else 50.0
    
    # 3. 2 Consecutive Days Probabilities
    if total_days > 1:
        is_green = pct_returns > 0
        is_red = pct_returns < 0
        consec_green_count = int(((is_green) & (is_green.shift(1))).sum())
        consec_red_count = int(((is_red) & (is_red.shift(1))).sum())
        total_pairs = total_days - 1
        consec_gain_prob = float((consec_green_count / total_pairs) * 100.0)
        consec_loss_prob = float((consec_red_count / total_pairs) * 100.0)
    else:
        consec_gain_prob = 25.0
        consec_loss_prob = 25.0
        
    # 4. Multi-Horizon Max Movements (5-Day, 30-Day, 90-Day in both directions)
    # 5 trading days = 1 week
    ret_5d = (closes.pct_change(5).dropna()) * 100.0
    max_gain_5d = float(ret_5d.max()) if not ret_5d.empty else 0.0
    max_loss_5d = float(ret_5d.min()) if not ret_5d.empty else 0.0
    
    # 30 calendar days ~ 21 trading days
    ret_30d = (closes.pct_change(21).dropna()) * 100.0
    max_gain_30d = float(ret_30d.max()) if not ret_30d.empty else 0.0
    max_loss_30d = float(ret_30d.min()) if not ret_30d.empty else 0.0
    
    # 90 calendar days ~ 63 trading days
    ret_90d = (closes.pct_change(63).dropna()) * 100.0
    max_gain_90d = float(ret_90d.max()) if not ret_90d.empty else 0.0
    max_loss_90d = float(ret_90d.min()) if not ret_90d.empty else 0.0
    
    # 5. Weekday Performance Breakdown (Mon=0, Tue=1, Wed=2, Thu=3, Fri=4)
    weekdays = [
        ("Mon", "Monday", 0),
        ("Tue", "Tuesday", 1),
        ("Wed", "Wednesday", 2),
        ("Thu", "Thursday", 3),
        ("Fri", "Friday", 4)
    ]
    weekday_stats: List[Dict[str, Any]] = []
    
    for short_day, full_day, day_idx in weekdays:
        day_rets = pct_returns[pct_returns.index.dayofweek == day_idx]
        if len(day_rets) > 0:
            avg_ret = float(day_rets.mean())
            green_prob = float((day_rets > 0).mean() * 100.0)
        else:
            avg_ret = 0.0
            green_prob = 50.0
            
        weekday_stats.append({
            "day": short_day,
            "dayName": full_day,
            "avgReturn": round(avg_ret, 2),
            "greenProb": round(green_prob, 1)
        })

    return {
        "daysAnalyzed": total_days,
        "dailyMedianReturn": round(daily_median_return, 2),
        "medianGain": round(median_gain, 2),
        "medianLoss": round(median_loss, 2),
        "greenDayProbability": round(green_day_prob, 1),
        "consecutive2DayGainProbability": round(consec_gain_prob, 1),
        "consecutive2DayLossProbability": round(consec_loss_prob, 1),
        "horizonMoves": {
            "d5": {
                "horizon": "5 Days",
                "maxGain": round(max_gain_5d, 2),
                "maxLoss": round(max_loss_5d, 2)
            },
            "d30": {
                "horizon": "30 Days",
                "maxGain": round(max_gain_30d, 2),
                "maxLoss": round(max_loss_30d, 2)
            },
            "d90": {
                "horizon": "90 Days",
                "maxGain": round(max_gain_90d, 2),
                "maxLoss": round(max_loss_90d, 2)
            }
        },
        "weekdayStats": weekday_stats
    }

def process_single_stock(symbol: str, company_name: str, horizon_days: int = 5) -> Optional[Dict[str, Any]]:
    """
    Fetches historical data, fits GJR-GARCH(1,1), calculates 5-day predicted volatility,
    extracts nearest ATM options implied volatility, and computes 365-day return stats.
    """
    try:
        logging.info(f"[{symbol}] Fetching historical data and options chain...")
        ticker = yf.Ticker(symbol)
        
        # 1. Fetch 2 years of daily historical data for robust GARCH fitting + 365d stats
        hist = ticker.history(period="2y")
        if hist.empty or len(hist) < 100:
            logging.error(f"[{symbol}] Insufficient historical price data.")
            return None
        
        current_price = float(hist['Close'].iloc[-1])
        
        # Daily percentage returns for numerical stability in optimizer
        returns = 100 * hist['Close'].pct_change().dropna()
        
        # 2. Fit GJR-GARCH(1,1) with Student's t-distribution
        model = arch_model(returns, vol='GARCH', p=1, o=1, q=1, dist='t', mean='Constant')
        results = model.fit(disp='off')
        
        # 3. Forecast 5-day variance horizon
        forecast = results.forecast(horizon=horizon_days)
        avg_daily_var = float(forecast.variance.values[-1, :].mean())
        predicted_vol = float((np.sqrt(avg_daily_var) / 100.0) * np.sqrt(252))
        
        # 4. Fetch Implied Volatility from Options Chain (ATM Call)
        implied_vol = None
        try:
            expirations = ticker.options
            if expirations and len(expirations) > 0:
                today = pd.Timestamp.now().tz_localize(None)
                target_exp = None
                
                for exp in expirations:
                    dte = (pd.Timestamp(exp) - today).days
                    if dte >= 5:
                        target_exp = exp
                        break
                
                if not target_exp:
                    target_exp = expirations[0]
                
                opt_chain = ticker.option_chain(target_exp)
                calls = opt_chain.calls
                
                if not calls.empty:
                    calls['abs_diff'] = (calls['strike'] - current_price).abs()
                    atm_call = calls.loc[calls['abs_diff'].idxmin()]
                    raw_iv = atm_call.get('impliedVolatility', None)
                    if raw_iv is not None and not np.isnan(raw_iv) and raw_iv > 0:
                        implied_vol = float(raw_iv)
        except Exception as opt_err:
            logging.warning(f"[{symbol}] Could not extract option IV: {opt_err}. Using 30-day realized IV proxy.")

        if implied_vol is None or np.isnan(implied_vol) or implied_vol <= 0:
            recent_returns = returns.tail(30) / 100.0
            implied_vol = float(recent_returns.std() * np.sqrt(252))

        # 5. Compute 365-Day Return & Movement Statistics
        stats_365 = calculate_365d_stats(hist)

        logging.info(
            f"[{symbol}] Price: ${current_price:.2f} | IV: {implied_vol*100:.1f}% | "
            f"Pred 5D Vol: {predicted_vol*100:.1f}% | 5D Max: +{stats_365['horizonMoves']['d5']['maxGain']}% / {stats_365['horizonMoves']['d5']['maxLoss']}%"
        )

        return {
            "symbol": symbol,
            "name": company_name,
            "currentPrice": round(current_price, 2),
            "currency": "USD",
            "latestImpliedVolatility": round(implied_vol, 4),
            "predictedVolatility": round(predicted_vol, 4),
            "stats": stats_365
        }

    except Exception as e:
        logging.error(f"[{symbol}] Error processing stock: {e}", exc_info=True)
        return None

def compute_all_symbols(horizon_days: int = 5) -> Dict[str, Any]:
    """Computes volatility forecast and 365-day statistics across all 10 symbols."""
    results = {}
    for symbol, name in TARGET_SYMBOLS.items():
        data = process_single_stock(symbol, name, horizon_days=horizon_days)
        if data:
            results[symbol] = data
        else:
            logging.warning(f"[{symbol}] Skipping due to processing failure.")
            
    return results
