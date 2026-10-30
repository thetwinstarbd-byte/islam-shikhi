package com.islamshikhi.app.feature.prayer

import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import java.time.LocalDate
import java.time.ZoneId

/** একটি দিনের সময়সূচি: প্রতিটি ওয়াক্তের epoch মিলিসেকেন্ড। */
data class DayTimes(val date: LocalDate, val times: Map<Prayer, Long>) {
    operator fun get(p: Prayer): Long = times.getValue(p)
}

/**
 * নামাজের সময় গণনা। হিসাবটি ওপেন-সোর্স adhan2 লাইব্রেরি (MIT) করে।
 * সতর্কতা: গণনা-পদ্ধতি ও এলাকাভেদে মসজিদের জামাতের সময়ের সঙ্গে কয়েক মিনিট পার্থক্য হতে পারে।
 */
object PrayerEngine {

    fun timesFor(settings: PrayerSettings, date: LocalDate): DayTimes =
        timesFor(settings.lat, settings.lon, date, settings.method, settings.hanafiAsr)

    fun timesFor(lat: Double, lon: Double, date: LocalDate, method: CalcMethodOption, hanafiAsr: Boolean): DayTimes {
        val params = method.adhan.parameters.copy(madhab = if (hanafiAsr) Madhab.HANAFI else Madhab.SHAFI)
        val pt = PrayerTimes(
            Coordinates(lat, lon),
            DateComponents(date.year, date.monthValue, date.dayOfMonth),
            params
        )
        return DayTimes(
            date,
            mapOf(
                Prayer.FAJR to pt.fajr.toEpochMilliseconds(),
                Prayer.SUNRISE to pt.sunrise.toEpochMilliseconds(),
                Prayer.DHUHR to pt.dhuhr.toEpochMilliseconds(),
                Prayer.ASR to pt.asr.toEpochMilliseconds(),
                Prayer.MAGHRIB to pt.maghrib.toEpochMilliseconds(),
                Prayer.ISHA to pt.isha.toEpochMilliseconds()
            )
        )
    }

    data class Upcoming(val prayer: Prayer, val atMillis: Long)

    /** [nowMillis]-এর পরের প্রথম ফরজ ওয়াক্ত (আজ শেষ হলে আগামীকালের ফজর)। */
    fun next(settings: PrayerSettings, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Upcoming {
        // LocalDate.ofInstant পুরোনো Android-এ (১৩-এর আগে) নেই, তাই atZone ব্যবহার
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0..1) {
            val day = timesFor(settings, today.plusDays(offset.toLong()))
            for (p in Prayer.obligations) {
                if (day[p] > nowMillis) return Upcoming(p, day[p])
            }
        }
        // তাত্ত্বিকভাবে অগম্য; উচ্চ অক্ষাংশে গণনা ব্যর্থ হলেও অ্যাপ যেন না ভাঙে
        return Upcoming(Prayer.FAJR, nowMillis + 24 * 3600_000L)
    }
}
