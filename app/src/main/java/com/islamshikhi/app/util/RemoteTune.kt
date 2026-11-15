package com.islamshikhi.app.util

import java.time.LocalDate
import java.time.chrono.HijrahDate

/**
 * Firebase Remote Config থেকে আসা দূর-নিয়ন্ত্রিত সমন্বয় (অ্যাপ আপডেট ছাড়াই)।
 * ডিফল্ট সব শূন্য — Firebase সেট না থাকলে বা নেট না থাকলে অ্যাপ আগের মতোই চলে। মান সীমার বাইরে গেলে ছাঁটা হয়।
 */
object RemoteTune {
    /** চাঁদ দেখার পার্থক্য মেটাতে হিজরি তারিখ আগে/পরে সরানো: -2..+2 দিন। */
    @Volatile var hijriOffsetDays: Int = 0

    /** ওয়াক্ত-ভিত্তিক মিনিটের সমন্বয় (fajr, dhuhr, asr, maghrib, isha): প্রতিটি -15..+15 মিনিট। */
    @Volatile var prayerAdjustMin: Map<String, Int> = emptyMap()

    fun clampHijri(v: Long): Int = v.coerceIn(-2, 2).toInt()
    fun clampMinutes(v: Long): Int = v.coerceIn(-15, 15).toInt()

    /** সব হিজরি হিসাব এই পথ দিয়ে যায়, যাতে একটি সমন্বয় সব জায়গায় একই থাকে। */
    fun hijri(date: LocalDate): HijrahDate = HijrahDate.from(date.plusDays(hijriOffsetDays.toLong()))
}
