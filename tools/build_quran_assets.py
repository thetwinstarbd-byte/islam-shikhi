#!/usr/bin/env python3
"""
কুরআনের অ্যাসেট তৈরি করে app/src/main/assets/quran/ এ রাখে।

উৎস (দুটোই https://github.com/risan/quran-json থেকে নেওয়া স্ন্যাপশট):
  - আরবি: Tanzil Uthmani (CC BY 3.0, শুধু অপরিবর্তিত কপির অনুমতি)
  - বাংলা: QuranEnc "Bengali Translation - Abu Bakr Zakaria" v1.1.1
টেক্সট একটুও বদলানো হয় না; শুধু সূরা অনুযায়ী ফাইলে ভাগ করা হয়।

ব্যবহার:  python3 tools/build_quran_assets.py /path/to/risan-quran-json-clone
"""
import json, os, re, sys

SRC = sys.argv[1]
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "quran")

BN_NAMES = """আল-ফাতিহা|আল-বাকারা|আলে ইমরান|আন-নিসা|আল-মায়িদাহ|আল-আন'আম|আল-আ'রাফ|আল-আনফাল|আত-তাওবাহ|ইউনুস|
হূদ|ইউসুফ|আর-রা'দ|ইবরাহীম|আল-হিজর|আন-নাহল|আল-ইসরা|আল-কাহফ|মারইয়াম|ত্বা-হা|
আল-আম্বিয়া|আল-হাজ্জ|আল-মু'মিনূন|আন-নূর|আল-ফুরকান|আশ-শু'আরা|আন-নামল|আল-কাসাস|আল-আনকাবূত|আর-রূম|
লুকমান|আস-সাজদাহ|আল-আহযাব|সাবা|ফাতির|ইয়া-সীন|আস-সাফফাত|সোয়াদ|আয-যুমার|গাফির|
ফুসসিলাত|আশ-শূরা|আয-যুখরুফ|আদ-দুখান|আল-জাসিয়াহ|আল-আহকাফ|মুহাম্মাদ|আল-ফাতহ|আল-হুজুরাত|ক্বাফ|
আয-যারিয়াত|আত-তূর|আন-নাজম|আল-কামার|আর-রাহমান|আল-ওয়াকি'আহ|আল-হাদীদ|আল-মুজাদালাহ|আল-হাশর|আল-মুমতাহিনাহ|
আস-সাফ|আল-জুমু'আহ|আল-মুনাফিকূন|আত-তাগাবুন|আত-তালাক|আত-তাহরীম|আল-মুলক|আল-কলম|আল-হাক্কাহ|আল-মা'আরিজ|
নূহ|আল-জিন|আল-মুয্যাম্মিল|আল-মুদ্দাস্সির|আল-কিয়ামাহ|আল-ইনসান|আল-মুরসালাত|আন-নাবা|আন-নাযি'আত|'আবাসা|
আত-তাকবীর|আল-ইনফিতার|আল-মুতাফফিফীন|আল-ইনশিকাক|আল-বুরূজ|আত-তারিক|আল-আ'লা|আল-গাশিয়াহ|আল-ফাজর|আল-বালাদ|
আশ-শামস|আল-লাইল|আদ-দুহা|আল-ইনশিরাহ|আত-তীন|আল-'আলাক|আল-কদর|আল-বাইয়্যিনাহ|আয-যিলযাল|আল-'আদিয়াত|
আল-কারি'আহ|আত-তাকাসুর|আল-'আসর|আল-হুমাযাহ|আল-ফীল|কুরাইশ|আল-মা'ঊন|আল-কাওসার|আল-কাফিরূন|আন-নাসর|
আল-মাসাদ|আল-ইখলাস|আল-ফালাক|আন-নাস""".replace("\n", "").split("|")
assert len(BN_NAMES) == 114, len(BN_NAMES)

def load(rel):
    with open(os.path.join(SRC, rel), encoding="utf-8") as f:
        return json.load(f)

def dump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, separators=(",", ":"))

ar = load("data/tanzil/uthmani.json")
ar_clean = load("data/tanzil/simple-clean.json")
bn = load("data/quranenc/bengali_zakaria.json")
chapters = load("data/tanzil/chapters.json")["chapters"]

meta = []
for c in chapters:
    n = c["id"]
    a, b, k = ar[str(n)], bn[str(n)], ar_clean[str(n)]
    assert len(a) == len(b) == len(k) == c["total_verses"], n
    for i, (x, y) in enumerate(zip(a, b), 1):
        assert x["verse"] == i and y["verse"] == i
    dump(f"{OUT}/ar/{n}.json", [x["text"] for x in a])
    dump(f"{OUT}/bn/{n}.json", [y["text"] for y in b])
    fns = {str(y["verse"]): y["footnotes"] for y in b if (y.get("footnotes") or "").strip()}
    dump(f"{OUT}/bnfn/{n}.json", fns)
    dump(f"{OUT}/search_ar/{n}.json", [x["text"] for x in k])
    meta.append({
        "id": n, "ar": c["name"], "bn": BN_NAMES[n - 1], "en": c["transliteration"],
        "meaning": c["translation"], "type": c["type"], "verses": c["total_verses"],
    })

dump(f"{OUT}/chapters.json", meta)
print("সূরা:", len(meta), "আয়াত:", sum(m["verses"] for m in meta))
