package com.islamshikhi.app.feature.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CustomTimesTest {
    private val zone = ZoneId.of("Asia/Dhaka")
    private val date = LocalDate.of(2026, 11, 7)

    private fun base(): DayTimes {
        val start = date.atStartOfDay(zone)
        return DayTimes(date, mapOf(
            Prayer.FAJR to start.plusMinutes(300).toInstant().toEpochMilli(),
            Prayer.SUNRISE to start.plusMinutes(360).toInstant().toEpochMilli(),
            Prayer.DHUHR to start.plusMinutes(700).toInstant().toEpochMilli(),
            Prayer.ASR to start.plusMinutes(900).toInstant().toEpochMilli(),
            Prayer.MAGHRIB to start.plusMinutes(1020).toInstant().toEpochMilli(),
            Prayer.ISHA to start.plusMinutes(1140).toInstant().toEpochMilli()
        ))
    }

    @Test fun encodeDecode_roundTrip() {
        val m = mapOf("fajr" to 315, "isha" to 1200)
        assertEquals(m, CustomTimes.decode(CustomTimes.encode(m)))
    }

    @Test fun decode_ignoresBadEntries() {
        val m = CustomTimes.decode("fajr=330,sunrise=360,dhuhr=5000,asr=abc,junk,maghrib=1080")
        assertEquals(mapOf("fajr" to 330, "maghrib" to 1080), m)
        assertTrue(CustomTimes.decode(null).isEmpty())
        assertTrue(CustomTimes.decode("").isEmpty())
    }

    @Test fun applyCustom_changesOnlyChosenObligations() {
        val b = base()
        val r = PrayerEngine.applyCustom(b, mapOf("fajr" to 330, "sunrise" to 400), zone)
        val start = date.atStartOfDay(zone)
        assertEquals(start.plusMinutes(330).toInstant().toEpochMilli(), r[Prayer.FAJR])
        assertEquals(b[Prayer.SUNRISE], r[Prayer.SUNRISE])
        assertEquals(b[Prayer.ISHA], r[Prayer.ISHA])
    }

    @Test fun jamaat_onlyWhenCustomOn() {
        val on = PrayerSettings(useCustom = true, jamaatMin = mapOf("dhuhr" to 780))
        val off = on.copy(useCustom = false)
        val expected = date.atStartOfDay(zone).plusMinutes(780).toInstant().toEpochMilli()
        assertEquals(expected, PrayerEngine.jamaatTimes(on, date, zone)[Prayer.DHUHR])
        assertTrue(PrayerEngine.jamaatTimes(off, date, zone).isEmpty())
    }

    @Test fun timesFor_defaultModeIgnoresStoredCustomTimes() {
        val on = PrayerSettings(useCustom = true, adhanMin = mapOf("fajr" to 10))
        val off = on.copy(useCustom = false)
        val calc = PrayerEngine.calculated(off, date)
        assertEquals(calc[Prayer.FAJR], PrayerEngine.timesFor(off, date, zone)[Prayer.FAJR])
        assertEquals(date.atStartOfDay(zone).plusMinutes(10).toInstant().toEpochMilli(), PrayerEngine.timesFor(on, date, zone)[Prayer.FAJR])
    }
}
