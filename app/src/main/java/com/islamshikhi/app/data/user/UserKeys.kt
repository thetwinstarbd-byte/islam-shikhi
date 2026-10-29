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

    // বুকমার্ক ও নোটের কী। বিদ্যমান DuaScreen "dua:ID" আকারই ব্যবহার করে।
    fun duaKey(id: String) = "dua:$id"
    fun ayahKey(chapter: Int, ayah: Int) = "ayah:$chapter:$ayah"
    fun masalaKey(id: String) = "masala:$id"
    fun kidKey(id: String) = "kid:$id"
    fun libraryKey(id: String) = "library:$id"
}
