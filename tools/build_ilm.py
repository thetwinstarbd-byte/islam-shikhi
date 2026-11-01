#!/usr/bin/env python3
"""
tools/content/ilm_*.py থেকে app/src/main/assets/content/ilm.json বানায় ("ইসলামি জ্ঞান": বিস্তারিত প্রবন্ধ)।

নিয়ম (docs/CONTENT_VERIFICATION.md):
- কুরআনের আয়াত: শুধু সূরা ও আয়াত-নম্বর স্পেকে থাকে; আরবি ও বাংলা অনুবাদ অ্যাপ চালু অবস্থায় অ্যাপের নিজস্ব
  কুরআন-ডেটা থেকে আসে (হাতে লেখা নয়)। এখানে নম্বর ডেটাসেটে আছে কি না যাচাই হয়।
- হাদীস: আরবি অংশ সহীহ বুখারী/মুসলিমের ডেটাসেট থেকে হুবহু কেটে নেওয়া (শুরু ও শেষের শব্দগুচ্ছ না মিললে বিল্ড থামে),
  বর্ণনাকারীর নামও ডেটাসেটের সঙ্গে মেলানো হয়। বাংলা অর্থ এই প্রকল্পে লেখা, আলেম-পর্যালোচিত নয়।
- আলেমদের মত ও বইয়ের নাম: ডেটাসেট নেই বলে মেশিনে মেলানো যায় না; সূত্র-লাইনে তা খোলাখুলি লেখা থাকে।
- verificationStatus: প্রবন্ধে "SOURCE_CHECKED" মানে আয়াত/হাদীস মেলানো; ব্যাখ্যা আলেম দ্বারা পর্যালোচিত নয়।

ব্যবহার:
  python3 tools/build_ilm.py <risan-quran-json clone> <AhmedBaset/hadith-json clone>
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "content"))
from lib_evidence import Evidence, BOOK_BN, CHAPTER_BN, strip  # noqa: E402
import ilm_core as C  # noqa: E402
import ilm_a  # noqa: E402,F401  (প্রবন্ধ-গুচ্ছ ১)
import ilm_b  # noqa: E402,F401  (প্রবন্ধ-গুচ্ছ ২)
import ilm_c  # noqa: E402,F401  (প্রবন্ধ-গুচ্ছ ৩)
import ilm_d  # noqa: E402,F401  (ইবাদত/সালাত)
import ilm_e  # noqa: E402,F401  (সিয়াম/যাকাত/হজ্জ/আখলাক)
import ilm_f  # noqa: E402,F401  (সীরাত)
import ilm_x1  # noqa: E402,F401  (বিস্তারিত সংযোজন: আকীদা)
import ilm_x2  # noqa: E402,F401  (বিস্তারিত সংযোজন: ইবাদত ও আখলাক)
import ilm_x3  # noqa: E402,F401  (বিস্তারিত সংযোজন: সীরাত)

# এই প্রবন্ধগুলোর জন্য প্রয়োজনীয় অতিরিক্ত অধ্যায়ের বাংলা নাম (কী = ডেটাসেটের হরকতহীন আরবি নাম)
CHAPTER_BN.update({
    "كتاب الاعتصام بالكتاب والسنه": "কিতাবুল ই'তিসাম বিল কিতাবি ওয়াস সুন্নাহ (কুরআন-সুন্নাহ আঁকড়ে ধরা অধ্যায়)",
    "كتاب التفسير": "কিতাবুত তাফসীর",
    "كتاب الوصايا": "কিতাবুল ওয়াসায়া (অসিয়ত অধ্যায়)",
    "كتاب الاستئذان": "কিতাবুল ইস্তিযান (অনুমতি প্রার্থনা অধ্যায়)",
    "كتاب الزهد والرقائق": "কিতাবুয যুহদ ওয়ার রাকাইক",
    "كتاب الاقضيه": "কিতাবুল আকযিয়া (বিচার-ফয়সালা অধ্যায়)",
    "كتاب صلاه التراويح": "কিতাবু সালাতিত তারাবীহ",
    "كتاب العلم": "কিতাবুল ইলম",
    "كتاب المناقب": "কিতাবুল মানাকিব",
    "كتاب الجمعه": "কিতাবুল জুমুআ (জুমুআর অধ্যায়)",
    "كتاب بدء الوحي": "কিতাবু বাদইল ওহী (ওহীর সূচনা অধ্যায়)",
    "كتاب الصوم": "কিতাবুস সাওম (রোযা অধ্যায়)",
    "كتاب العمره": "কিতাবুল উমরা (উমরা অধ্যায়)",
    "كتاب القدر": "কিতাবুল কদর (তাকদীর অধ্যায়)",
    "كتاب فضائل الصحابه رضي الله تعالي عنهم": "কিতাবু ফাযাইলিস সাহাবা (সাহাবীদের মর্যাদা অধ্যায়)",
})

QURAN, HADITH = sys.argv[1], sys.argv[2]
OUT = os.path.join(HERE, "..", "app", "src", "main", "assets", "content", "ilm.json")
ev = Evidence(QURAN, HADITH)
errors = []


def ayah_ref(s):
    try:
        ev.quran(s)  # নম্বর বৈধ কি না
        ch, rng = s.split(":")
        a, _, b = rng.partition("-")
        return {"chapter": int(ch), "from": int(a), "to": int(b or a)}
    except Exception as ex:  # noqa: BLE001
        errors.append(f"কুরআন {s}: {ex!r}")
        return {"chapter": 1, "from": 1, "to": 1}


def hadith(h):
    book, n, start, end, who_en, who_bn, meaning, grade = h
    try:
        r = ev.hadith(book, n, start, end, who_en, who_bn, meaning)
    except Exception as ex:  # noqa: BLE001
        errors.append(f"{book} {n}: {ex!r}")
        return {"ref": "", "narratorBn": who_bn, "arabic": None, "meaningBn": meaning, "gradeBn": grade}
    return {"ref": r["ref"], "narratorBn": who_bn, "arabic": r["arabic"], "meaningBn": meaning, "gradeBn": grade}


def section(s):
    return {
        "headingBn": s["h"],
        "paragraphs": s["p"],
        "ayahs": [ayah_ref(a) for a in s.get("q", [])],
        "hadiths": [hadith(h) for h in s.get("h_", [])],
        "viewpoints": [{"titleBn": t, "textBn": x, "sourceRef": src} for (t, x, src) in s.get("v", [])],
    }


articles = []
for a in C.ARTICLES:
    a["sections"] = a["sections"] + C.EXTRA.get(a["id"], [])
    articles.append({
        "id": a["id"], "category": a["cat"], "titleBn": a["title"], "summaryBn": a["summary"], "levelBn": a["level"],
        "sections": [section(s) for s in a["sections"]],
        "readingBn": a.get("reading", []),
        "verificationStatus": "SOURCE_CHECKED",
        "checkNoteBn": C.NOTE,
    })

if errors:
    print("ত্রুটি:\n" + "\n".join(errors))
    sys.exit(1)

out = {"schemaVersion": 1, "categories": [{"id": i, "titleBn": t, "introBn": x} for (i, t, x) in C.CATEGORIES], "articles": articles}
with open(OUT, "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
words = sum(len(p.split()) for a in articles for s in a["sections"] for p in s["paragraphs"])
print(f"ilm.json তৈরি: {len(articles)}টি প্রবন্ধ, প্রায় {words} শব্দ")
