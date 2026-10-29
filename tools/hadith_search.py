#!/usr/bin/env python3
"""হাদীস ডেটাসেটে ইংরেজি শব্দ দিয়ে খোঁজার সহায়ক (কনটেন্ট লেখার সময় সঠিক হাদীস বেছে নিতে)।
ব্যবহার: python3 tools/hadith_search.py <hadith-json clone> <bukhari|muslim> "regex" [আরও regex...]
সব regex একসঙ্গে মিলতে হবে (case-insensitive)।"""
import json, re, sys
root, book, *pats = sys.argv[1:]
d = json.load(open(f"{root}/db/by_book/the_9_books/{book}.json", encoding="utf-8"))
ch = {c["id"]: c for c in d["chapters"]}
n = 0
for h in d["hadiths"]:
    t = re.sub(r"\s+", " ", h["english"]["narrator"] + " " + h["english"]["text"])
    if all(re.search(p, t, re.I) for p in pats):
        n += 1
        print(f'[{book} {h["idInBook"]}] {ch[h["chapterId"]]["arabic"]} | {h["english"]["narrator"].strip()}')
        print("   ", t[:330])
        if n >= 3: break
if n == 0: print("কিছু মেলেনি")
