package com.islamshikhi.app.util

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** বাংলায় ইংরেজি ও হিজরি তারিখ দেখানোর সহায়ক। */
object DateFormatBn {

    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    private val weekdays = listOf(
        "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার", "রবিবার"
    )

    private val gregorianMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val hijriMonths = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
        "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    /** সংখ্যার ইংরেজি অঙ্ক বাংলা অঙ্কে বদলায়। */
    fun digits(number: Int): String = digits(number.toString())

    fun digits(text: String): String =
        text.map { if (it in '0'..'9') bnDigits[it - '0'] else it }.joinToString("")

    /** যেমন: "সোমবার, ২৭ অক্টোবর ২০২৫" */
    fun gregorian(date: LocalDate): String {
        val weekday = weekdays[date.dayOfWeek.value - 1]
        val month = gregorianMonths[date.monthValue - 1]
        return "$weekday, ${digits(date.dayOfMonth)} $month ${digits(date.year)}"
    }

    /**
     * হিজরি তারিখ (উম্মুল কুরা পঞ্জিকা অনুযায়ী গণনা)।
     * সতর্কতা: স্থানীয় চাঁদ দেখার ওপর ভিত্তি করে বাস্তবে এক দিন পর্যন্ত পার্থক্য হতে পারে।
     */
    fun hijri(date: LocalDate): String {
        val hijri = HijrahDate.from(date)
        val day = hijri.get(ChronoField.DAY_OF_MONTH)
        val month = hijri.get(ChronoField.MONTH_OF_YEAR)
        val year = hijri.get(ChronoField.YEAR)
        return "${digits(day)} ${hijriMonths[month - 1]} ${digits(year)} হিজরি"
    }
}
