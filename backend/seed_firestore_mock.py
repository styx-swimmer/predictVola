#!/usr/bin/env python3
"""
Seed script for Volatility Forecast & 365-Day Return Statistics.
Populates the single document `market_data/latest_forecast` with predicted 5-day volatility
and 365-day quantitative return statistics for the 10 target stocks.
Now uses the actual forecast engine instead of hardcoded data.
"""

import sys
import json
import argparse
from datetime import datetime, timezone
from forecast_engine import compute_all_symbols

def generate_forecast_payload():
    """Generates the single-document schema with real 365-day statistics for 10 symbols."""
    print("Computing real statistics for 10 symbols (this may take a minute)...")
    symbols_data = compute_all_symbols(horizon_days=5)
    
    return {
        "updatedAt": datetime.now(timezone.utc).isoformat(),
        "symbols": symbols_data
    }

def main():
    parser = argparse.ArgumentParser(description="Seed latest volatility forecast & 365-day statistics to Firestore.")
    parser.add_argument("--dry-run", action="store_true", help="Print JSON payload without uploading.")
    parser.add_argument("--key", type=str, default="serviceAccountKey.json", help="Path to Firebase service account key JSON.")
    args = parser.parse_args()

    payload = generate_forecast_payload()

    if args.dry_run:
        print("=== [DRY RUN] Generated Firestore Document Payload with Real 365-Day Stats ===")
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
        print(f"Successfully written real forecast & stats data for {len(payload['symbols'])} symbols to Firestore (market_data/latest_forecast)!")

    except ImportError:
        print("Error: firebase_admin is not installed. Install it with 'pip install firebase-admin' or use --dry-run")
        sys.exit(1)
    except Exception as e:
        print(f"Error updating Firestore: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

