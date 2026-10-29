package com.islamshikhi.app.ui.navigation

/**
 * ট্যাবের ভেতরের পাতাগুলোর গন্তব্যের নাম। ট্যাবের নাম আছে [AppTab]-এ।
 * নিচের ট্যাব-বারে কোন ট্যাব নির্বাচিত দেখাবে তা পথের প্রথম অংশ (ibadah/, quran/, assistant/, home/, profile/) দেখে ঠিক হয়।
 */
object Routes {
    // ইবাদত
    const val DUA = "ibadah/dua"
    const val PRAYER = "ibadah/prayer"
    const val QIBLA = "ibadah/qibla"
    const val CALENDAR = "ibadah/calendar"
    const val TASBIH = "ibadah/tasbih"
    const val TRACKER = "ibadah/tracker"

    // কুরআন
    const val QURAN_SEARCH = "quran/search"
    const val QURAN_NOTICE = "quran/notice"
    const val QURAN_MEMORIZE = "quran/memorize"
    const val QURAN_READ = "quran/read/{chapter}?ayah={ayah}"
    fun quranRead(chapter: Int, ayah: Int = 1) = "quran/read/$chapter?ayah=$ayah"

    // হোম
    const val BOOKMARKS = "home/bookmarks"

    // সহকারী
    const val MASAIL = "assistant/masail"
    const val MASALA_DETAIL = "assistant/masala/{id}"
    fun masala(id: String) = "assistant/masala/$id"
    const val ZAKAT = "assistant/zakat"
    const val KIDS = "assistant/kids"
    const val KID_DETAIL = "assistant/kids/{id}"
    fun kid(id: String) = "assistant/kids/$id"
    const val QUIZ = "assistant/quiz"
    const val EMERGENCY = "assistant/emergency"
    const val EMERGENCY_DETAIL = "assistant/emergency/{id}"
    fun emergency(id: String) = "assistant/emergency/$id"
    const val LIBRARY = "assistant/library"
    const val LIBRARY_DETAIL = "assistant/library/{id}"
    fun library(id: String) = "assistant/library/$id"

    // প্রোফাইল
    const val HABITS = "profile/habits"
    const val ABOUT = "profile/about"
}
