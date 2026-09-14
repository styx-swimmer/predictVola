import pandas as pd
import numpy as np

# Just create fake data
dates = pd.date_range("2023-01-01", periods=100, freq="B")
closes = pd.Series(np.random.randn(100).cumsum() + 100, index=dates)

pct_returns = closes.pct_change().dropna() * 100.0

weekdays = [
    ("Mon", "Monday", 0),
    ("Tue", "Tuesday", 1),
    ("Wed", "Wednesday", 2),
    ("Thu", "Thursday", 3),
    ("Fri", "Friday", 4)
]
weekday_stats = []

for short_day, full_day, day_idx in weekdays:
    day_rets = pct_returns[pct_returns.index.dayofweek == day_idx]
    if len(day_rets) > 0:
        avg_ret = float(day_rets.mean())
        green_prob = float((day_rets > 0).mean() * 100.0)
    else:
        avg_ret = 0.0
        green_prob = 50.0
        
    weekday_stats.append({
        "day": short_day,
        "dayName": full_day,
        "avgReturn": round(avg_ret, 2),
        "greenProb": round(green_prob, 1)
    })

print(weekday_stats)
