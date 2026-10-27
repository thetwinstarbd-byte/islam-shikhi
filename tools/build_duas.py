#!/usr/bin/env python3
"""
tools/content/duas_spec.py থেকে app/src/main/assets/content/duas.json বানায়।
আরবি লেখা কুরআন (Tanzil) ও হাদীস ডেটাসেট থেকে হুবহু কেটে নেওয়া হয়; কোথাও না মিললে স্ক্রিপ্ট থেমে যায়।

ব্যবহার:
  python3 tools/build_duas.py <risan-quran-json clone> <AhmedBaset/hadith-json clone>
"""
import json, os, re, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "content"))
import duas_spec as S

QURAN, HADITH = sys.argv[1], sys.argv[2]
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "content", "duas.json")

def strip(s):
    s = re.sub(r"[ً-ٰٟۖ-ۭـ]", "", s)
    s = s.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ٱ", "ا").replace("ى", "ي").replace("ة", "ه")
    s = re.sub(r"[^ء-ي\s]", " ", s)
    return re.sub(r"\s+", " ", s).strip()

BN_DIGITS = "০১২৩৪৫৬৭৮৯"
def bn(n): return "".join(BN_DIGITS[int(c)] if c.isdigit() else c for c in str(n))

ar = json.load(open(f"{QURAN}/data/tanzil/uthmani.json", encoding="utf-8"))
bn_tr = json.load(open(f"{QURAN}/data/quranenc/bengali_zakaria.json", encoding="utf-8"))
chapters = {c["id"]: c for c in json.load(open(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "quran", "chapters.json"), encoding="utf-8"))}

H = {}
HCH = {}
for b in ("bukhari", "muslim"):
    d = json.load(open(f"{HADITH}/db/by_book/the_9_books/{b}.json", encoding="utf-8"))
    H[b] = {h["idInBook"]: h for h in d["hadiths"]}
    HCH[b] = {c["id"]: c for c in d["chapters"]}

BISM = strip(ar["1"][0]["text"]).split()

def narrator_bn(eng):
    for k in sorted(S.NARRATOR_BN, key=len, reverse=True):
        if k.strip() in eng: return S.NARRATOR_BN[k]
    raise SystemExit("বর্ণনাকারী মেলেনি: " + eng)

def hadith_source(book, n):
    h = H[book][n]
    ch = HCH[book][h["chapterId"]]["arabic"]
    if ch not in S.CHAPTER_BN: raise SystemExit("অধ্যায়ের বাংলা নাম নেই: " + ch)
    return f"{S.BOOK_BN[book]}, {S.CHAPTER_BN[ch]}, {narrator_bn(h['english']['narrator'])} বর্ণিত"

def find_seq(stripped_tokens, seq, start=0):
    for i in range(start, len(stripped_tokens) - len(seq) + 1):
        if stripped_tokens[i:i + len(seq)] == seq: return i
    return None

def quran_text(ref, frm, to):
    ch, rng = ref.split(":")
    ch = int(ch)
    a, _, b = rng.partition("-")
    a = int(a); b = int(b) if b else a
    verses = []
    for v in range(a, b + 1):
        toks = ar[str(ch)][v - 1]["text"].split()
        if v == 1 and ch not in (1, 9):
            assert [strip(t) for t in toks[:4]] == BISM, (ch, v)
            toks = toks[4:]
        verses.append(toks)
    if frm:
        seq = strip(frm).split()
        st = [strip(t) for t in verses[0]]
        i = find_seq(st, seq)
        assert i is not None, f"{ref}: from '{frm}' মেলেনি"
        verses[0] = verses[0][i:]
    if to:
        seq = strip(to).split()
        st = [strip(t) for t in verses[-1]]
        i = find_seq(st, seq)
        assert i is not None, f"{ref}: to '{to}' মেলেনি"
        verses[-1] = verses[-1][:i + len(seq)]
    return " ".join(" ".join(v) for v in verses), ch, a, b

def hadith_text(book, n, start, end):
    h = H[book][n]
    toks = h["arabic"].replace("‏", " ").split()
    st = [strip(t) for t in toks]
    s = find_seq(st, strip(start).split())
    assert s is not None, f"{book} {n}: শুরু '{start}' মেলেনি"
    e = find_seq(st, strip(end).split(), s)
    assert e is not None, f"{book} {n}: শেষ '{end}' মেলেনি"
    e += len(strip(end).split())
    return " ".join(toks[s:e]).replace('"', "").strip()

entries = []
for e in S.E:
    out = dict(id=e["id"], category=e["category"], titleBn=e["title"], transliterationBn=e.get("translit"),
               usageBn=e.get("usage"), suggestedCount=e.get("count"), hadithGrade=None,
               viewpoints=[], audience=["ALL"], verificationStatus="SOURCE_CHECKED", arabicChecked=True,
               reviewedBy=None, reviewedOn=None, quranRef=None, meaningCreditBn=None)
    sources = []
    if e.get("q"):
        text, ch, a, b = quran_text(e["q"], e.get("frm"), e.get("to"))
        out["arabic"] = text
        out["quranRef"] = {"chapter": ch, "from": a, "to": b}
        ref = f"{bn(ch)}:{bn(a)}" + (f"-{bn(b)}" if b != a else "")
        sources.append(f"কুরআন, সূরা {chapters[ch]['bn']} ({ref})")
        out["checkNoteBn"] = "আরবি টেক্সট Tanzil থেকে হুবহু নেওয়া।"
        if e.get("meaning_from_quran"):
            out["meaningBn"] = " ".join(bn_tr[str(ch)][v - 1]["text"] for v in range(a, b + 1))
            out["meaningCreditBn"] = "অনুবাদ: আবু বকর জাকারিয়া (QuranEnc.com, সংস্করণ ১.১.১); কোনো পরিবর্তন ছাড়া।"
        else:
            out["meaningBn"] = e["meaning"]
        if e.get("extra_h"):
            bk, n = e["extra_h"]
            sources.append(hadith_source(bk, n))
            out["hadithGrade"] = S.SAHIHAIN
            out["checkNoteBn"] += " হাদীসের সূত্র হাদীস ডেটাসেটে খুঁজে পাওয়া গেছে।"
    else:
        bk, n, st, en = e["h"]
        out["arabic"] = hadith_text(bk, n, st, en)
        out["meaningBn"] = e["meaning"]
        sources.append(hadith_source(bk, n))
        out["hadithGrade"] = S.SAHIHAIN
        out["checkNoteBn"] = "আরবি টেক্সট সহীহ বুখারী/মুসলিমের ডেটাসেট (sunnah.com ভিত্তিক) থেকে হুবহু নেওয়া।"
        if e.get("extra_q"):
            pass
    if e.get("extra_h") and e["id"].startswith(("ikhlas", "falaq", "nas")):
        pass
    out["sourceRef"] = "; ".join(sources)
    entries.append(out)

doc = {
    "schemaVersion": 1,
    "categories": [{"id": i, "titleBn": t} for i, t in S.CATEGORIES],
    "entries": entries,
}
os.makedirs(os.path.dirname(OUT), exist_ok=True)
json.dump(doc, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("এন্ট্রি:", len(entries))
for x in entries:
    print("-", x["id"], "|", x["sourceRef"][:90]); print("   ", x["arabic"][:110])
