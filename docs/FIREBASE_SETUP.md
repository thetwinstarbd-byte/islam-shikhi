# Firebase চালু করার ধাপ (আপনার করণীয়)

অ্যাপে ক্লাউড-কোড তৈরি আছে। শুধু `app/google-services.json` ফাইলটি বসালেই চালু হয়; না থাকলে অ্যাপ আগের মতো চলে ও ক্লাউড পাতায় "শীঘ্রই আসছে" দেখায়।

1. https://console.firebase.google.com এ গিয়ে নতুন প্রজেক্ট বানান (Analytics বন্ধ রাখতে পারেন)।
2. **Add app > Android**: Package name `com.islamshikhi.app`। 
3. **SHA-1 দিন (খুব জরুরি):** Project settings > আপনার Android app > Add fingerprint।
   - GitHub Actions-এর debug APK প্রতিবার আলাদা debug key দিয়ে সই হতে পারে, তাই Google সাইন-ইন সেই APK-তে না-ও চলতে পারে। স্থায়ী একটি debug keystore বানিয়ে CI-তে ব্যবহার করুন, অথবা release keystore-এর SHA-1 দিন (`docs/RELEASE.md` দেখুন)।
   - Play Store-এ দিলে Play Console > App signing-এর SHA-1ও যোগ করতে হবে।
4. **Authentication > Sign-in method > Google** চালু করুন।
5. **Firestore Database** তৈরি করুন (production mode), তারপর **Rules** ট্যাবে `firebase/firestore.rules` ফাইলের লেখা বসিয়ে Publish করুন। এতে প্রতিজন শুধু নিজের তথ্য পড়তে-লিখতে পারে।
6. Project settings থেকে `google-services.json` নামিয়ে `app/` ফোল্ডারে রাখুন, তারপর GitHub-এ commit করুন। (এই ফাইলে গোপন পাসওয়ার্ড থাকে না, তবে API key-এ package/SHA-1 বিধিনিষেধ দিয়ে রাখুন।)
7. Play Store-এর "Data safety" ফর্মে লিখুন: ঐচ্ছিক সাইন-ইন; নাম, ইমেইল ও ব্যবহারকারীর তৈরি কনটেন্ট (নোট, বুকমার্ক) জমা হয়; মুছে ফেলার উপায় অ্যাপে আছে।
8. Firebase Console > Usage থেকে খরচ নজরে রাখুন; বিনামূল্যের সীমা (Spark) ছোট অ্যাপের জন্য যথেষ্ট।

## Facebook সাইন-ইন (পরে চাইলে)
Facebook for Developers-এ অ্যাপ বানাতে হবে, Android key hash দিতে হবে, এবং Facebook-এর অ্যাপ review ও (ক্ষেত্রবিশেষে) business verification লাগে। Firebase Authentication-এ Facebook provider চালু করে App ID/Secret বসাতে হয়। বলবেন, তখন কোড যোগ করব।

## বড় ফাইল (অডিও, বাড়তি কনটেন্ট) সার্ভারে রাখা
- Firebase Storage-এ রেখে ব্যবহারকারী চাইলে নামানো যাবে; অ্যাপ হালকা থাকবে। তবে অডিওর লাইসেন্স আগে নিশ্চিত করতে হবে।
- এখনকার "কনটেন্ট আপডেট" ব্যবস্থা (`updates/` ফোল্ডার ও sha256 যাচাই) Storage বা নিজের সার্ভারেও চলবে; শুধু ঠিকানা বদলাতে হবে।
