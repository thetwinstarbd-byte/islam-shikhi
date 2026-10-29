# -*- coding: utf-8 -*-
"""
কনটেন্টের দলিল (কুরআনের আয়াত/হাদীস) মেশিনে মিলিয়ে নেওয়ার সহায়ক।

- আরবি লেখা কখনো হাতে টাইপ করা হয় না। কুরআন Tanzil (Uthmani) থেকে, হাদীস সহীহ বুখারী/মুসলিমের
  ডেটাসেট থেকে হুবহু কেটে নেওয়া হয়। কোথাও না মিললে AssertionError দিয়ে থেমে যায়।
- কুরআনের বাংলা অনুবাদ QuranEnc (আবু বকর জাকারিয়া, ১.১.১) থেকে অপরিবর্তিত (পূর্ণ আয়াত)।
- হাদীসের বাংলা অর্থ এই প্রকল্পে লেখা (আলেম-পর্যালোচিত নয়)।

ব্যবহার (স্পেক ফাইল থেকে):  ev = Evidence(QURAN_CLONE, HADITH_CLONE); ev.quran("2:183"); ev.hadith(...)
"""
import json, os, re

BN_DIGITS = "০১২৩৪৫৬৭৮৯"
APP_ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")


def bn(n):
    return "".join(BN_DIGITS[int(c)] if c.isdigit() else c for c in str(n))


def strip(s):
    """হরকত ও বর্ণরূপের পার্থক্য বাদ দিয়ে তুলনার উপযোগী রূপ।"""
    s = re.sub(r"[ً-ٰٟۖ-ۭـ]", "", s)
    s = s.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ٱ", "ا").replace("ى", "ي").replace("ة", "ه")
    s = re.sub(r"[^ء-ي\s]", " ", s)
    return re.sub(r"\s+", " ", s).strip()


def find_seq(tokens, seq, start=0):
    for i in range(start, len(tokens) - len(seq) + 1):
        if tokens[i:i + len(seq)] == seq:
            return i
    return None


BOOK_BN = {"bukhari": "সহীহ বুখারী", "muslim": "সহীহ মুসলিম"}

# হাদীস ডেটাসেটের (হরকতহীন) আরবি অধ্যায়ের নাম → বাংলা
CHAPTER_BN = {
    "كتاب الوضوء": "কিতাবুল উযূ (ওযূ অধ্যায়)",
    "كتاب الايمان": "কিতাবুল ঈমান (ঈমান অধ্যায়)",
    "كتاب الحيض": "কিতাবুল হায়েয (হায়েয অধ্যায়)",
    "كتاب التيمم": "কিতাবুত তায়াম্মুম",
    "كتاب الصلاه": "কিতাবুস সালাত (নামাজ অধ্যায়)",
    "كتاب مواقيت الصلاه": "কিতাবু মাওয়াকীতিস সালাত (নামাজের সময় অধ্যায়)",
    "كتاب الاذان": "কিতাবুল আযান",
    "كتاب التقصير": "কিতাবুত তাকসীর (সফরের নামাজ সংক্ষেপ অধ্যায়)",
    "كتاب الزكاه": "কিতাবুয যাকাত",
    "كتاب البيوع": "কিতাবুল বুয়ূ' (ক্রয়-বিক্রয় অধ্যায়)",
    "كتاب الادب": "কিতাবুল আদাব (শিষ্টাচার অধ্যায়)",
    "كتاب اللباس": "কিতাবুল লিবাস (পোশাক অধ্যায়)",
    "كتاب الجنائز": "কিতাবুল জানাইয (জানাজা অধ্যায়)",
    "كتاب احاديث الانبياء": "কিতাবু আহাদীসিল আম্বিয়া (নবীদের ঘটনাবলী অধ্যায়)",
    "كتاب الحج": "কিতাবুল হজ্জ",
    "كتاب الايمان والنذور": "কিতাবুল আইমান ওয়ান নুযূর (শপথ ও মানত অধ্যায়)",
    "كتاب المساجد ومواضع الصلاه": "কিতাবুল মাসাজিদ ওয়া মাওয়াযি'স সালাত",
    "كتاب المساقاه": "কিতাবুল মুসাকাত",
    "كتاب التوبه": "কিতাবুত তাওবা",
    "كتاب البر والصله والاداب": "কিতাবুল বিররি ওয়াস সিলাতি ওয়াল আদাব",
}


class Evidence:
    def __init__(self, quran_clone, hadith_clone):
        q = quran_clone
        self.ar = json.load(open(f"{q}/data/tanzil/uthmani.json", encoding="utf-8"))
        self.bn_tr = json.load(open(f"{q}/data/quranenc/bengali_zakaria.json", encoding="utf-8"))
        self.chapters = {c["id"]: c for c in json.load(open(os.path.join(APP_ASSETS, "quran", "chapters.json"), encoding="utf-8"))}
        self.H, self.HCH = {}, {}
        for b in ("bukhari", "muslim"):
            d = json.load(open(f"{hadith_clone}/db/by_book/the_9_books/{b}.json", encoding="utf-8"))
            self.H[b] = {h["idInBook"]: h for h in d["hadiths"]}
            self.HCH[b] = {c["id"]: c for c in d["chapters"]}
        self.bism = strip(self.ar["1"][0]["text"]).split()

    # ---------- কুরআন ----------
    def quran(self, ref):
        """ref: "সূরা:আয়াত" বা "সূরা:আয়াত-আয়াত"। পূর্ণ আয়াত, আরবি ও বাংলা উভয়ই অপরিবর্তিত।"""
        ch, rng = ref.split(":")
        ch = int(ch)
        a, _, b = rng.partition("-")
        a = int(a)
        b = int(b) if b else a
        parts = []
        for v in range(a, b + 1):
            toks = self.ar[str(ch)][v - 1]["text"].split()
            if v == 1 and ch not in (1, 9):
                assert [strip(t) for t in toks[:4]] == self.bism, (ch, v)
                toks = toks[4:]
            parts.append(" ".join(toks))
        shown = f"{bn(ch)}:{bn(a)}" + (f"-{bn(b)}" if b != a else "")
        return {
            "kind": "quran",
            "ref": f"কুরআন, সূরা {self.chapters[ch]['bn']} ({shown})",
            "arabic": " ".join(parts),
            "meaningBn": " ".join(self.bn_tr[str(ch)][v - 1]["text"] for v in range(a, b + 1)),
            "creditBn": "আরবি: Tanzil (অপরিবর্তিত)। অনুবাদ: আবু বকর জাকারিয়া (QuranEnc.com, সংস্করণ ১.১.১), কোনো পরিবর্তন ছাড়া।",
        }

    # ---------- হাদীস ----------
    def hadith(self, book, n, start, end, who_en, who_bn, meaning_bn):
        """
        book: bukhari|muslim; n: ডেটাসেটের ক্রম (শুধু টুলের ভেতরে ব্যবহৃত, অ্যাপে দেখানো হয় না);
        start/end: আরবি অংশের প্রথম ও শেষ শব্দগুচ্ছ (হরকত থাকলেও চলে); who_en: ইংরেজি বর্ণনাকারীর নামের অংশ যা
        ডেটাসেটে থাকতেই হবে (যাচাই); who_bn: সূত্রে দেখানোর বাংলা নাম।
        """
        h = self.H[book][n]
        eng = h["english"]["narrator"] + " " + h["english"]["text"]
        assert who_en.lower() in eng.lower(), f"{book} {n}: বর্ণনাকারী '{who_en}' মেলেনি"
        toks = [t for t in h["arabic"].replace("‏", " ").split() if strip(t)]   # শুধু যতিচিহ্নের টোকেন বাদ
        st = [strip(t) for t in toks]
        s = find_seq(st, strip(start).split())
        assert s is not None, f"{book} {n}: শুরু '{start}' মেলেনি"
        e = find_seq(st, strip(end).split(), s)
        assert e is not None, f"{book} {n}: শেষ '{end}' মেলেনি"
        e += len(strip(end).split())
        arabic = " ".join(toks[s:e]).replace('"', "").strip()
        chap = strip(self.HCH[book][h["chapterId"]]["arabic"])
        assert chap in CHAPTER_BN, f"অধ্যায়ের বাংলা নাম নেই: {chap}"
        return {
            "kind": "hadith",
            "ref": f"{BOOK_BN[book]}, {CHAPTER_BN[chap]}, {who_bn} বর্ণিত",
            "arabic": arabic,
            "meaningBn": meaning_bn,
            "creditBn": "আরবি: সহীহ বুখারী/মুসলিমের ডেটাসেট (অপরিবর্তিত)। বাংলা অর্থ এই প্রকল্পে লেখা; আলেম-পর্যালোচিত নয়।",
        }
