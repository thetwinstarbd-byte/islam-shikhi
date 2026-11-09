#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""নামাজের সময় যাচাই: ইসলামিক ফাউন্ডেশনের (বা মসজিদের) সময়সূচি CSV-র সাথে অ্যাপের গণনা (করাচি ১৮°, হানাফি আসর) মেলায়।

ব্যবহার:  python3 tools/check_prayer_times.py if_times.csv
CSV-র কলাম (প্রথম সারি শিরোনাম):  district,date,fajr,sunrise,dhuhr,asr,maghrib,isha
  district = অ্যাপের শহর-id (যেমন dhaka, sylhet — app/.../PrayerModels.kt দেখুন); date = YYYY-MM-DD; সময় = HH:MM (২৪ ঘণ্টা)
আউটপুটে প্রতিটি ওয়াক্তের পার্থক্য (মিনিট; + মানে অ্যাপের সময় বেশি) দেখানো হয়।
এই স্ক্রিপ্ট adhan2-এর মতো সাধারণ সৌর-সূত্র ব্যবহার করে, তাই ১ মিনিটের কাছাকাছি পার্থক্য স্বাভাবিক।
"""
import csv, math, re, sys, datetime as dt, pathlib

def cities():
    t = pathlib.Path(__file__).resolve().parent.parent.joinpath("app/src/main/java/com/islamshikhi/app/feature/prayer/PrayerModels.kt").read_text(encoding="utf-8")
    return {m[0]: (float(m[2]), float(m[3])) for m in re.findall(r'City\("(\w+)", "([^"]*)", ([\d.\-]+), ([\d.\-]+)\)', t)}

def sun(jd):
    d = jd - 2451545.0
    g = math.radians((357.529 + 0.98560028 * d) % 360)
    q = (280.459 + 0.98564736 * d) % 360
    L = math.radians((q + 1.915 * math.sin(g) + 0.020 * math.sin(2 * g)) % 360)
    e = math.radians(23.439 - 0.00000036 * d)
    ra = math.degrees(math.atan2(math.cos(e) * math.sin(L), math.cos(L))) / 15 % 24
    return math.asin(math.sin(e) * math.sin(L)), ((q / 15 - ra) + 12) % 24 - 12

def compute(date, lat, lon, tz=6, hanafi=True):
    jd = date.toordinal() + 1721424.5
    decl, eqt = sun(jd + 0.5 - lon / 360)
    noon = 12 + tz - lon / 15 - eqt
    la = math.radians(lat)
    def ang(a):
        return math.degrees(math.acos((-math.sin(math.radians(a)) - math.sin(la) * math.sin(decl)) / (math.cos(la) * math.cos(decl)))) / 15
    k = 2 if hanafi else 1
    alt = math.degrees(math.atan(1 / (k + math.tan(abs(la - decl)))))
    asr = noon + math.degrees(math.acos((math.sin(math.radians(alt)) - math.sin(la) * math.sin(decl)) / (math.cos(la) * math.cos(decl)))) / 15
    return {"fajr": noon - ang(18), "sunrise": noon - ang(0.833), "dhuhr": noon, "asr": asr, "maghrib": noon + ang(0.833), "isha": noon + ang(18)}

def hm(s):
    h, m = s.split(":"); return int(h) + int(m) / 60

if __name__ == "__main__":
    C = cities()
    rows = list(csv.DictReader(open(sys.argv[1], encoding="utf-8")))
    worst = 0
    for r in rows:
        lat, lon = C[r["district"]]
        calc = compute(dt.date.fromisoformat(r["date"]), lat, lon)
        out = []
        for k in ("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"):
            if r.get(k):
                diff = round((calc[k] - hm(r[k])) * 60)
                worst = max(worst, abs(diff)); out.append(f"{k} {diff:+d}")
        print(r["district"], r["date"], " ".join(out))
    print("সর্বোচ্চ পার্থক্য:", worst, "মিনিট")
