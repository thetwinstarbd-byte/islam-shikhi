# ইসলাম শিখি — আর্কিটেকচার ও পরিকল্পনা (পর্যায় ১)

## ১. প্রজেক্টের মূল সিদ্ধান্ত

| বিষয় | সিদ্ধান্ত |
|---|---|
| অ্যাপের নাম | ইসলাম শিখি |
| প্যাকেজ নেম | `com.islamshikhi.app` |
| ভাষা ও UI | Kotlin, Jetpack Compose, Material 3 |
| ন্যূনতম Android | API 26 (Android 8.0), টার্গেট/কম্পাইল API 35 |
| আর্কিটেকচার | MVVM + Repository, এক মডিউল (`app`), ফিচার অনুযায়ী প্যাকেজ |
| স্থানীয় সেটিংস | Jetpack DataStore (Preferences) |
| স্থানীয় ডেটাবেস | Room (দোয়া, মাসআলা, বুকমার্ক, নোট, ট্র্যাকার, কুরআন) |
| নির্ধারিত কাজ | WorkManager + AlarmManager (নামাজের নোটিফিকেশন) |
| নেভিগেশন | Navigation Compose |
| ডিপেন্ডেন্সি ইনজেকশন | প্রথমে হাতে-লেখা সহজ `AppContainer` (Hilt নয়, যাতে শিক্ষানবিশের জন্য সহজ থাকে) |
| নেটওয়ার্ক/অ্যাকাউন্ট/বিজ্ঞাপন | প্রথম সংস্করণে নেই |

## ২. ফিচার ভাগ

### MVP (প্রথম ব্যবহারযোগ্য সংস্করণ)
- Welcome ও প্রথমবার সেটআপ: লিঙ্গ, বয়সের শ্রেণি, ভাষা, নোটিফিকেশন পছন্দ
- পাঁচ ট্যাবের নেভিগেশন ও হোম স্ক্রিন
- দোয়া ও যিকির (আরবি, উচ্চারণ, অর্থ, সূত্র), তাসবিহ কাউন্টার, বুকমার্ক
- নামাজের সময়, কাউন্টডাউন, লোকাল নোটিফিকেশন
- কিবলা কম্পাস, হিজরি তারিখ
- কুরআন: ১১৪ সূরা, আরবি, বাংলা অনুবাদ (লাইসেন্স-যাচাই সাপেক্ষে), অনুসন্ধান, পড়ার অগ্রগতি
- ডার্ক/লাইট থিম, ফন্ট সাইজ

### দ্বিতীয় পর্যায়
- যাচাইকৃত মাসআলা ডেটাবেস, নারী-পুরুষভিত্তিক কনটেন্ট
- যাকাত ক্যালকুলেটর
- আমল ট্র্যাকার, কাস্টম রিমাইন্ডার, সাপ্তাহিক রিপোর্ট
- শিশুদের শিক্ষা, গল্প ও কুইজ
- জরুরি গাইড

### ভবিষ্যৎ
- তাজবিদ পাঠ, কুরআন অডিও (অনুমোদিত উৎস)
- ইসলামিক বই ও নিবন্ধের লাইব্রেরি
- ব্যাকআপ ও রিস্টোর
- পরিবারের পৃথক প্রোফাইল
- ঐচ্ছিক AI সংযোগ (যাচাইকৃত ফতোয়া হিসেবে দেখানো হবে না)

## ৩. ফোল্ডার স্ট্রাকচার

```
islam-shikhi/
├── README.md
├── .gitignore
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── docs/
│   ├── ARCHITECTURE.md
│   └── CONTENT_VERIFICATION.md
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/ (values/strings.xml, values-bn/, xml/ ইত্যাদি)
        ├── assets/ (পরে: dua.json, masail.json, quran ডেটা)
        └── java/com/islamshikhi/app/
            ├── IslamShikhiApp.kt        // Application ক্লাস
            ├── MainActivity.kt
            ├── AppContainer.kt          // সহজ DI
            ├── data/
            │   ├── profile/             // UserProfile, ProfileRepository (DataStore)
            │   ├── local/               // Room (পরের পর্যায়ে)
            │   └── content/             // JSON থেকে কনটেন্ট লোড (পরের পর্যায়ে)
            ├── domain/model/            // Gender, AgeGroup, AppLanguage ইত্যাদি
            ├── ui/
            │   ├── theme/               // Color, Type, Theme
            │   ├── navigation/          // NavGraph, বটম নেভিগেশন
            │   ├── onboarding/          // Welcome ও সেটআপ
            │   ├── home/
            │   ├── ibadah/
            │   ├── quran/
            │   ├── assistant/
            │   └── profile/
            └── feature/                 // পরের পর্যায়ে: prayer, dua, zakat ইত্যাদির লজিক
```

## ৪. লাইব্রেরি ও বিনামূল্যের ডেটা উৎসের পরিকল্পনা

| প্রয়োজন | পরিকল্পনা | অবস্থা |
|---|---|---|
| UI | Compose BOM, Material 3, Navigation Compose | ব্যবহৃত |
| সেটিংস ও ব্যবহারকারীর তথ্য | DataStore Preferences | ব্যবহৃত |
| কনটেন্ট (দোয়া, মাসআলা, কুরআন ইত্যাদি) | অ্যাপের ভেতরে JSON/টেক্সট ফাইল (`assets`) | ব্যবহৃত; Room লাগেনি, তাই যোগ করা হয়নি |
| নামাজের সময় গণনা | adhan2 0.0.5 (MIT) | ব্যবহৃত |
| রিমাইন্ডার | AlarmManager (`ReminderScheduler`) | ব্যবহৃত; ফোনে পরীক্ষা বাকি |
| কিবলা | ডিভাইস সেন্সর + কাবার দিক গণনা | ব্যবহৃত; সেন্সরের সীমাবদ্ধতা অ্যাপে জানানো আছে |
| হিজরি তারিখ | `java.time.chrono.HijrahDate` | ব্যবহৃত; চাঁদ দেখার পার্থক্যের নোটসহ |
| কুরআনের আরবি | Tanzil (উসমানী) | ব্যবহৃত, লাইসেন্স `docs/LICENSES.md`-এ |
| বাংলা অনুবাদ | আবু বকর যাকারিয়া, QuranEnc.com | ব্যবহৃত, লেখা অপরিবর্তিত, শর্ত অ্যাপে দেখানো হয় |
| দোয়া, মাসআলা | নিজেদের সংকলিত JSON, প্রতিটির সূত্রসহ | ব্যবহৃত; `tools/check_content.py` সূত্র নিয়ম যাচাই করে, আলেমের পর্যালোচনা বাকি |

## ৫. ইসলামিক কনটেন্ট যাচাইয়ের পদ্ধতি

বিস্তারিত: `docs/CONTENT_VERIFICATION.md`। সারসংক্ষেপ:
1. প্রতিটি কনটেন্ট এন্ট্রিতে `sourceRef` (কুরআনের সূরা:আয়াত বা হাদিস গ্রন্থ, নম্বর) বাধ্যতামূলক।
2. `verificationStatus`: `DRAFT` → `SOURCE_CHECKED` → `SCHOLAR_REVIEWED`। শুধু শেষ দুই অবস্থার কনটেন্ট ব্যবহারকারীকে দেখানো হবে (ডেভেলপমেন্টে DRAFT দেখা যাবে)।
3. মতভেদের বিষয়ে `viewpoints` তালিকা থাকবে, একক মত নয়।
4. AI বা আমি যে ড্রাফট লিখব, তা কখনোই নিজে থেকে `SCHOLAR_REVIEWED` হবে না। যোগ্য আলেম বা নির্ভরযোগ্য মূল গ্রন্থ থেকে মিলিয়ে দেখার পরই স্ট্যাটাস বদলাবে।
5. আয়াত, হাদিস বা ফতোয়া বানিয়ে লেখা হবে না। সূত্র না পেলে এন্ট্রি যুক্ত হবে না।

## ৬. প্রোফাইল ও সেটিংসের ডেটা মডেল (DataStore)

```kotlin
data class UserProfile(
    val onboardingCompleted: Boolean = false,
    val gender: Gender? = null,          // MALE | FEMALE, প্রথম সেটআপে আবশ্যক
    val ageGroup: AgeGroup? = null,      // CHILD_TEEN | ADULT | SENIOR
    val language: AppLanguage = AppLanguage.BANGLA,  // BANGLA | ENGLISH
    val notificationsEnabled: Boolean = true,
    val interests: Set<LearningInterest> = emptySet(),
    val learningGoal: LearningGoal? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f
)
```

- লিঙ্গ শুধু কনটেন্ট সাজানোর জন্য, পরিচয় যাচাই নয়। Settings থেকে পরে বদলানো যাবে।
- সব ডেটা শুধু ডিভাইসে থাকবে। কোনো সার্ভারে যাবে না।
- "সব ডেটা মুছুন" বাটনে DataStore ও Room দুটোই খালি হবে।

## ৭. সীমাবদ্ধতা (খোলাখুলি)

- এই পরিবেশে Android SDK দিয়ে বিল্ড করে দেখা যাবে কি না তা নিশ্চিত নয়। যে কোড লেখা হবে তা আপনার Android Studio-তে প্রথম বিল্ডে যাচাই হবে, এবং সেখানকার Error আমাকে পাঠালে আমি ঠিক করে দেব।
- নামাজের সময়, কিবলা ও কুরআনের অনুবাদ এমন বিষয়, যেখানে ভুল হলে ক্ষতি হয়। এগুলো ব্যবহারের আগে একাধিক নির্ভরযোগ্য উৎসের সঙ্গে মিলিয়ে দেখা হবে।
