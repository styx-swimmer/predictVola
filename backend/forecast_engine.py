#!/usr/bin/env python3
"""
Volatility Forecast Engine: GJR-GARCH(1,1) + ATM Implied Volatility & Options Analytics
Calculates predicted 5-day volatility and ATM implied volatility for the 10 target symbols:
TSLA, MSFT, NVDA, AAPL, AMZN, GOOG, META, AVGO, AMD, LLY.
"""

import logging
import warnings
import numpy as np
import pandas as pd
import yfinance as yf
from arch import arch_model
from scipy.stats import norm
from typing import Dict, Any, Optional

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
    """
    Calculates the Black-Scholes price for European options.
    S: Current stock price
    K: Strike price
    T: Time to expiration in years (trading days / 252)
    r: Risk-free rate (decimal, e.g. 0.05)
    sigma: Annualized volatility (decimal, e.g. 0.25)
    """
    if T <= 0 or sigma <= 0 or S <= 0 or K <= 0:
        return max(0.0, S - K) if option_type == 'call' else max(0.0, K - S)
    
    d1 = (np.log(S / K) + (r + 0.5 * sigma**2) * T) / (sigma * np.sqrt(T))
    d2 = d1 - sigma * np.sqrt(T)
    
    if option_type == 'call':
        return float(S * norm.cdf(d1) - K * np.exp(-r * T) * norm.cdf(d2))
    else:
        return float(K * np.exp(-r * T) * norm.cdf(-d2) - S * norm.cdf(-d1))

def process_single_stock(symbol: str, company_name: str, horizon_days: int = 5) -> Optional[Dict[str, Any]]:
    """
    Fetches historical data, fits GJR-GARCH(1,1), calculates 5-day predicted volatility,
    and extracts current market price and nearest ATM options implied volatility.
    """
    try:
        logging.info(f"[{symbol}] Fetching historical data and options chain...")
        ticker = yf.Ticker(symbol)
        
        # 1. Fetch 2 years of daily historical data for robust GARCH parameter estimation
        hist = ticker.history(period="2y")
        if hist.empty or len(hist) < 100:
            logging.error(f"[{symbol}] Insufficient historical price data.")
            return None
        
        current_price = float(hist['Close'].iloc[-1])
        
        # Daily percentage returns for numerical stability in optimizer
        returns = 100 * hist['Close'].pct_change().dropna()
        
        # 2. Fit GJR-GARCH(1,1) with Student's t-distribution (handles fat tails & leverage effect)
        # p=1 (ARCH), o=1 (Asymmetric/GJR leverage), q=1 (GARCH), dist='t'
        model = arch_model(returns, vol='GARCH', p=1, o=1, q=1, dist='t', mean='Constant')
        results = model.fit(disp='off')
        
        # 3. Forecast 5-day variance horizon
        forecast = results.forecast(horizon=horizon_days)
        # Average the daily forecasted variance across the 5-day window
        avg_daily_var = float(forecast.variance.values[-1, :].mean())
        # Convert variance (%^2) to annualized decimal standard deviation
        predicted_vol = float((np.sqrt(avg_daily_var) / 100.0) * np.sqrt(252))
        
        # 4. Fetch Implied Volatility from Options Chain (ATM Call)
        implied_vol = None
        market_option_price = None
        strike_price = None
        selected_dte = horizon_days
        
        try:
            expirations = ticker.options
            if expirations and len(expirations) > 0:
                # Find nearest expiration that has >= 5 DTE
                today = pd.Timestamp.now().tz_localize(None)
                target_exp = None
                
                for exp in expirations:
                    dte = (pd.Timestamp(exp) - today).days
                    if dte >= 5:
                        target_exp = exp
                        selected_dte = dte
                        break
                
                if not target_exp:
                    target_exp = expirations[0]
                    selected_dte = max(1, (pd.Timestamp(target_exp) - today).days)
                
                opt_chain = ticker.option_chain(target_exp)
                calls = opt_chain.calls
                
                if not calls.empty:
                    # Select strike closest to current spot price
                    calls['abs_diff'] = (calls['strike'] - current_price).abs()
                    atm_call = calls.loc[calls['abs_diff'].idxmin()]
                    
                    strike_price = float(atm_call['strike'])
                    market_option_price = float(atm_call.get('lastPrice', 0.0))
                    raw_iv = atm_call.get('impliedVolatility', None)
                    if raw_iv is not None and not np.isnan(raw_iv) and raw_iv > 0:
                        implied_vol = float(raw_iv)
        except Exception as opt_err:
            logging.warning(f"[{symbol}] Could not extract option IV: {opt_err}. Using 30-day realized IV proxy.")

        # Fallback for IV if options chain unavailable (e.g. holiday or Yahoo rate limit)
        if implied_vol is None or np.isnan(implied_vol) or implied_vol <= 0:
            # 30-day historical realized volatility proxy
            recent_returns = returns.tail(30) / 100.0
            implied_vol = float(recent_returns.std() * np.sqrt(252))

        logging.info(
            f"[{symbol}] Price: ${current_price:.2f} | Implied IV: {implied_vol*100:.1f}% | "
            f"Predicted 5D Vol: {predicted_vol*100:.1f}%"
        )

        return {
            "symbol": symbol,
            "name": company_name,
            "currentPrice": round(current_price, 2),
            "currency": "USD",
            "latestImpliedVolatility": round(implied_vol, 4),
            "predictedVolatility": round(predicted_vol, 4)
        }

    except Exception as e:
        logging.error(f"[{symbol}] Error processing stock: {e}", exc_info=True)
        return None

def compute_all_symbols(horizon_days: int = 5) -> Dict[str, Any]:
    """
    Computes volatility forecast across all 10 tracked symbols.
    Returns the ready-to-write Firestore document structure.
    """
    results = {}
    for symbol, name in TARGET_SYMBOLS.items():
        data = process_single_stock(symbol, name, horizon_days=horizon_days)
        if data:
            results[symbol] = data
        else:
            logging.warning(f"[{symbol}] Skipping due to processing failure.")
            
    return results
