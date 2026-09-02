#!/usr/bin/env python3
"""
Cloud Function Entrypoint & Local Execution Runner for PredictVola.
Calculates 5-day predicted volatility for 10 stocks and updates Firestore:
Collection: 'market_data'
Document:   'latest_forecast'
"""

import os
import sys
import json
import logging
import argparse
from datetime import datetime, timezone
from typing import Optional, Dict, Any

try:
    import functions_framework
    _HAS_FF = True
except ImportError:
    _HAS_FF = False

    def http_dummy_decorator(func):
        return func
    functions_framework = type('obj', (object,), {'http': http_dummy_decorator})

from forecast_engine import compute_all_symbols

# Google Cloud & Firebase Admin
import firebase_admin
from firebase_admin import credentials, firestore

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

_db: Optional[firestore.firestore.Client] = None

def get_firestore_client(key_path: Optional[str] = None) -> firestore.firestore.Client:
    """Initializes or retrieves Firestore client instance."""
    global _db
    if _db is not None:
        return _db
    
    if not firebase_admin._apps:
        if key_path and os.path.exists(key_path):
            cred = credentials.Certificate(key_path)
            firebase_admin.initialize_app(cred)
            logging.info(f"Initialized Firebase with service key: {key_path}")
        else:
            # Running inside Google Cloud environment (Functions/Run) with Default Credentials
            firebase_admin.initialize_app()
            logging.info("Initialized Firebase with Application Default Credentials (ADC)")
            
    _db = firestore.client()
    return _db

def execute_pipeline(key_path: Optional[str] = None, dry_run: bool = False) -> Dict[str, Any]:
    """Executes the calculation and writes to Firestore."""
    logging.info("Starting Volatility Forecast calculation pipeline for 10 stocks...")
    
    symbols_data = compute_all_symbols(horizon_days=5)
    
    if not symbols_data:
        raise RuntimeError("No symbol data was successfully calculated.")
        
    payload = {
        "updatedAt": datetime.now(timezone.utc).isoformat(),
        "symbols": symbols_data
    }
    
    if dry_run:
        logging.info("=== [DRY RUN] Generated Payload (Skipping Firestore Write) ===")
        print(json.dumps(payload, indent=2))
        return payload

    # Write to Firestore single-document: market_data/latest_forecast
    logging.info("Connecting to Firestore to write market_data/latest_forecast...")
    db = get_firestore_client(key_path)
    doc_ref = db.collection("market_data").document("latest_forecast")
    doc_ref.set(payload)
    logging.info(f"Successfully published {len(symbols_data)} stocks to Firestore (market_data/latest_forecast)!")
    return payload

@functions_framework.http
def calculate_and_publish_volatility(request):
    """
    Google Cloud Function HTTP Entrypoint.
    Triggered by Google Cloud Scheduler daily after market close.
    """
    logging.info("Received HTTP request to calculate_and_publish_volatility.")
    try:
        payload = execute_pipeline(dry_run=False)
        return (
            json.dumps({
                "status": "success",
                "message": f"Successfully updated {len(payload['symbols'])} symbols",
                "timestamp": payload["updatedAt"]
            }),
            200,
            {"Content-Type": "application/json"}
        )
    except Exception as e:
        logging.error(f"Cloud Function execution failed: {e}", exc_info=True)
        return (
            json.dumps({"status": "error", "message": str(e)}),
            500,
            {"Content-Type": "application/json"}
        )

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Run Volatility Forecast Pipeline.")
    parser.add_argument("--dry-run", action="store_true", help="Calculate and print output without writing to Firestore.")
    parser.add_argument("--key", type=str, default="serviceAccountKey.json", help="Path to service account key JSON.")
    args = parser.parse_args()

    key_file = args.key if os.path.exists(args.key) else None
    execute_pipeline(key_path=key_file, dry_run=args.dry_run)
