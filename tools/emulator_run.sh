#!/usr/bin/env bash
# Emulator চালু হওয়ার পর এই স্ক্রিপ্ট চলে: APK ইনস্টল করে ড্রাইভার স্ক্রিপ্ট চালায়।
set -u
adb install -r app/build/outputs/apk/debug/app-debug.apk || { echo "::error::APK ইনস্টল ব্যর্থ"; exit 1; }
python3 tools/emulator_drive.py
