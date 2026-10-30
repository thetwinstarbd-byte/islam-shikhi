#!/usr/bin/env python3
"""
Emulator-এ অ্যাপ চালিয়ে নিজে নিজে সেটআপ পার করে, পাঁচ ট্যাবের ভেতরের বাটনগুলোতে ট্যাপ করে
এবং ক্র্যাশ হলে লগ ধরে। GitHub Actions থেকে চালানো হয় (.github/workflows/emulator-test.yml)।
ক্র্যাশ ধরা পড়লে এক্সিট কোড 1 এবং GitHub-এর Error অ্যানোটেশনে স্ট্যাক ট্রেস দেখায়।
"""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PKG = "com.islamshikhi.app"
TABS = ["হোম", "ইবাদত", "কুরআন", "সহকারী", "প্রোফাইল"]
MAX_PER_TAB = 8


def adb(*args, timeout=90):
    return subprocess.run(
        ["adb", *args], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=timeout
    )


def annotate(level, title, message):
    msg = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print(f"::{level} title={title}::{msg}", flush=True)


def save_logcat():
    with open("logcat.txt", "w", encoding="utf-8") as f:
        f.write(adb("logcat", "-d").stdout)


def crash_text():
    out = adb("logcat", "-d", "-b", "crash").stdout
    if PKG in out and ("FATAL EXCEPTION" in out or "AndroidRuntime" in out):
        return out
    return ""


def fail(where):
    save_logcat()
    text = crash_text()
    if not text:
        text = adb("logcat", "-d", "-t", "80").stdout
    annotate("error", f"অ্যাপ ক্র্যাশ ({where})", text[-3500:])
    print(text)
    sys.exit(1)


def check(where):
    if crash_text() or not adb("shell", "pidof", PKG).stdout.strip():
        fail(where)


def dump():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("shell", "cat", "/sdcard/ui.xml").stdout
    try:
        root = ET.fromstring(xml[xml.index("<"):])
    except (ValueError, ET.ParseError):
        return []
    nodes = []
    for n in root.iter("node"):
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds", ""))
        if not m:
            continue
        nodes.append({
            "text": n.get("text") or "",
            "desc": n.get("content-desc") or "",
            "clickable": n.get("clickable") == "true",
            "b": tuple(int(v) for v in m.groups()),
        })
    return nodes


def center(b):
    return (b[0] + b[2]) // 2, (b[1] + b[3]) // 2


def tap_xy(x, y):
    adb("shell", "input", "tap", str(x), str(y))


def tap_text(label, timeout=25, required=True):
    end = time.time() + timeout
    while time.time() < end:
        check(f"'{label}' খোঁজার সময়")
        for n in dump():
            if n["text"] == label or n["desc"] == label:
                tap_xy(*center(n["b"]))
                time.sleep(1.5)
                check(f"'{label}' চাপার পর")
                return True
        time.sleep(1.5)
    if required:
        save_logcat()
        screen = "\n".join(f"{n['text'] or n['desc']}" for n in dump() if n["text"] or n["desc"])
        annotate("error", f"'{label}' পাওয়া যায়নি", "স্ক্রিনে যা ছিল:\n" + screen)
        sys.exit(1)
    return False


def foreground_ok():
    out = adb("shell", "dumpsys", "window").stdout
    focus = [l for l in out.splitlines() if "mCurrentFocus" in l]
    return bool(focus) and PKG in focus[0]


def ensure_foreground():
    if foreground_ok():
        return
    adb("shell", "input", "keyevent", "4")  # কোনো ডায়ালগ থাকলে বন্ধ
    time.sleep(1)
    if not foreground_ok():
        adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
        time.sleep(4)
    check("অ্যাপ সামনে আনার সময়")


def screen_height():
    out = adb("shell", "wm", "size").stdout
    m = re.search(r"(\d+)x(\d+)", out)
    return int(m.group(2)) if m else 1920


def main():
    adb("logcat", "-c")
    adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
    time.sleep(6)
    check("প্রথম চালুর সময়")

    # সেটআপ: স্বাগত > লিঙ্গ > বয়স > ভাষা > পছন্দ
    tap_text("শুরু করি")
    tap_text("পুরুষ")
    tap_text("পরবর্তী")
    tap_text("প্রাপ্তবয়স্ক")
    tap_text("পরবর্তী")
    tap_text("পরবর্তী")
    tap_text("সম্পন্ন")
    print("সেটআপ শেষ, হোম স্ক্রিনের জন্য অপেক্ষা", flush=True)
    time.sleep(5)
    check("সেটআপ শেষে হোম খোলার সময়")
    tap_text("হোম", timeout=30)

    height = screen_height()
    visited = 0
    for tab in TABS:
        ensure_foreground()
        tap_text(tab)
        time.sleep(2)
        check(f"'{tab}' ট্যাব খোলার পর")
        seen = set()
        candidates = []
        for n in dump():
            cy = center(n["b"])[1]
            if not n["clickable"] or cy > height * 0.86 or cy < height * 0.05:
                continue
            if n["b"] in seen:
                continue
            seen.add(n["b"])
            candidates.append(n["b"])
        print(f"{tab}: {len(candidates)}টি ট্যাপযোগ্য জিনিস", flush=True)
        for b in candidates[:MAX_PER_TAB]:
            ensure_foreground()
            tap_text(tab, required=False, timeout=5)
            tap_xy(*center(b))
            time.sleep(2.5)
            check(f"'{tab}' ট্যাবে ({center(b)}) ট্যাপের পর")
            visited += 1
            adb("shell", "input", "keyevent", "4")
            time.sleep(1)
        ensure_foreground()

    save_logcat()
    annotate("notice", "Emulator স্মোক টেস্ট সফল", f"সেটআপ পার হয়েছে, পাঁচ ট্যাব খোলা গেছে, {visited}টি ট্যাপ করা হয়েছে, ক্র্যাশ হয়নি।")
    print("সফল")


if __name__ == "__main__":
    main()
