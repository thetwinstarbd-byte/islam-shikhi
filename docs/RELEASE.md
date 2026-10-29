# Signed Release APK বানানো

**ব্যক্তিগত ব্যবহারের জন্য Debug APK-ই যথেষ্ট** (GitHub Actions > Build Debug APK)। Signed Release APK লাগে শুধু তখন, যখন অ্যাপটি অন্যদের দেবেন বা Play Store-এ দেবেন।

> ⚠️ **এই ওয়ার্কফ্লোটি এখনও একবারও চালিয়ে পরীক্ষা করা হয়নি**, কারণ এর জন্য আপনার নিজের keystore লাগে। প্রথমবার ব্যর্থ হলে Error আমাকে পাঠান।

## ১. keystore বানান (একবারই; কম্পিউটারে, Java ইনস্টল থাকলে)

```bash
keytool -genkey -v -keystore islam-shikhi.jks -keyalg RSA -keysize 2048 -validity 10000 -alias islamshikhi
```
প্রশ্ন করলে পাসওয়ার্ড দিন ও মনে রাখুন। **`islam-shikhi.jks` ফাইল ও পাসওয়ার্ড হারালে ভবিষ্যতে একই অ্যাপের আপডেট দেওয়া যাবে না।** ফাইলটি কোথাও নিরাপদে (যেমন পাসওয়ার্ড ম্যানেজার বা আলাদা ড্রাইভ) রাখুন। **এটি GitHub-এ কখনো আপলোড করবেন না** (`.gitignore` এটি আটকায়)।

## ২. Base64-এ বদলান

- Linux/Mac: `base64 -w0 islam-shikhi.jks > keystore.b64` (Mac-এ `base64 -i islam-shikhi.jks -o keystore.b64`)
- Windows (PowerShell): `[Convert]::ToBase64String([IO.File]::ReadAllBytes("islam-shikhi.jks")) | Set-Content keystore.b64`

## ৩. GitHub-এ চারটি Secret রাখুন
রিপোজিটরি > **Settings > Secrets and variables > Actions > New repository secret**

| নাম | মান |
|---|---|
| `KEYSTORE_BASE64` | `keystore.b64` ফাইলের পুরো লেখা |
| `KEYSTORE_PASSWORD` | keystore-এর পাসওয়ার্ড |
| `KEY_ALIAS` | `islamshikhi` (ওপরের `-alias`) |
| `KEY_PASSWORD` | key-এর পাসওয়ার্ড (সাধারণত একই) |

## ৪. চালান
**Actions > Build Signed Release APK > Run workflow**। শেষ হলে Artifacts থেকে `islam-shikhi-release-apk` নামান; ভেতরে `app-release.apk`।

## Android Studio দিয়ে (বিকল্প)
`Build > Generate Signed App Bundle / APK > APK` → keystore বেছে নিন → `release` → Finish।
