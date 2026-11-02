#!/usr/bin/env python3
"""কনটেন্ট আপডেট প্রকাশ: app/src/main/assets/content/*.json থেকে updates/ ফোল্ডার ও manifest.json বানায়।
ব্যবহার: python3 tools/publish_update.py [--bump]   (--bump দিলে সংস্করণ ১ বাড়ে)
আগে tools/check_content.py পাস করাতে হবে। তারপর updates/ কমিট করে main-এ পুশ করলেই অ্যাপ আপডেট পাবে।
আলেমের পর্যালোচনার আগে প্রকাশ করবেন না।"""
import hashlib, json, os, shutil, sys
root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
src = os.path.join(root, "app/src/main/assets/content")
out = os.path.join(root, "updates")
names = ["ilm", "masail", "duas", "quiz", "kids", "emergency", "library"]
mpath = os.path.join(out, "manifest.json")
ver = json.load(open(mpath))["version"] if os.path.exists(mpath) else 1
if "--bump" in sys.argv: ver += 1
os.makedirs(out, exist_ok=True)
files = {}
for n in names:
    b = open(os.path.join(src, n + ".json"), "rb").read()
    json.loads(b.decode("utf-8"))
    shutil.copyfile(os.path.join(src, n + ".json"), os.path.join(out, n + ".json"))
    files[n] = {"path": n + ".json", "sha256": hashlib.sha256(b).hexdigest()}
json.dump({"schema": 1, "version": ver, "files": files}, open(mpath, "w"), indent=1)
print("সংস্করণ", ver, "—", len(files), "ফাইল")
