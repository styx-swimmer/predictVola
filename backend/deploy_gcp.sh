#!/bin/bash
# ==============================================================================
# Google Cloud Deployment Script for PredictVola Backend Pipeline
# Deploys:
#   1. Cloud Function (Gen 2): Runs GJR-GARCH 5-Day Volatility Engine
#   2. Cloud Scheduler Job: Triggers function Mon-Fri at 16:30 EST (Post Market Close)
# ==============================================================================

set -e

# Configuration Variables - adjust if necessary
PROJECT_ID=$(gcloud config get-value project)
REGION="us-central1"
FUNCTION_NAME="predict-volatility-engine"
SCHEDULER_JOB_NAME="daily-volatility-trigger"
SCHEDULE="30 16 * * 1-5" # Mon-Fri at 16:30 (4:30 PM) New York Time
TIME_ZONE="America/New_York"

echo "=== Deploying PredictVola Backend to Google Cloud ==="
echo "Project ID:    $PROJECT_ID"
echo "Region:        $REGION"
echo "Function Name: $FUNCTION_NAME"
echo "Schedule:      $SCHEDULE ($TIME_ZONE)"
echo "======================================================"

# 1. Enable required GCP APIs
echo "[1/4] Enabling required GCP APIs..."
gcloud services enable \
    cloudfunctions.googleapis.com \
    cloudbuild.googleapis.com \
    cloudscheduler.googleapis.com \
    firestore.googleapis.com \
    run.googleapis.com

# 2. Deploy Cloud Function (Gen 2, Python 3.11/3.12, 1GB RAM, 540s timeout for model fitting)
echo "[2/4] Deploying Cloud Function Gen 2..."
gcloud functions deploy "$FUNCTION_NAME" \
    --gen2 \
    --runtime=python311 \
    --region="$REGION" \
    --source=. \
    --entry-point=calculate_and_publish_volatility \
    --trigger-http \
    --memory=1024MB \
    --timeout=540s \
    --no-allow-unauthenticated

# 3. Retrieve the Cloud Function URL
FUNCTION_URL=$(gcloud functions describe "$FUNCTION_NAME" --gen2 --region="$REGION" --format="value(serviceConfig.uri)")
echo "Function deployed successfully at: $FUNCTION_URL"

# 4. Set up Google Cloud Scheduler Job
echo "[3/4] Creating / Updating Cloud Scheduler Job..."
# Retrieve or create a Service Account for invoking the function
SERVICE_ACCOUNT="volatility-scheduler-sa@${PROJECT_ID}.iam.gserviceaccount.com"

if ! gcloud iam service-accounts describe "$SERVICE_ACCOUNT" > /dev/null 2>&1; then
    echo "Creating scheduler service account: $SERVICE_ACCOUNT"
    gcloud iam service-accounts create volatility-scheduler-sa --display-name="Volatility Scheduler Invoker"
fi

# Grant Invoker role
gcloud run services add-iam-policy-binding "$FUNCTION_NAME" \
    --region="$REGION" \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/run.invoker" > /dev/null 2>&1 || true

# Create or Update Scheduler Job
if gcloud scheduler jobs describe "$SCHEDULER_JOB_NAME" --location="$REGION" > /dev/null 2>&1; then
    echo "Updating existing scheduler job: $SCHEDULER_JOB_NAME"
    gcloud scheduler jobs update http "$SCHEDULER_JOB_NAME" \
        --location="$REGION" \
        --schedule="$SCHEDULE" \
        --time-zone="$TIME_ZONE" \
        --uri="$FUNCTION_URL" \
        --http-method=POST \
        --oidc-service-account-email="$SERVICE_ACCOUNT"
else
    echo "Creating new scheduler job: $SCHEDULER_JOB_NAME"
    gcloud scheduler jobs create http "$SCHEDULER_JOB_NAME" \
        --location="$REGION" \
        --schedule="$SCHEDULE" \
        --time-zone="$TIME_ZONE" \
        --uri="$FUNCTION_URL" \
        --http-method=POST \
        --oidc-service-account-email="$SERVICE_ACCOUNT"
fi

echo "======================================================"
echo "[4/4] Deployment Complete!"
echo "Your volatility engine will now run automatically Monday through Friday at 16:30 EST."
echo "You can trigger a manual test run at any time with:"
echo "  gcloud scheduler jobs run $SCHEDULER_JOB_NAME --location=$REGION"
echo "======================================================"
