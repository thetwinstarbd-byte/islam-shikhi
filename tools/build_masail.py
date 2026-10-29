#!/usr/bin/env python3
"""
tools/content/masail_spec.py থেকে app/src/main/assets/content/masail.json বানায়।
আরবি দলিল Tanzil ও হাদীস ডেটাসেট থেকে হুবহু কেটে নেওয়া হয়; কিছু না মিললে থেমে যায়।

ব্যবহার:
  python3 tools/build_masail.py <risan-quran-json clone> <AhmedBaset/hadith-json clone>
"""
import json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "content"))
from lib_evidence import Evidence
import masail_spec as S

QURAN, HADITH = sys.argv[1], sys.argv[2]
OUT = os.path.join(HERE, "..", "app", "src", "main", "assets", "content", "masail.json")
ev = Evidence(QURAN, HADITH)

cat_ids = {c for c, _ in S.CATEGORIES}
ids = [e["id"] for e in S.E]
assert len(ids) == len(set(ids)), "আইডি ডুপ্লিকেট"

cache = {}
errors = []
def resolve(item):
    if item not in cache:
        try:
            cache[item] = ev.quran(item[1]) if item[0] == "q" else ev.hadith(*item[1:])
        except AssertionError as ex:   # সব ত্রুটি একসঙ্গে দেখানোর জন্য জমানো হয়
            errors.append(str(ex))
            cache[item] = {"kind": "x", "ref": "", "arabic": "", "meaningBn": "", "creditBn": ""}
    return cache[item]

entries = []
for e in S.E:
    assert e["category"] in cat_ids, e["id"]
    assert e["audience"] and all(a in ("ALL", "FEMALE", "MALE") for a in e["audience"]), e["id"]
    for k in ("questionBn", "shortAnswerBn", "detailBn"):
        assert e[k].strip(), (e["id"], k)
    assert e["ev"], f"{e['id']}: দলিল নেই"
    for r in e["related"]:
        assert r in ids, f"{e['id']}: related '{r}' পাওয়া যায়নি"
    entries.append(dict(
        id=e["id"], category=e["category"], audience=e["audience"],
        questionBn=e["questionBn"], shortAnswerBn=e["shortAnswerBn"], detailBn=e["detailBn"],
        evidence=[resolve(x) for x in e["ev"]],
        viewpoints=e["viewpoints"], related=e["related"], askScholar=e["askScholar"],
        verificationStatus="SOURCE_CHECKED", checkNoteBn=e["checkNoteBn"],
    ))

if errors:
    print("ত্রুটি:")
    for x in dict.fromkeys(errors): print(" -", x)
    sys.exit(1)

doc = {"schemaVersion": 1, "categories": [{"id": i, "titleBn": t} for i, t in S.CATEGORIES], "entries": entries}
os.makedirs(os.path.dirname(OUT), exist_ok=True)
json.dump(doc, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("মাসআলা:", len(entries), "| ভিন্ন দলিল:", len(cache))
for x in entries:
    print("-", x["id"], f"({len(x['evidence'])} দলিল)")
    for d in x["evidence"]:
        print("     ", d["ref"][:70], "|", d["arabic"][:60])
