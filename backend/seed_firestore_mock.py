#!/usr/bin/env python3
"""
Seed script for Volatility Forecast & 365-Day Return Statistics.
Populates the single document `market_data/latest_forecast` with predicted 5-day volatility
and 365-day quantitative return statistics for the 10 target stocks.
"""

import sys
import json
import argparse
from datetime import datetime, timezone

def generate_forecast_payload():
    """Generates the single-document schema with 365-day statistics for 10 symbols."""
    symbols_data = {
        "TSLA": {
            "symbol": "TSLA",
            "name": "Tesla, Inc.",
            "currentPrice": 214.20,
            "currency": "USD",
            "latestImpliedVolatility": 0.521,
            "predictedVolatility": 0.584,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.22,
                "medianGain": 2.65,
                "medianLoss": -2.35,
                "greenDayProbability": 52.4,
                "consecutive2DayGainProbability": 28.6,
                "consecutive2DayLossProbability": 23.4,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 19.8, "maxLoss": -15.4},
                    "d30": {"horizon": "30 Days", "maxGain": 42.1, "maxLoss": -28.6},
                    "d90": {"horizon": "90 Days", "maxGain": 76.5, "maxLoss": -41.2}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.48, "greenProb": 56.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.12, "greenProb": 52.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.65, "greenProb": 60.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.25, "greenProb": 46.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": -0.10, "greenProb": 48.0}
                ]
            }
        },
        "MSFT": {
            "symbol": "MSFT",
            "name": "Microsoft Corporation",
            "currentPrice": 418.50,
            "currency": "USD",
            "latestImpliedVolatility": 0.245,
            "predictedVolatility": 0.218,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.15,
                "medianGain": 1.25,
                "medianLoss": -1.18,
                "greenDayProbability": 54.2,
                "consecutive2DayGainProbability": 30.1,
                "consecutive2DayLossProbability": 21.0,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 7.4, "maxLoss": -6.2},
                    "d30": {"horizon": "30 Days", "maxGain": 14.8, "maxLoss": -11.5},
                    "d90": {"horizon": "90 Days", "maxGain": 24.6, "maxLoss": -16.2}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.22, "greenProb": 54.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.18, "greenProb": 56.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.31, "greenProb": 58.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": 0.05, "greenProb": 50.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.02, "greenProb": 52.0}
                ]
            }
        },
        "NVDA": {
            "symbol": "NVDA",
            "name": "NVIDIA Corporation",
            "currentPrice": 126.80,
            "currency": "USD",
            "latestImpliedVolatility": 0.442,
            "predictedVolatility": 0.490,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.35,
                "medianGain": 2.85,
                "medianLoss": -2.40,
                "greenDayProbability": 55.6,
                "consecutive2DayGainProbability": 32.4,
                "consecutive2DayLossProbability": 20.8,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 21.4, "maxLoss": -16.8},
                    "d30": {"horizon": "30 Days", "maxGain": 48.6, "maxLoss": -24.2},
                    "d90": {"horizon": "90 Days", "maxGain": 85.0, "maxLoss": -32.5}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.55, "greenProb": 58.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.40, "greenProb": 56.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.60, "greenProb": 60.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": 0.10, "greenProb": 52.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.15, "greenProb": 52.0}
                ]
            }
        },
        "AAPL": {
            "symbol": "AAPL",
            "name": "Apple Inc.",
            "currentPrice": 227.30,
            "currency": "USD",
            "latestImpliedVolatility": 0.218,
            "predictedVolatility": 0.195,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.12,
                "medianGain": 1.15,
                "medianLoss": -1.05,
                "greenDayProbability": 53.5,
                "consecutive2DayGainProbability": 29.2,
                "consecutive2DayLossProbability": 21.8,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 8.2, "maxLoss": -5.9},
                    "d30": {"horizon": "30 Days", "maxGain": 16.5, "maxLoss": -9.8},
                    "d90": {"horizon": "90 Days", "maxGain": 28.0, "maxLoss": -14.5}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.25, "greenProb": 56.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.15, "greenProb": 54.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.28, "greenProb": 56.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.05, "greenProb": 48.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.08, "greenProb": 52.0}
                ]
            }
        },
        "AMZN": {
            "symbol": "AMZN",
            "name": "Amazon.com, Inc.",
            "currentPrice": 178.60,
            "currency": "USD",
            "latestImpliedVolatility": 0.312,
            "predictedVolatility": 0.355,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.18,
                "medianGain": 1.75,
                "medianLoss": -1.60,
                "greenDayProbability": 53.0,
                "consecutive2DayGainProbability": 28.8,
                "consecutive2DayLossProbability": 22.4,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 12.6, "maxLoss": -10.5},
                    "d30": {"horizon": "30 Days", "maxGain": 26.4, "maxLoss": -18.2},
                    "d90": {"horizon": "90 Days", "maxGain": 45.2, "maxLoss": -25.6}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.32, "greenProb": 54.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.20, "greenProb": 52.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.40, "greenProb": 58.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.15, "greenProb": 48.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.05, "greenProb": 52.0}
                ]
            }
        },
        "GOOG": {
            "symbol": "GOOG",
            "name": "Alphabet Inc.",
            "currentPrice": 164.90,
            "currency": "USD",
            "latestImpliedVolatility": 0.280,
            "predictedVolatility": 0.260,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.14,
                "medianGain": 1.55,
                "medianLoss": -1.45,
                "greenDayProbability": 53.2,
                "consecutive2DayGainProbability": 29.0,
                "consecutive2DayLossProbability": 22.0,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 10.8, "maxLoss": -8.9},
                    "d30": {"horizon": "30 Days", "maxGain": 22.0, "maxLoss": -15.4},
                    "d90": {"horizon": "90 Days", "maxGain": 36.8, "maxLoss": -21.0}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.28, "greenProb": 54.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.16, "greenProb": 52.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.35, "greenProb": 56.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.08, "greenProb": 50.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.02, "greenProb": 52.0}
                ]
            }
        },
        "META": {
            "symbol": "META",
            "name": "Meta Platforms, Inc.",
            "currentPrice": 510.40,
            "currency": "USD",
            "latestImpliedVolatility": 0.368,
            "predictedVolatility": 0.412,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.25,
                "medianGain": 2.20,
                "medianLoss": -1.95,
                "greenDayProbability": 54.5,
                "consecutive2DayGainProbability": 31.0,
                "consecutive2DayLossProbability": 21.5,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 24.5, "maxLoss": -14.8},
                    "d30": {"horizon": "30 Days", "maxGain": 45.0, "maxLoss": -22.0},
                    "d90": {"horizon": "90 Days", "maxGain": 78.2, "maxLoss": -29.5}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.45, "greenProb": 56.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.30, "greenProb": 54.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.50, "greenProb": 58.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.10, "greenProb": 50.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.12, "greenProb": 54.0}
                ]
            }
        },
        "AVGO": {
            "symbol": "AVGO",
            "name": "Broadcom Inc.",
            "currentPrice": 156.70,
            "currency": "USD",
            "latestImpliedVolatility": 0.395,
            "predictedVolatility": 0.365,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.22,
                "medianGain": 2.10,
                "medianLoss": -1.85,
                "greenDayProbability": 54.0,
                "consecutive2DayGainProbability": 30.5,
                "consecutive2DayLossProbability": 21.8,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 18.2, "maxLoss": -12.4},
                    "d30": {"horizon": "30 Days", "maxGain": 36.5, "maxLoss": -19.5},
                    "d90": {"horizon": "90 Days", "maxGain": 64.0, "maxLoss": -27.0}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.38, "greenProb": 56.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.25, "greenProb": 54.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.45, "greenProb": 58.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.05, "greenProb": 50.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.08, "greenProb": 52.0}
                ]
            }
        },
        "AMD": {
            "symbol": "AMD",
            "name": "Advanced Micro Devices",
            "currentPrice": 148.90,
            "currency": "USD",
            "latestImpliedVolatility": 0.410,
            "predictedVolatility": 0.465,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.20,
                "medianGain": 2.50,
                "medianLoss": -2.25,
                "greenDayProbability": 52.8,
                "consecutive2DayGainProbability": 29.0,
                "consecutive2DayLossProbability": 23.0,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 19.5, "maxLoss": -15.8},
                    "d30": {"horizon": "30 Days", "maxGain": 40.2, "maxLoss": -26.0},
                    "d90": {"horizon": "90 Days", "maxGain": 72.5, "maxLoss": -36.0}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.42, "greenProb": 54.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.20, "greenProb": 52.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.52, "greenProb": 58.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": -0.18, "greenProb": 48.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.05, "greenProb": 50.0}
                ]
            }
        },
        "LLY": {
            "symbol": "LLY",
            "name": "Eli Lilly and Company",
            "currentPrice": 945.10,
            "currency": "USD",
            "latestImpliedVolatility": 0.292,
            "predictedVolatility": 0.270,
            "stats": {
                "daysAnalyzed": 252,
                "dailyMedianReturn": 0.18,
                "medianGain": 1.65,
                "medianLoss": -1.40,
                "greenDayProbability": 54.8,
                "consecutive2DayGainProbability": 31.2,
                "consecutive2DayLossProbability": 20.5,
                "horizonMoves": {
                    "d5": {"horizon": "5 Days", "maxGain": 14.5, "maxLoss": -9.2},
                    "d30": {"horizon": "30 Days", "maxGain": 28.0, "maxLoss": -15.0},
                    "d90": {"horizon": "90 Days", "maxGain": 52.4, "maxLoss": -20.5}
                },
                "weekdayStats": [
                    {"day": "Mon", "dayName": "Monday", "avgReturn": 0.35, "greenProb": 56.0},
                    {"day": "Tue", "dayName": "Tuesday", "avgReturn": 0.28, "greenProb": 56.0},
                    {"day": "Wed", "dayName": "Wednesday", "avgReturn": 0.42, "greenProb": 58.0},
                    {"day": "Thu", "dayName": "Thursday", "avgReturn": 0.05, "greenProb": 50.0},
                    {"day": "Fri", "dayName": "Friday", "avgReturn": 0.10, "greenProb": 54.0}
                ]
            }
        }
    }

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
        print("=== [DRY RUN] Generated Firestore Document Payload with 365-Day Stats ===")
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
        print(f"Successfully written forecast & stats data for {len(payload['symbols'])} symbols to Firestore (market_data/latest_forecast)!")

    except ImportError:
        print("Error: firebase_admin is not installed. Install it with 'pip install firebase-admin' or use --dry-run")
        sys.exit(1)
    except Exception as e:
        print(f"Error updating Firestore: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
