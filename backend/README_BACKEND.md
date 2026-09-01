# PredictVola - Quantitative Volatility Engine & Cloud Pipeline

Automated background pipeline that fits **GJR-GARCH(1,1)** models with Student's t-distribution on 2-year daily returns, calculates predicted 5-day annualized volatility, extracts options market Implied Volatility (IV), and publishes single-document updates to **Firebase Cloud Firestore** (`market_data/latest_forecast`).

---

## 🧮 Quantitative Architecture

For each of the 10 target symbols (**TSLA, MSFT, NVDA, AAPL, AMZN, GOOG, META, AVGO, AMD, LLY**):
1. **Price Data:** Fetches 2 years of daily historical closing prices.
2. **Returns Calculation:** Daily percentage returns ($r_t = 100 \times \frac{P_t - P_{t-1}}{P_{t-1}}$).
3. **Model Specification:** `GJR-GARCH(1,1)` with **Student's t-distribution** (`arch_model(returns, vol='GARCH', p=1, o=1, q=1, dist='t', mean='Constant')`):
   * $\alpha$: Shock magnitude.
   * $\gamma$: Asymmetric leverage effect (negative shocks increase vol).
   * $\beta$: Volatility persistence.
   * $\nu$: Degrees of freedom (capturing fat tails).
4. **5-Day Volatility Forecast:**
   $$\sigma_{\text{pred, 5D}} = \left(\frac{\sqrt{\frac{1}{5} \sum_{i=1}^5 \hat{\sigma}_{t+i}^2}}{100}\right) \times \sqrt{252}$$
5. **Implied Volatility (IV):** Fetches the active options chain and isolates the nearest At-The-Money (ATM) call contract to obtain market IV.
6. **Firestore Upload:** Stores all 10 symbols in a single document: `market_data/latest_forecast`.

---

## 💻 Local Testing & Execution

### 1. Install Dependencies
```bash
cd backend
pip install -r requirements.txt
```

### 2. Dry Run (No Firebase credentials needed)
Simulates data retrieval, model fitting, and prints the exact JSON structure:
```bash
python3 main.py --dry-run
```

### 3. Local Upload with Service Account Key
To run locally and upload to your live Firestore database:
```bash
python3 main.py --key path/to/serviceAccountKey.json
```

---

## ☁️ Google Cloud Deployment (Cloud Functions + Cloud Scheduler)

### Automated 1-Click Deployment
Make sure you are logged into the Google Cloud CLI (`gcloud auth login` and `gcloud config set project YOUR_PROJECT_ID`), then run:
```bash
./deploy_gcp.sh
```

### Manual Setup via Google Cloud Console

#### Step 1: Deploy Cloud Function
1. In the **Google Cloud Console**, go to **Cloud Functions** $\rightarrow$ **Create Function**.
2. **Environment:** 2nd gen.
3. **Function Name:** `predict-volatility-engine`.
4. **Trigger:** HTTPS (Require authentication).
5. **Memory:** `1 GiB` (sufficient for numpy/scipy/arch optimization).
6. **Timeout:** `540 seconds` (gives ample time to process all 10 stocks sequentially).
7. **Runtime:** Python 3.11.
8. **Entry Point:** `calculate_and_publish_volatility`.
9. Upload source files (`main.py`, `forecast_engine.py`, `requirements.txt`).

#### Step 2: Set Up Cloud Scheduler
1. In Google Cloud Console, go to **Cloud Scheduler** $\rightarrow$ **Create Job**.
2. **Name:** `daily-volatility-trigger`.
3. **Region:** Same as function (e.g. `us-central1`).
4. **Frequency:** `30 16 * * 1-5` (Mon–Fri at 16:30 / 4:30 PM EST, right after US equity markets close).
5. **Timezone:** `America/New_York` (or `America/Toronto`).
6. **Target Type:** `HTTP`.
7. **URL:** The trigger URL of your deployed Cloud Function.
8. **HTTP Method:** `POST`.
9. **Auth Header:** `Add OIDC token` (Select a service account with `Cloud Run Invoker` permissions).
10. Click **Create** and use **Force Run** to test immediately!
