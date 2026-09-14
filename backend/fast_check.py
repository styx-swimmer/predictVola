import firebase_admin
from firebase_admin import credentials, firestore
import json

try:
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    doc = db.collection('market_data').document('latest_forecast').get()
    if doc.exists:
        data = doc.to_dict()
        symbols = data.get('symbols', {})
        first_sym = list(symbols.keys())[0]
        stats = symbols[first_sym].get('stats', {})
        print("Keys in stats:", list(stats.keys()))
        print("weekdayStats type:", type(stats.get('weekdayStats')))
        print("weekdayStats value:", stats.get('weekdayStats'))
    else:
        print("Doc not found")
except Exception as e:
    print(f"Error: {e}")
