# Firebase চালু করার ধাপ (আপনার করণীয়)

অ্যাপে ক্লাউড-কোড তৈরি আছে। শুধু `app/google-services.json` ফাইলটি বসালেই চালু হয়; না থাকলে অ্যাপ আগের মতো চলে ও ক্লাউড পাতায় "শীঘ্রই আসছে" দেখায়।

সবচেয়ে সহজ পথ (কোডে হাত দিতে হবে না):

1. https://console.firebase.google.com খুলুন (জিমেইল দিয়ে) → **Create a project** → নাম দিন (যেমন islam-shikhi)। Google Analytics বন্ধ রাখতে পারেন।
2. প্রজেক্টের হোমে **Android আইকন (</>নয়, রোবট)** চাপুন → **Android package name:** `com.islamshikhi.app` → **Debug signing certificate SHA-1:** `7B:F4:5F:8B:5F:46:25:16:52:D7:E9:C7:B6:9A:CF:1A:ED:3B:56:99` (এটি অ্যাপের স্থায়ী ডিবাগ কী; এটি না দিলে Google সাইন-ইন চলবে না) → **Register app**।
3. **Download google-services.json** চাপুন। এই ফাইলটি কোথাও আপলোড করতে হবে না। পরের ধাপগুলো চালিয়ে যান (Next → Continue to console)।
4. বাঁ মেনুতে **Build → Authentication → Get started → Sign-in method → Google → Enable** → আপনার ইমেইল দিন → **Save**।
5. **Build → Firestore Database → Create database** → Location বেছে নিন (যেমন asia-south1) → **Start in production mode** → **Rules** ট্যাবে `firebase/firestore.rules` ফাইলের লেখা বসিয়ে **Publish**।
6. ডাউনলোড করা `google-services.json` ফাইলটি Notepad দিয়ে খুলে **পুরো লেখা কপি** করুন।
7. GitHub রিপোজিটরি → **Settings → Secrets and variables → Actions → New repository secret** → Name: `GOOGLE_SERVICES_JSON` → Secret-এ পুরো লেখা পেস্ট → **Add secret**।
8. **Actions → Build Debug APK → Run workflow**। বিল্ডের লগে "google-services.json ঠিক আছে" আসবে। নতুন APK ইনস্টল করলে অ্যাকাউন্ট ও ক্লাউড পাতায় Google সাইন-ইন চালু থাকবে।
9. Play Store-এ দেওয়ার সময় release keystore-এর SHA-1ও Firebase-এ যোগ করতে হবে (Project settings → আপনার app → Add fingerprint); Play Console > App signing-এরটিও। দেখুন `docs/RELEASE.md`।
10. Play Store-এর "Data safety" ফর্মে লিখুন: ঐচ্ছিক সাইন-ইন; নাম, ইমেইল ও ব্যবহারকারীর তৈরি কনটেন্ট (নোট, বুকমার্ক) জমা হয়; মুছে ফেলার উপায় অ্যাপে আছে।
11. Firebase Console > Usage থেকে খরচ নজরে রাখুন; বিনামূল্যের সীমা (Spark) ছোট অ্যাপের জন্য যথেষ্ট।

(`google-services.json` ফাইল রিপোজিটরিতে commit করবেন না; এটি `.gitignore`-এ বাদ দেওয়া। CI secret থেকে ফাইলটি বানিয়ে নেয়।)

## Facebook সাইন-ইন (পরে চাইলে)
Facebook for Developers-এ অ্যাপ বানাতে হবে, Android key hash দিতে হবে, এবং Facebook-এর অ্যাপ review ও (ক্ষেত্রবিশেষে) business verification লাগে। Firebase Authentication-এ Facebook provider চালু করে App ID/Secret বসাতে হয়। বলবেন, তখন কোড যোগ করব।

## বড় ফাইল (অডিও, বাড়তি কনটেন্ট) সার্ভারে রাখা
- Firebase Storage-এ রেখে ব্যবহারকারী চাইলে নামানো যাবে; অ্যাপ হালকা থাকবে। তবে অডিওর লাইসেন্স আগে নিশ্চিত করতে হবে।
- এখনকার "কনটেন্ট আপডেট" ব্যবস্থা (`updates/` ফোল্ডার ও sha256 যাচাই) Storage বা নিজের সার্ভারেও চলবে; শুধু ঠিকানা বদলাতে হবে।

## ঘোষণা ও নোটিফিকেশন পাঠানো (Remote Config ও Cloud Messaging)
এগুলো সব ব্যবহারকারীর জন্য সবসময় চালু (অ্যাপের ভেতরে বন্ধ করার সুইচ নেই)।

**হোমে ঘোষণা দেখানো (Remote Config):**
1. Firebase > **Remote Config** (Product categories > DevOps & Engagement বা Search for products) > **Create configuration**।
2. প্যারামিটার যোগ করুন (Data type: String):
   - `announcement_id` = `a1` (যেকোনো ছোট লেখা; ঘোষণা বদলালে এটিও বদলান, তাহলে আগে "বন্ধ করা" ঘোষণা আবার দেখাবে)
   - `announcement_bn` = ঘোষণার লেখা (খালি রাখলে কিছু দেখাবে না)
   - `latest_version_code` (Number) = নতুন সংস্করণের versionCode (এখন ২); অ্যাপের নম্বরের চেয়ে বড় হলে আপডেটের খবর দেখায়
   - `update_message_bn` = "নতুন সংস্করণ এসেছে…"
3. **Publish changes**। ব্যবহারকারীর ফোনে সর্বোচ্চ ৬ ঘণ্টার মধ্যে পৌঁছায়।

**নোটিফিকেশন পাঠানো (Cloud Messaging):**
1. Firebase > **Messaging** (Engagement) > **Create your first campaign** > **Firebase Notification messages**।
2. শিরোনাম ও লেখা দিন > **Target: Topic** > `announcements` > পাঠান।

**দূর-নিয়ন্ত্রিত সমন্বয় (Remote Config-এ Number প্যারামিটার):**
- `hijri_offset_days` — হিজরি তারিখ সরানো। চাঁদ দেখা অনুযায়ী দেশে তারিখ ১ দিন পিছিয়ে থাকলে `-1`। সীমা -2..+2। হোমের তারিখ, ক্যালেন্ডার ও রোযার হিসাবে প্রয়োগ হয়।
- `prayer_adjust_fajr_min`, `prayer_adjust_dhuhr_min`, `prayer_adjust_asr_min`, `prayer_adjust_maghrib_min`, `prayer_adjust_isha_min` — ওই ওয়াক্তের সময়ে মিনিট যোগ (+) বা বিয়োগ (-)। সীমা -15..+15। সবার স্ক্রিন ও আজান/রিমাইন্ডারে প্রয়োগ হয়।
- মান না বসালে বা শূন্য রাখলে আগের মতোই চলে। বদল ফোনে পৌঁছাতে সর্বোচ্চ ৬ ঘণ্টা।
- সতর্কতা: ভুল মান বসালে সবার ওয়াক্ত ভুল দেখাবে। বসানোর আগে নিশ্চিত হোন, প্রথমে ছোট মানে পরীক্ষা করুন। নামাজের সময় বদলানোর আগে ইসলামিক ফাউন্ডেশনের তালিকার সাথে মিলিয়ে নিন।
