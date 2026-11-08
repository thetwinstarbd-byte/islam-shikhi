package com.islamshikhi.app.feature.adhan

import com.islamshikhi.app.R

/** আজান-সংক্রান্ত সেটিংস ও কোন ওয়াক্তে কোন অডিও বাজবে তার হিসাব। */
object AdhanPrefs {
    /** আজান বাজবে কি না (ডিফল্ট: হ্যাঁ)। বন্ধ থাকলে শুধু নোটিফিকেশন আসে। */
    const val KEY_ON = "adhan_sound_on"
    /** প্রথমবার "আজান ঠিকমতো বাজানোর ব্যবস্থা" জানানো হয়েছে কি না। */
    const val KEY_PROMPTED = "adhan_setup_prompted"
    /** অটো-স্টার্ট (Xiaomi, Oppo ইত্যাদি) ব্যবহারকারী নিজে চালু করেছেন বলে জানিয়েছেন কি না। */
    const val KEY_AUTOSTART_DONE = "adhan_autostart_done"

    /** ফজরে আলাদা আজান ("আস-সালাতু খাইরুম মিনান নাউম" সহ), বাকি ওয়াক্তে সাধারণ আজান। */
    fun rawFor(prayerId: String): Int = if (prayerId == "fajr") R.raw.adhan_fajr else R.raw.adhan_normal
}
