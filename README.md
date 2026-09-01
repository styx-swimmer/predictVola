# PredictVola - 5-Day Stock Volatility Prediction App

Native Android Application built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Google Cloud Firestore** to track predicted 5-day volatility for 10 major US equities.

---

## 🎯 Tracked Universe (10 Stocks)
* **TSLA** (Tesla)
* **MSFT** (Microsoft)
* **NVDA** (NVIDIA)
* **AAPL** (Apple)
* **AMZN** (Amazon)
* **GOOG** (Alphabet)
* **META** (Meta Platforms)
* **AVGO** (Broadcom)
* **AMD** (Advanced Micro Devices)
* **LLY** (Eli Lilly)

---

## 🎨 Visual Color Rules
* **Yellowish / Amber Tile**: Displayed when **`latestImpliedVolatility < predictedVolatility`** (Market IV is below predicted 5-day volatility).
* **Greenish / Emerald Tile**: Displayed when **`latestImpliedVolatility >= predictedVolatility`** (Market IV meets or exceeds predicted volatility).

---

## 📱 Opening & Running in Android Studio

1. Open **Android Studio**.
2. Click **Open** (or `File > Open...`).
3. Select the folder:
   ```
   /Users/kok/Library/Mobile Documents/com~apple~CloudDocs/MT5/Gemini/cpo/predictVola
   ```
4. Android Studio will automatically sync the Gradle build files.
5. Click the green **Run** button (or press `Shift + F10`) to launch on your connected Android device or emulator.

> 💡 **Instant Testing (Mock Mode):**
> The app comes with an automatic offline fallback dataset containing all 10 stocks. You can run and inspect the app immediately in Android Studio without needing Firebase keys right away.

---

## 🔥 Connecting Live Firebase Firestore

When you are ready to connect to your live Firebase project:
1. In the [Firebase Console](https://console.firebase.google.com/), add an Android app with Package Name: `com.volatility.predict`.
2. Download `google-services.json` and place it in the `predictVola/app/` directory:
   ```
   predictVola/app/google-services.json
   ```
3. Sync Gradle in Android Studio. The repository will automatically switch to reading live data from `market_data/latest_forecast`.

---

## 🐍 Backend Seeding Script

To test uploading the dataset from Python to Firestore:
```bash
cd backend
# Test dry-run:
python3 seed_firestore_mock.py --dry-run

# Upload to Firestore using your service account key:
pip install -r requirements.txt
python3 seed_firestore_mock.py --key path/to/serviceAccountKey.json
```

---

## 🌐 Companion Website & Disclosures
* See [`docs/WEBSITE_CONTENT_RECOMMENDATIONS.md`](docs/WEBSITE_CONTENT_RECOMMENDATIONS.md) for structure and content recommendations.
* See [`docs/PRIVACY_POLICY_TEMPLATE.md`](docs/PRIVACY_POLICY_TEMPLATE.md) for Google Play privacy policy template.
