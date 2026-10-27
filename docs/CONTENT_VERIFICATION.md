# কনটেন্ট যাচাই নীতিমালা

ইসলামিক কনটেন্টের নির্ভুলতা ডিজাইনের চেয়ে গুরুত্বপূর্ণ।

## এন্ট্রির কাঠামো (JSON)

```json
{
  "id": "unique-id",
  "category": "category-key",
  "titleBn": "...",
  "arabic": "...",
  "transliterationBn": "...",
  "meaningBn": "...",
  "sourceRef": "গ্রন্থের নাম, খণ্ড/নম্বর",
  "hadithGrade": null,
  "viewpoints": [],
  "audience": ["ALL"],
  "verificationStatus": "DRAFT",
  "reviewedBy": null,
  "reviewedOn": null
}
```

## নিয়ম

1. `sourceRef` ফাঁকা থাকলে এন্ট্রি অ্যাপে দেখানো হবে না।
2. `verificationStatus` শুধু `DRAFT`, `SOURCE_CHECKED` বা `SCHOLAR_REVIEWED` হতে পারে।
3. মতভেদ থাকলে `viewpoints`-এ প্রতিটি মত ও তার সূত্র আলাদাভাবে থাকবে।
4. জটিল তালাক, উত্তরাধিকার, আর্থিক লেনদেন ও ব্যক্তিগত মাসআলায় অ্যাপ যোগ্য আলেমের পরামর্শ নিতে বলবে।
5. কপিরাইটযুক্ত অনুবাদ বা অডিও অনুমতি ছাড়া যুক্ত হবে না। প্রতিটি অনুবাদের লাইসেন্স `docs/LICENSES.md`-এ লেখা থাকবে।
6. AI-সহায়তায় লেখা ড্রাফট সবসময় `DRAFT` থেকে শুরু হবে।
