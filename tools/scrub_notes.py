#!/usr/bin/env python3
"""
masail/duas/kids/emergency/library JSON-এর পাঠকমুখী লেখা পরিষ্কার করে (অ্যাপে যা দেখা যায়):
- "checkNoteBn" (যাচাই-নোট) বাদ; "creditBn" শুধু ডেটাসেটের সূত্রটুকু।
ব্যবহার: python3 tools/scrub_notes.py   (build_masail.py/build_duas.py/build_info.py চালানোর পর আবার চালাতে হবে)
"""
import json, os
D = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "assets", "content")
CREDIT = "আরবি: সহীহ বুখারী/মুসলিমের ডেটাসেট (অপরিবর্তিত)।"


def walk(o):
    if isinstance(o, dict):
        o.pop("checkNoteBn", None)
        if "creditBn" in o and isinstance(o["creditBn"], str):
            o["creditBn"] = CREDIT
        for v in o.values():
            walk(v)
    elif isinstance(o, list):
        for v in o:
            walk(v)


for f in ("masail", "duas", "kids", "emergency", "library"):
    p = os.path.join(D, f + ".json")
    d = json.load(open(p, encoding="utf-8"))
    walk(d)
    json.dump(d, open(p, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("পরিষ্কার হয়েছে")
