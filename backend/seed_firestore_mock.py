#!/usr/bin/env python3
"""
Seed script for Volatility Forecast Firestore Database.
Populates the single document `market_data/latest_forecast` with predicted 5-day volatility data
for the 10 target stocks: TSLA, MSFT, NVDA, AAPL, AMZN, GOOG, META, AVGO, AMD, LLY.

Usage:
    # Dry-run (prints JSON payload without uploading):
    python3 seed_firestore_mock.py --dry-run

    # Live upload using serviceAccountKey.json:
    python3 seed_firestore_mock.py --key path/to/serviceAccountKey.json
"""

import sys
import json
import argparse
from datetime import datetime, timezone

def generate_forecast_payload():
    """Generates the single-document schema for the 10 symbols."""
    symbols_data = {
        "TSLA": {
            "symbol": "TSLA",
            "name": "Tesla, Inc.",
            "currentPrice": 214.20,
            "currency": "USD",
            "latestImpliedVolatility": 0.521,  # 52.1%
            "predictedVolatility": 0.584       # 58.4% -> Yellowish (IV < Predicted)
        },
        "MSFT": {
            "symbol": "MSFT",
            "name": "Microsoft Corporation",
            "currentPrice": 418.50,
            "currency": "USD",
            "latestImpliedVolatility": 0.245,  # 24.5%
            "predictedVolatility": 0.218       # 21.8% -> Greenish (IV >= Predicted)
        },
        "NVDA": {
            "symbol": "NVDA",
            "name": "NVIDIA Corporation",
            "currentPrice": 126.80,
            "currency": "USD",
            "latestImpliedVolatility": 0.442,  # 44.2%
            "predictedVolatility": 0.490       # 49.0% -> Yellowish
        },
        "AAPL": {
            "symbol": "AAPL",
            "name": "Apple Inc.",
            "currentPrice": 227.30,
            "currency": "USD",
            "latestImpliedVolatility": 0.218,  # 21.8%
            "predictedVolatility": 0.195       # 19.5% -> Greenish
        },
        "AMZN": {
            "symbol": "AMZN",
            "name": "Amazon.com, Inc.",
            "currentPrice": 178.60,
            "currency": "USD",
            "latestImpliedVolatility": 0.312,  # 31.2%
            "predictedVolatility": 0.355       # 35.5% -> Yellowish
        },
        "GOOG": {
            "symbol": "GOOG",
            "name": "Alphabet Inc.",
            "currentPrice": 164.90,
            "currency": "USD",
            "latestImpliedVolatility": 0.280,  # 28.0%
            "predictedVolatility": 0.260       # 26.0% -> Greenish
        },
        "META": {
            "symbol": "META",
            "name": "Meta Platforms, Inc.",
            "currentPrice": 510.40,
            "currency": "USD",
            "latestImpliedVolatility": 0.368,  # 36.8%
            "predictedVolatility": 0.412       # 41.2% -> Yellowish
        },
        "AVGO": {
            "symbol": "AVGO",
            "name": "Broadcom Inc.",
            "currentPrice": 156.70,
            "currency": "USD",
            "latestImpliedVolatility": 0.395,  # 39.5%
            "predictedVolatility": 0.365       # 36.5% -> Greenish
        },
        "AMD": {
            "symbol": "AMD",
            "name": "Advanced Micro Devices",
            "currentPrice": 148.90,
            "currency": "USD",
            "latestImpliedVolatility": 0.410,  # 41.0%
            "predictedVolatility": 0.465       # 46.5% -> Yellowish
        },
        "LLY": {
            "symbol": "LLY",
            "name": "Eli Lilly and Company",
            "currentPrice": 945.10,
            "currency": "USD",
            "latestImpliedVolatility": 0.292,  # 29.2%
            "predictedVolatility": 0.270       # 27.0% -> Greenish
        }
    }

    return {
        "updatedAt": datetime.now(timezone.utc).isoformat(),
        "symbols": symbols_data
    }

def main():
    parser = argparse.ArgumentParser(description="Seed latest volatility forecast to Firestore.")
    parser.add_argument("--dry-run", action="store_true", help="Print JSON payload without uploading.")
    parser.add_argument("--key", type=str, default="serviceAccountKey.json", help="Path to Firebase service account key JSON.")
    args = parser.parse_args()

    payload = generate_forecast_payload()

    if args.dry_run:
        print("=== [DRY RUN] Generated Firestore Document Payload ===")
        print(json.dumps(payload, indent=2))
        print("Target Document: market_data/latest_forecast")
        return

    try:
        import firebase_admin
        from firebase_admin import credentials, firestore

        cred = credentials.Certificate(args.key)
        firebase_admin.initialize_app(cred)
        db = firestore.client()

        doc_ref = db.collection("market_data").document("latest_forecast")
        doc_ref.set(payload)
        print(f"Successfully written forecast data for {len(payload['symbols'])} symbols to Firestore (market_data/latest_forecast)!")

    except ImportError:
        print("Error: firebase_admin is not installed. Install it with 'pip install firebase-admin' or use --dry-run")
        sys.exit(1)
    except Exception as e:
        print(f"Error updating Firestore: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
