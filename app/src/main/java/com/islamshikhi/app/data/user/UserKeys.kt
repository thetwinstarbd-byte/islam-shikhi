package com.islamshikhi.app.data.user

import java.time.LocalDate

/** UserDataRepository-তে ব্যবহৃত কী-গুলো এক জায়গায়, যেন বানান ভুলে ডেটা হারিয়ে না যায়। */
object UserKeys {
    /** কুরআনে শেষ পড়ার অবস্থান, "সূরা:আয়াত" আকারে। */
    const val QURAN_LAST = "quran_last"

    /** দিনে কতটি আয়াত পড়ার লক্ষ্য। */
    const val QURAN_GOAL = "quran_goal"
    const val QURAN_GOAL_DEFAULT = 10

    /** নির্দিষ্ট দিনে পড়া হিসেবে চিহ্নিত আয়াতের সেট ("সূরা:আয়াত")। */
    fun quranRead(date: LocalDate) = "quran_read:$date"

    /** মুখস্থ করার পরিকল্পনায় থাকা সূরার আইডি এবং যেগুলো মুখস্থ হয়েছে। */
    const val MEMORIZE_PLAN = "memorize_plan"
    const val MEMORIZED = "memorized"

    /** দৈনিক আয়াত/হাদীসের নোটিফিকেশন: চালু আছে কি না ও সময় (দিনের শুরু থেকে মিনিট)। */
    const val DAILY_VERSE_ON = "daily_verse_on"
    const val DAILY_VERSE_MIN = "daily_verse_min"

    /** কুরআন পড়ার পাতা: আরবি লেখার আকার (sp) ও স্ক্রিন জাগিয়ে রাখা। */
    const val QURAN_ARABIC_SP = "quran_arabic_sp"
    const val QURAN_ARABIC_SP_DEFAULT = 28
    const val KEEP_SCREEN_ON = "keep_screen_on"
    const val SHOW_TRANSLATION = "show_translation"      // আয়াতের বাংলা অনুবাদ দেখানো (ডিফল্ট চালু)
    const val TASBIH_HAPTIC = "tasbih_haptic"            // তাসবিহে কম্পন (ডিফল্ট চালু)
    const val DIGITS_ENGLISH = "digits_english"          // ১২৩ এর বদলে 123
    const val TIME_24H = "time_24h"                      // ২৪ ঘণ্টার সময়

    // বুকমার্ক ও নোটের কী। বিদ্যমান DuaScreen "dua:ID" আকারই ব্যবহার করে।
    fun duaKey(id: String) = "dua:$id"
    fun ayahKey(chapter: Int, ayah: Int) = "ayah:$chapter:$ayah"
    fun masalaKey(id: String) = "masala:$id"
    fun kidKey(id: String) = "kid:$id"
    fun libraryKey(id: String) = "library:$id"
}
