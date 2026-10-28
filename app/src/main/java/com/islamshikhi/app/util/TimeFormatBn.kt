package com.islamshikhi.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/** বাংলা সময় দেখানো: যেমন "ভোর ৪:২৯", "দুপুর ১২:১৫"। */
object TimeFormatBn {

    fun period(hour24: Int): String = when (hour24) {
        in 4..5 -> "ভোর"
        in 6..11 -> "সকাল"
        in 12..14 -> "দুপুর"
        in 15..16 -> "বিকাল"
        in 17..18 -> "সন্ধ্যা"
        else -> "রাত"
    }

    fun format(hour24: Int, minute: Int): String {
        val h12 = when (val h = hour24 % 12) { 0 -> 12 else -> h }
        return "${period(hour24)} ${DateFormatBn.digits(h12)}:${DateFormatBn.digits("%02d".format(minute))}"
    }

    fun format(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val t: ZonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(zone)
        return format(t.hour, t.minute)
    }

    /** বাকি সময়: "২ ঘণ্টা ১৫ মিনিট" বা "৪৫ মিনিট ১০ সেকেন্ড"। */
    fun countdown(millis: Long): String {
        val total = (millis / 1000).coerceAtLeast(0)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return when {
            h > 0 -> "${DateFormatBn.digits(h.toInt())} ঘণ্টা ${DateFormatBn.digits(m.toInt())} মিনিট"
            else -> "${DateFormatBn.digits(m.toInt())} মিনিট ${DateFormatBn.digits(s.toInt())} সেকেন্ড"
        }
    }
}
