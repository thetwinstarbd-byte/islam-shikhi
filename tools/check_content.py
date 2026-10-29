#!/usr/bin/env python3
"""
app/src/main/assets/content/*.json-এর নিয়ম-যাচাই (docs/CONTENT_VERIFICATION.md)। GitHub Actions-এ প্রতিবার চলে।
ব্যবহার: python3 tools/check_content.py
কোনো নিয়ম ভাঙলে ত্রুটি দেখিয়ে exit 1।

বিশেষ নিয়ম: কোনো আলেম কনটেন্ট দেখে অনুমোদন না দেওয়া পর্যন্ত "SCHOLAR_REVIEWED" অবস্থা নিষিদ্ধ। আলেম
সত্যিই দেখে দিলে এই স্ক্রিপ্টে ALLOW_SCHOLAR_REVIEWED আইডির তালিকায় সেই এন্ট্রির আইডি যোগ করুন।
"""
import json, os, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "assets", "content")
VALID = {"DRAFT", "SOURCE_CHECKED", "SCHOLAR_REVIEWED"}
ALLOW_SCHOLAR_REVIEWED = set()   # আলেম-পর্যালোচিত এন্ট্রির আইডি এখানে
errors = []


def load(name):
    with open(os.path.join(ROOT, name), encoding="utf-8") as f:
        return json.load(f)


def status_ok(kind, e):
    st = e.get("verificationStatus")
    if st not in VALID:
        errors.append(f"{kind} {e.get('id')}: অবৈধ verificationStatus {st!r}")
    if st == "SCHOLAR_REVIEWED" and e.get("id") not in ALLOW_SCHOLAR_REVIEWED:
        errors.append(f"{kind} {e.get('id')}: কোনো আলেম পর্যালোচনা করেননি, অথচ SCHOLAR_REVIEWED")


def unique(kind, items):
    ids = [i["id"] for i in items]
    for d in {x for x in ids if ids.count(x) > 1}:
        errors.append(f"{kind}: আইডি ডুপ্লিকেট {d}")
    return set(ids)


# ---- দোয়া ----
d = load("duas.json")
cats = {c["id"] for c in d["categories"]}
unique("dua", d["entries"])
for e in d["entries"]:
    if not e.get("sourceRef", "").strip():
        errors.append(f"dua {e['id']}: sourceRef ফাঁকা")
    if e["category"] not in cats:
        errors.append(f"dua {e['id']}: অজানা বিভাগ {e['category']}")
    if not e.get("arabic", "").strip():
        errors.append(f"dua {e['id']}: আরবি ফাঁকা")
    status_ok("dua", e)

# ---- মাসআলা ----
m = load("masail.json")
cats = {c["id"] for c in m["categories"]}
ids = unique("masala", m["entries"])
for e in m["entries"]:
    if e["category"] not in cats:
        errors.append(f"masala {e['id']}: অজানা বিভাগ")
    if not e["evidence"]:
        errors.append(f"masala {e['id']}: দলিল নেই")
    for ev in e["evidence"]:
        if not (ev.get("ref") or "").strip() or not (ev.get("arabic") or "").strip():
            errors.append(f"masala {e['id']}: দলিলের সূত্র/আরবি ফাঁকা")
    for r in e["related"]:
        if r not in ids:
            errors.append(f"masala {e['id']}: related '{r}' নেই")
    if not set(e["audience"]) <= {"ALL", "FEMALE", "MALE"} or not e["audience"]:
        errors.append(f"masala {e['id']}: অবৈধ audience")
    status_ok("masala", e)

# ---- শিশু, জরুরি, লাইব্রেরি ----
for name in ("kids.json", "emergency.json", "library.json"):
    x = load(name)
    cats = {c["id"] for c in x["categories"]}
    unique(name, x["items"])
    for it in x["items"]:
        if it["category"] not in cats:
            errors.append(f"{name} {it['id']}: অজানা বিভাগ")
        if not it["sections"]:
            errors.append(f"{name} {it['id']}: সেকশন নেই")
        if not any((s.get("sourceRef") or "").strip() for s in it["sections"]):
            errors.append(f"{name} {it['id']}: কোনো সেকশনে সূত্র নেই")
        status_ok(name, it)

# ---- কুইজ ----
q = load("quiz.json")["questions"]
unique("quiz", q)
for e in q:
    if len(e["options"]) != 4 or not 0 <= e["answerIndex"] < 4:
        errors.append(f"quiz {e['id']}: বিকল্প/উত্তরের ক্রম ভুল")
    if len(set(e["options"])) != 4:
        errors.append(f"quiz {e['id']}: বিকল্প ডুপ্লিকেট")
    if not (e.get("sourceRef") or "").strip():
        errors.append(f"quiz {e['id']}: sourceRef নেই")

if errors:
    print("কনটেন্ট যাচাই ব্যর্থ:")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print(f"কনটেন্ট ঠিক আছে: দোয়া {len(d['entries'])}, মাসআলা {len(m['entries'])}, কুইজ {len(q)}")
