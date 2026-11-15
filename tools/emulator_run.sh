#!/usr/bin/env bash
# Emulator চালু হওয়ার পর এই স্ক্রিপ্ট চলে: APK ইনস্টল করে ড্রাইভার স্ক্রিপ্ট চালায়।
set -u
adb install -r app/build/outputs/apk/debug/app-debug.apk || { echo "::error::APK ইনস্টল ব্যর্থ"; exit 1; }
# অনুমতির ডায়ালগ যেন পরীক্ষা আটকে না দেয়: নোটিফিকেশন ও অ্যালার্মের অনুমতি আগেই দেওয়া হয়
adb shell pm grant com.islamshikhi.app android.permission.POST_NOTIFICATIONS || true
adb shell appops set com.islamshikhi.app SCHEDULE_EXACT_ALARM allow || true
python3 tools/emulator_drive.py
