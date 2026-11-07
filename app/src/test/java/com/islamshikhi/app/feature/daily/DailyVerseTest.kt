package com.islamshikhi.app.feature.daily

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyVerseTest {
    @Test fun evenDaysGiveAyah_oddDaysGiveHadith() {
        assertTrue(DailyVerse.choose(20000L, 19, 10) is DailyVerse.Pick.AyahPick)
        assertTrue(DailyVerse.choose(20001L, 19, 10) is DailyVerse.Pick.HadithPick)
    }
    @Test fun noHadith_alwaysAyah() {
        assertTrue(DailyVerse.choose(20001L, 19, 0) is DailyVerse.Pick.AyahPick)
    }
    @Test fun indexAlwaysInRange() {
        for (d in 0L..400L) {
            when (val p = DailyVerse.choose(d, 19, 7)) {
                is DailyVerse.Pick.AyahPick -> assertTrue(p.index in 0 until 19)
                is DailyVerse.Pick.HadithPick -> assertTrue(p.index in 0 until 7)
            }
        }
    }
    @Test fun sameDaySamePick() {
        assertEquals(DailyVerse.choose(21000L, 19, 7), DailyVerse.choose(21000L, 19, 7))
    }
}
