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
TAB_IDS = {"হোম": "home", "ইবাদত": "ibadah", "কুরআন": "quran", "সহকারী": "assistant", "প্রোফাইল": "profile"}
# Dhaka-র আকাশের ছয় অবস্থা দেখতে Emulator-এর ঘড়ি (UTC) বদলানো হয়; Dhaka = UTC+6
SKY_TIMES = {"night": "10302000", "dawn": "10302330", "morning": "10310200", "noon": "10310630",
             "afternoon": "10310930", "dusk": "10311200"}  # MMDDhhmm
MAX_PER_TAB = 6


def adb(*args, timeout=90):
    return subprocess.run(
        ["adb", *args], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=timeout
    )


def annotate(level, title, message):
    msg = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print(f"::{level} title={title}::{msg}", flush=True)


SHOTS = "screenshots"


def shot(name):
    """স্ক্রিনের ছবি তোলে (CI পরে এগুলো ci-screenshots ব্রাঞ্চে রাখে, যেন ডিজাইন দেখা যায়)।"""
    import os
    os.makedirs(SHOTS, exist_ok=True)
    r = subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True, timeout=60)
    if r.returncode == 0 and len(r.stdout) > 1000:
        with open(f"{SHOTS}/{name}.png", "wb") as f:
            f.write(r.stdout)


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


def tap_prefix(prefix, timeout=15, required=False, scrolls=0):
    """যে বাটনের লেখা prefix দিয়ে শুরু (কার্ডে শিরোনামের সঙ্গে আরও লেখা থাকে), তাতে ট্যাপ করে।
    scrolls > 0 হলে না পেলে পাতা স্ক্রল করে আবার খোঁজে (লম্বা তালিকার জন্য)।"""
    end = time.time() + timeout
    left = scrolls
    while True:
        check(f"'{prefix}' খোঁজার সময়")
        for n in dump():
            if n["text"].startswith(prefix) or n["desc"].startswith(prefix):
                tap_xy(*center(n["b"]))
                time.sleep(2)
                check(f"'{prefix}' চাপার পর")
                return True
        if left > 0:
            swipe_up()
            left -= 1
            continue
        if time.time() >= end:
            break
        time.sleep(1.5)
    if required:
        screen = "\n".join(f"{n['text'] or n['desc']}" for n in dump() if n["text"] or n["desc"])
        annotate("error", f"'{prefix}...' পাওয়া যায়নি", "স্ক্রিনে যা ছিল:\n" + screen)
        sys.exit(1)
    return False


def content_tour():
    """দোয়ার তালিকা → একটি দোয়ার পাতা, এবং ইসলামি জ্ঞানের তালিকা → দুটি প্রবন্ধ; প্রতিটির ছবি তোলে।"""
    ensure_foreground()
    tap_text("ইবাদত")
    if tap_prefix("দোয়া ও যিকির", required=True):
        shot("50-dua-list")
        swipe_up()
        shot("50-dua-list-scrolled")
        swipe_down()
        if tap_prefix("ঘুমানোর দোয়া", required=True):
            shot("51-dua-detail")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()
    tap_text("সহকারী")
    if tap_prefix("ইসলামি জ্ঞান", required=True):
        shot("52-ilm-list")
        swipe_up(2)
        shot("52-ilm-list-scrolled")
        swipe_down(2)
        for name, key in (("শিরক কাকে বলে", "shirk"), ("বিদআত কাকে বলে", "bidah")):
            swipe_down(8)
            if tap_prefix(name, required=True, scrolls=6):
                shot(f"53-ilm-{key}-top")
                for i in range(1, 4):
                    swipe_up(2)
                    shot(f"53-ilm-{key}-{i}")
                adb("shell", "input", "keyevent", "4")
                time.sleep(1.5)
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()


def net_tour():
    """কাছের মসজিদ, কনটেন্ট আপডেট ও কুরআনের তিলাওয়াত বাটনের পাতা; ছবি তোলে (ইন্টারনেট না থাকলেও ত্রুটির বার্তা দেখায়)।"""
    ensure_foreground()
    tap_text("ইবাদত")
    if tap_prefix("কাছের মসজিদ", required=False, scrolls=3):
        shot("60-mosque")
        if tap_prefix("নামাজের সময়ের স্থান থেকে খুঁজুন", required=False):
            time.sleep(12)
            shot("61-mosque-result")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()
    tap_text("প্রোফাইল", required=False, timeout=8)
    if tap_prefix("কনটেন্ট আপডেট", required=False, scrolls=4):
        shot("62-update")
        if tap_prefix("আপডেট আছে কি না দেখুন", required=False):
            time.sleep(8)
            shot("63-update-result")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()


def custom_tour():
    """নিজের আজান/জামাতের সময় বসানোর পাতার ছবি। না মিললে ফেল নয়, শুধু ছবি বাদ যায়।"""
    ensure_foreground()
    tap_text("ইবাদত", required=False, timeout=8)
    swipe_down(4)
    if tap_prefix("নামাজের সময়", required=False, scrolls=3):
        shot("80-prayer")
        if tap_prefix("আমার নিজের সময়", required=False, scrolls=3):
            shot("80-prayer-custom")
            swipe_up(1)
            shot("80-prayer-custom-2")
            if tap_prefix("আজান", required=False, scrolls=2):
                shot("80-prayer-timepicker")
                adb("shell", "input", "keyevent", "4")
                time.sleep(1)
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)


def english_tour():
    """ইংরেজি ভাষা বেছে মেনু, সাহায্য কেন্দ্র ও 'ভুল জানান'-এর ছবি। না মিললে ফেল নয়, শুধু ছবি বাদ যায়।"""
    try:
        ensure_foreground()
        tap_text("প্রোফাইল", required=False, timeout=8)
        swipe_down(4)
        shot("90-profile-bn")
        if tap_prefix("সাহায্য কেন্দ্র", required=False, scrolls=3):
            shot("90-help-bn")
            if tap_prefix("ভুল জানান", required=False, scrolls=2):
                shot("90-report-bn")
                adb("shell", "input", "keyevent", "4")
                time.sleep(1)
            adb("shell", "input", "keyevent", "4")
            time.sleep(1)
        swipe_up(3)
        if tap_prefix("English", required=False, scrolls=4):
            time.sleep(2)
            swipe_down(4)
            shot("91-profile-en")
            for tab in ["Home", "Worship", "Quran", "Assistant"]:
                if tap_text(tab, required=False, timeout=5):
                    time.sleep(1.5)
                    shot("91-en-" + tab.lower())
            tap_text("Profile", required=False, timeout=5)
            swipe_down(4)
            if tap_prefix("Help center", required=False, scrolls=3):
                shot("91-help-en")
                if tap_prefix("Report a mistake", required=False, scrolls=2):
                    shot("91-report-en")
                    adb("shell", "input", "keyevent", "4")
                    time.sleep(1)
                adb("shell", "input", "keyevent", "4")
                time.sleep(1)
            # পরের ধাপগুলোর জন্য আবার বাংলায় ফেরা
            swipe_up(3)
            tap_prefix("Bangla", required=False, scrolls=4)
            time.sleep(1.5)
    except SystemExit:
        raise
    except Exception as e:  # noqa: BLE001
        print("ইংরেজি ছবি তোলা যায়নি:", e)


def feature_tour():
    """সার্চ, রমযান মোড, কাযা, খতম, হজ্জ গাইড ও ব্যাকআপ পাতার ছবি। কোনোটি না মিললে টেস্ট ফেল হয় না, শুধু ছবি বাদ যায়।"""
    ensure_foreground()
    tap_text("ইবাদত")
    for name, key in (("রমযান মোড", "ramadan"), ("কাযা নামাজ ও রোযা", "qada"), ("হজ্জ-উমরা গাইড", "hajj")):
        swipe_down(4)
        if tap_prefix(name, required=False, scrolls=6):
            shot(f"70-{key}")
            if key == "ramadan":
                swipe_up(2)
                shot("70-ramadan-scrolled")
            if key == "hajj":
                swipe_up(2)
                shot("70-hajj-scrolled")
            adb("shell", "input", "keyevent", "4")
            time.sleep(1)
    ensure_foreground()
    tap_text("কুরআন", required=False, timeout=8)
    if tap_prefix("কুরআন খতম ট্র্যাকার", required=False, scrolls=3):
        shot("71-khatm")
        if tap_prefix("খতম শুরু করুন", required=False):
            time.sleep(1)
            shot("71-khatm-started")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()
    tap_text("হোম", required=False, timeout=8)
    swipe_up(2)
    if tap_prefix("খুঁজুন", required=False, scrolls=3):
        shot("72-search-empty")
        if tap_prefix("দোয়া, মাসআলা", required=False):
            adb("shell", "input", "text", "Fati")
            time.sleep(3)
            shot("72-search-result")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()
    tap_text("সহকারী", required=False, timeout=8)
    if tap_prefix("ইসলামি জ্ঞান", required=False):
        swipe_down(4)
        if tap_prefix("হজ্জ ও উমরা: ধাপে ধাপে", required=False, scrolls=12):
            shot("73-hajj-article-top")
            for i in range(1, 4):
                swipe_up(2)
                shot(f"73-hajj-article-{i}")
            adb("shell", "input", "keyevent", "4")
            time.sleep(1)
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()
    tap_text("প্রোফাইল", required=False, timeout=8)
    if tap_prefix("ব্যাকআপ ও রিস্টোর", required=False, scrolls=4):
        shot("74-backup")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    ensure_foreground()


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


def screen_size():
    out = adb("shell", "wm", "size").stdout
    m = re.search(r"(\d+)x(\d+)", out)
    return (int(m.group(1)), int(m.group(2))) if m else (1080, 1920)


def screen_height():
    return screen_size()[1]


def swipe_up(times=1):
    """পাতা নিচের দিকে স্ক্রল করে (আঙুল ওপরে টানা)।"""
    w, h = screen_size()
    for _ in range(times):
        adb("shell", "input", "swipe", str(w // 2), str(int(h * 0.78)), str(w // 2), str(int(h * 0.22)), "350")
        time.sleep(0.8)


def swipe_down(times=1):
    w, h = screen_size()
    for _ in range(times):
        adb("shell", "input", "swipe", str(w // 2), str(int(h * 0.22)), str(w // 2), str(int(h * 0.78)), "350")
        time.sleep(0.8)


def restart_app():
    adb("shell", "am", "force-stop", PKG)
    time.sleep(1)
    adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
    time.sleep(5)
    check("অ্যাপ আবার চালুর সময়")


def extra_screenshots():
    """ডার্ক মোড ও আকাশের ছয় অবস্থার ছবি। কোনোটি না হলে টেস্ট ফেল হয় না, শুধু ছবি বাদ যায়।"""
    try:
        adb("shell", "cmd", "uimode", "night", "yes")
        restart_app()
        for tab in TABS:
            tap_text(tab, required=False, timeout=8)
            time.sleep(1.5)
            shot(f"30-dark-{TAB_IDS[tab]}")
        adb("shell", "cmd", "uimode", "night", "no")
    except SystemExit:
        raise
    except Exception as e:  # noqa: BLE001
        print("ডার্ক মোডের ছবি তোলা যায়নি:", e)

    try:
        adb("root")
        time.sleep(2)
        adb("shell", "settings", "put", "global", "auto_time", "0")
        adb("shell", "settings", "put", "global", "auto_time_zone", "0")
        adb("shell", "setprop", "persist.sys.timezone", "UTC")
        for name, stamp in SKY_TIMES.items():
            adb("shell", "date", f"{stamp}2026.00")
            restart_app()
            tap_text("হোম", required=False, timeout=8)
            time.sleep(2)
            shot(f"40-sky-{name}")
    except SystemExit:
        raise
    except Exception as e:  # noqa: BLE001
        print("আকাশের ছবি তোলা যায়নি:", e)


def main():
    adb("logcat", "-c")
    adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
    time.sleep(6)
    check("প্রথম চালুর সময়")
    shot("01-welcome")

    # সেটআপ: স্বাগত > লিঙ্গ > বয়স > ভাষা > পছন্দ
    tap_text("শুরু করি")
    shot("02-gender")
    tap_text("পুরুষ")
    tap_text("পরবর্তী")
    shot("03-age")
    tap_text("প্রাপ্তবয়স্ক")
    tap_text("পরবর্তী")
    shot("04-language")
    tap_text("পরবর্তী")
    shot("05-preferences")
    tap_text("সম্পন্ন")
    print("সেটআপ শেষ, হোম স্ক্রিনের জন্য অপেক্ষা", flush=True)
    time.sleep(5)
    check("সেটআপ শেষে হোম খোলার সময়")
    if tap_text("ব্যবস্থা করুন", timeout=6, required=False):
        shot("06-adhan-setup")
        swipe_up(1)
        shot("06-adhan-setup-2")
        adb("shell", "input", "keyevent", "4")
        time.sleep(1)
    tap_text("পরে", timeout=3, required=False)
    tap_text("হোম", timeout=30)
    time.sleep(2)
    shot("06-home")
    swipe_up()
    shot("07-home-scrolled-1")
    swipe_up()
    shot("08-home-scrolled-2")
    swipe_up(2)
    shot("09-home-scrolled-3")
    swipe_down(6)

    height = screen_height()
    visited = 0
    for tab in TABS:
        ensure_foreground()
        tap_text(tab)
        time.sleep(2)
        check(f"'{tab}' ট্যাব খোলার পর")
        shot(f"10-tab-{TAB_IDS[tab]}")
        if tab != "হোম":
            swipe_up()
            shot(f"11-tab-{TAB_IDS[tab]}-scrolled")
            swipe_down(3)
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
        for b in candidates[: (12 if tab == "হোম" else MAX_PER_TAB)]:
            ensure_foreground()
            tap_text(tab, required=False, timeout=5)
            tap_xy(*center(b))
            time.sleep(2.5)
            check(f"'{tab}' ট্যাবে ({center(b)}) ট্যাপের পর")
            visited += 1
            shot(f"20-{TAB_IDS[tab]}-sub{visited:02d}")
            adb("shell", "input", "keyevent", "4")
            time.sleep(1)
        ensure_foreground()

    content_tour()
    net_tour()
    feature_tour()
    custom_tour()
    english_tour()

    extra_screenshots()

    save_logcat()
    annotate("notice", "Emulator স্মোক টেস্ট সফল", f"সেটআপ পার হয়েছে, পাঁচ ট্যাব খোলা গেছে, {visited}টি ট্যাপ করা হয়েছে, ক্র্যাশ হয়নি।")
    print("সফল")


if __name__ == "__main__":
    main()
