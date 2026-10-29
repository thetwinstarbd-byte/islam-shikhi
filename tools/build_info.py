#!/usr/bin/env python3
"""
tools/content/info_spec.py থেকে kids.json, emergency.json, library.json ও quiz.json বানায়।
আয়াত/হাদীসের সূত্র ডেটাসেটের সঙ্গে মেলানো হয়; কিছু না মিললে সব ত্রুটি একসঙ্গে দেখিয়ে থেমে যায়।

ব্যবহার:
  python3 tools/build_info.py <risan-quran-json clone> <AhmedBaset/hadith-json clone>
"""
import json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "content"))
from lib_evidence import Evidence, BOOK_BN, CHAPTER_BN, strip, bn
import info_spec as S

QURAN, HADITH = sys.argv[1], sys.argv[2]
OUT = os.path.join(HERE, "..", "app", "src", "main", "assets", "content")
ev = Evidence(QURAN, HADITH)
errors = []


def label(ref):
    """সূত্র যাচাই করে পাঠকের জন্য লেবেল দেয়।"""
    kind = ref[0]
    if kind == "q":
        try:
            return ev.quran(ref[1])["ref"]
        except Exception as ex:
            errors.append(f"কুরআন {ref[1]}: {ex!r}")
            return ""
    if kind == "h":
        _, book, n, who_en, who_bn, kws = ref
        try:
            h = ev.H[book][n]
            text = (h["english"]["narrator"] + " " + h["english"]["text"]).lower()
            assert who_en.lower() in text, f"বর্ণনাকারী '{who_en}' মেলেনি"
            for k in kws:
                assert k.lower() in text, f"শব্দ '{k}' মেলেনি"
            chap = strip(ev.HCH[book][h["chapterId"]]["arabic"])
            assert chap in CHAPTER_BN, f"অধ্যায়ের বাংলা নাম নেই: {chap}"
            return f"{BOOK_BN[book]}, {CHAPTER_BN[chap]}, {who_bn} বর্ণিত"
        except Exception as ex:
            errors.append(f"{book} {n}: {ex!r}")
            return ""
    if kind == "t":
        return ref[1]
    raise ValueError(ref)


def sections(secs):
    out = []
    for s in secs:
        labels = [x for x in (label(r) for r in s["refs"]) if x]
        out.append(dict(titleBn=s["titleBn"], bodyBn=s["bodyBn"], sourceRef="; ".join(dict.fromkeys(labels)) or None))
    return out


def info(path, cats, items):
    ids = [i["id"] for i in items]
    assert len(ids) == len(set(ids)), f"{path}: আইডি ডুপ্লিকেট"
    cid = {c for c, _ in cats}
    built = []
    for it in items:
        assert it["category"] in cid, it["id"]
        secs = sections(it["sections"])
        assert secs and all(s["bodyBn"].strip() for s in secs), it["id"]
        built.append(dict(id=it["id"], category=it["category"], titleBn=it["titleBn"], summaryBn=it["summaryBn"],
                          sections=secs, audience=["ALL"], minAgeGroup=None,
                          verificationStatus="SOURCE_CHECKED", checkNoteBn=S.NOTE))
    doc = {"schemaVersion": 1, "categories": [{"id": i, "titleBn": t} for i, t in cats], "items": built}
    json.dump(doc, open(os.path.join(OUT, path), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(path, len(built))


chapters = ev.chapters   # id -> {bn, verses, ...}

def fact(ref):
    k = ref[1]
    if k == "chapters_total":
        assert len(chapters) == 114
    elif k == "verses":
        assert chapters[ref[2]]["verses"] == ref[3], f"সূরা {ref[2]}: আয়াত {chapters[ref[2]]['verses']} ≠ {ref[3]}"
    elif k == "longest":
        top = max(chapters.values(), key=lambda c: c["verses"])
        assert top["bn"] == ref[2], f"সবচেয়ে বড় সূরা {top['bn']}"
        assert sorted(c["verses"] for c in chapters.values())[-2] < top["verses"]
    elif k == "no_bismillah":
        first = ev.ar[str(ref[2])][0]["text"].split()
        assert [strip(t) for t in first[:4]] != ev.bism, "এই সূরায় বিসমিল্লাহ আছে"
        # বাকি সব সূরায় (ফাতিহা বাদে) শুরুতে বিসমিল্লাহ আছে কি না
        others = [c for c in range(2, 115) if c != 9 and [strip(t) for t in ev.ar[str(c)][0]["text"].split()[:4]] != ev.bism]
        assert not others, f"আরও সূরা বিসমিল্লাহ ছাড়া: {others}"
    else:
        raise ValueError(k)
    return "কুরআনের সূরা-তালিকা ও টেক্সট (অ্যাপের ডেটা থেকে মেলানো)"


info("kids.json", S.KIDS_CATEGORIES, S.KIDS)
info("emergency.json", S.EMERGENCY_CATEGORIES, S.EMERGENCY)
info("library.json", S.LIBRARY_CATEGORIES, S.LIBRARY)

qs = []
for q in S.QUIZ:
    assert len(q["o"]) == 4 and 0 <= q["a"] < 4, q["id"]
    ref = q["ref"]
    try:
        src = fact(ref) if ref[0] == "fact" else label(ref)
    except AssertionError as ex:
        errors.append(f"{q['id']}: {ex}")
        src = ""
    qs.append(dict(id=q["id"], topic=q["topic"], questionBn=q["q"], options=q["o"], answerIndex=q["a"],
                   explanationBn=q["e"], sourceRef=src or None))
ids = [q["id"] for q in qs]
assert len(ids) == len(set(ids))

if errors:
    print("ত্রুটি:")
    for e in dict.fromkeys(errors):
        print(" -", e)
    sys.exit(1)

json.dump({"schemaVersion": 1, "questions": qs}, open(os.path.join(OUT, "quiz.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("quiz.json", len(qs))
