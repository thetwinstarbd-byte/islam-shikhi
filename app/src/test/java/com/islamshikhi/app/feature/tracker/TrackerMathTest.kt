package com.islamshikhi.app.feature.tracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class TrackerMathTest {

    @Test fun khatm_dailyTarget_roundsUp() {
        assertEquals(208, KhatmMath.dailyTarget(6236, 30))   // 6236/30 = 207.9
        assertEquals(6236, KhatmMath.dailyTarget(6236, 1))
        assertEquals(0, KhatmMath.dailyTarget(6236, 0))
    }

    @Test fun khatm_daysLeft_countsStartDayAsFirst() {
        val start = LocalDate.of(2026, 3, 1)
        assertEquals(30, KhatmMath.daysLeft(start, 30, start))
        assertEquals(20, KhatmMath.daysLeft(start, 30, start.plusDays(10)))
        assertEquals(0, KhatmMath.daysLeft(start, 30, start.plusDays(45)))
        assertEquals(30, KhatmMath.daysLeft(start, 30, start.minusDays(3)))   // ঘড়ি পিছিয়ে গেলেও বাড়ে না
    }

    @Test fun khatm_requiredPerDay() {
        assertEquals(0, KhatmMath.requiredPerDay(0, 5))
        assertEquals(100, KhatmMath.requiredPerDay(1000, 10))
        assertEquals(101, KhatmMath.requiredPerDay(1001, 10))
        assertEquals(500, KhatmMath.requiredPerDay(500, 0))   // সময় শেষ: বাকি সবটুকু
    }

    @Test fun ramadan_info_findsNextRamadanStart() {
        val today = LocalDate.of(2026, 1, 10)
        val info = RamadanMath.info(today)
        if (!info.inRamadan) {
            val h = HijrahDate.from(info.start)
            assertEquals(9, h.get(ChronoField.MONTH_OF_YEAR))
            assertEquals(1, h.get(ChronoField.DAY_OF_MONTH))
            assertTrue(info.daysUntil > 0)
            assertTrue(!info.start.isBefore(today))
        }
    }

    @Test fun ramadan_info_insideRamadanDayNumberMatches() {
        val start = RamadanMath.info(LocalDate.of(2026, 1, 10)).start
        val mid = start.plusDays(9)
        val info = RamadanMath.info(mid)
        assertTrue(info.inRamadan)
        assertEquals(10, info.dayNumber)
        assertEquals(start, info.start)
        assertEquals(0, info.daysUntil)
    }

    @Test fun ramadan_next_picksCorrectEvent() {
        assertEquals(RamadanMath.Event.SEHRI_END, RamadanMath.next(10, 100, 500, 1100).event)
        assertEquals(RamadanMath.Event.IFTAR, RamadanMath.next(200, 100, 500, 1100).event)
        val after = RamadanMath.next(600, 100, 500, 1100)
        assertEquals(RamadanMath.Event.SEHRI_END, after.event)
        assertEquals(1100L, after.atMillis)
    }

    @Test fun ramadan_count_onlyCountsThirtyDays() {
        val start = LocalDate.of(2026, 2, 18)
        val done = setOf(
            RamadanMath.key(start, RamadanMath.Deed.ROZA),
            RamadanMath.key(start.plusDays(29), RamadanMath.Deed.ROZA),
            RamadanMath.key(start.plusDays(30), RamadanMath.Deed.ROZA),
            RamadanMath.key(start, RamadanMath.Deed.TARAWIH)
        )
        assertEquals(2, RamadanMath.count(done, start, RamadanMath.Deed.ROZA))
        assertEquals(1, RamadanMath.count(done, start, RamadanMath.Deed.TARAWIH))
        assertFalse(RamadanMath.key(start, RamadanMath.Deed.ROZA) == RamadanMath.key(start, RamadanMath.Deed.TARAWIH))
    }

    @Test fun qada_keysAreUnique() {
        assertEquals(QadaItem.entries.size, QadaItem.entries.map { it.key }.toSet().size)
    }
}
