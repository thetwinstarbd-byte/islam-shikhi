package com.islamshikhi.app.feature.tracker

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/** কাযা নামাজ ও রোযার হিসাবের আইটেম। প্রতিটির বাকি সংখ্যা UserDataRepository-তে "qada_<id>" কী-তে থাকে। */
enum class QadaItem(val id: String, val labelBn: String) {
    FAJR("fajr", "ফজর"),
    DHUHR("dhuhr", "যোহর"),
    ASR("asr", "আসর"),
    MAGHRIB("maghrib", "মাগরিব"),
    ISHA("isha", "এশা"),
    WITR("witr", "বিতর"),
    ROZA("roza", "রোযা");

    val key: String get() = "qada_$id"
}

/** কুরআন খতমের হিসাব: কত আয়াত, কত দিন। */
object KhatmMath {
    const val KEY_DAYS = "khatm_days"
    const val KEY_START = "khatm_start"
    const val KEY_READ = "khatm_read"
    const val DEFAULT_DAYS = 30

    /** দৈনিক লক্ষ্য (উপরের দিকে পূর্ণসংখ্যা)। */
    fun dailyTarget(totalAyahs: Int, days: Int): Int {
        if (days <= 0 || totalAyahs <= 0) return 0
        return (totalAyahs + days - 1) / days
    }

    /** শুরুর দিনকে ১ম দিন ধরে বাকি দিন (০-এর নিচে নামে না)। */
    fun daysLeft(start: LocalDate, days: Int, today: LocalDate): Int {
        val elapsed = ChronoUnit.DAYS.between(start, today).toInt().coerceAtLeast(0)
        return (days - elapsed).coerceAtLeast(0)
    }

    /** আজ থেকে বাকি দিনে রোজ কত আয়াত পড়তে হবে। সময় শেষ হলে বাকি সবটুকুই। */
    fun requiredPerDay(remaining: Int, daysLeft: Int): Int {
        if (remaining <= 0) return 0
        if (daysLeft <= 0) return remaining
        return (remaining + daysLeft - 1) / daysLeft
    }
}

/** রমযান-সংক্রান্ত হিসাব (উম্মুল কুরা পঞ্জিকা অনুযায়ী; চাঁদ দেখার সঙ্গে ১ দিন ফারাক হতে পারে)। */
object RamadanMath {
    data class Info(val start: LocalDate, val inRamadan: Boolean, val dayNumber: Int, val daysUntil: Int)

    private fun hijriMonth(d: LocalDate): Int = HijrahDate.from(d).get(ChronoField.MONTH_OF_YEAR)
    private fun hijriDay(d: LocalDate): Int = HijrahDate.from(d).get(ChronoField.DAY_OF_MONTH)

    fun info(today: LocalDate): Info {
        if (hijriMonth(today) == 9) {
            val day = hijriDay(today)
            return Info(today.minusDays((day - 1).toLong()), true, day, 0)
        }
        var d = today
        repeat(400) {
            d = d.plusDays(1)
            if (hijriMonth(d) == 9 && hijriDay(d) == 1) {
                return Info(d, false, 0, ChronoUnit.DAYS.between(today, d).toInt())
            }
        }
        return Info(today, false, 0, -1)
    }

    /** রমযানের দৈনিক আমল। তারিখ|আইডি আকারে "ramadan_done" সেটে থাকে। */
    enum class Deed(val id: String, val labelBn: String) {
        ROZA("roza", "রোযা রাখা"),
        TARAWIH("tarawih", "তারাবীহ"),
        QURAN("quran", "কুরআন তিলাওয়াত"),
        SADAQAH("sadaqah", "সদকা/দান"),
        DUA_IFTAR("dua_iftar", "ইফতারের দোয়া"),
        TAHAJJUD("tahajjud", "তাহাজ্জুদ/কিয়ামুল লাইল")
    }

    const val DONE_SET = "ramadan_done"
    fun key(date: LocalDate, deed: Deed) = "$date|${deed.id}"
    fun count(done: Set<String>, start: LocalDate, deed: Deed): Int =
        (0 until 30).count { key(start.plusDays(it.toLong()), deed) in done }

    /** সেহরির শেষ = ফজরের শুরু; ইফতার = মাগরিব। এখনকার সময় অনুযায়ী পরবর্তী ঘটনা। */
    enum class Event { SEHRI_END, IFTAR }
    data class Next(val event: Event, val atMillis: Long)

    fun next(nowMillis: Long, fajrToday: Long, maghribToday: Long, fajrTomorrow: Long): Next = when {
        nowMillis < fajrToday -> Next(Event.SEHRI_END, fajrToday)
        nowMillis < maghribToday -> Next(Event.IFTAR, maghribToday)
        else -> Next(Event.SEHRI_END, fajrTomorrow)
    }
}
