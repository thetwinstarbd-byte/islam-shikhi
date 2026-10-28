package com.islamshikhi.app.feature.tracker

import java.time.LocalDate

/** আমল ট্র্যাকারের প্রতিটি দৈনিক আইটেম। */
enum class AmalItem(val id: String, val labelBn: String, val isPrayer: Boolean) {
    FAJR("fajr", "ফজরের নামাজ", true),
    DHUHR("dhuhr", "যোহরের নামাজ", true),
    ASR("asr", "আসরের নামাজ", true),
    MAGHRIB("maghrib", "মাগরিবের নামাজ", true),
    ISHA("isha", "এশার নামাজ", true),
    QURAN("quran", "কুরআন তিলাওয়াত", false),
    MORNING_DHIKR("morning_dhikr", "সকালের যিকির", false),
    EVENING_DHIKR("evening_dhikr", "সন্ধ্যার যিকির", false),
    ISTIGHFAR("istighfar", "ইস্তিগফার", false),
    SADAQAH("sadaqah", "সদকা/দান", false)
}

/** ডেটা রাখা হয় "তারিখ|আইটেম" আকারের একটি সেটে (UserDataRepository-র "amal_done" কী-তে)। */
object AmalKeys {
    const val SET = "amal_done"
    fun key(date: LocalDate, item: AmalItem): String = "$date|${item.id}"

    fun doneCount(done: Set<String>, date: LocalDate): Int =
        AmalItem.entries.count { key(date, it) in done }
}
