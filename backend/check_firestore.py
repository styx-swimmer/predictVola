import firebase_admin
from firebase_admin import credentials, firestore
import json

cred = credentials.Certificate('serviceAccountKey.json')
firebase_admin.initialize_app(cred)
db = firestore.client()

doc = db.collection('market_data').document('latest_forecast').get()
if doc.exists:
    data = doc.to_dict()
    symbols = data.get('symbols', {})
    first_sym = list(symbols.keys())[0]
    print(f"Data for {first_sym}:")
    print(json.dumps(symbols[first_sym]['stats'], indent=2))
else:
    print("Doc not found")
