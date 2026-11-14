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
