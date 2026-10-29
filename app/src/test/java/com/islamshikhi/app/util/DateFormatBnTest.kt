package com.islamshikhi.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateFormatBnTest {
    @Test fun digits_convertsToBangla() {
        assertEquals("২০২৫", DateFormatBn.digits(2025))
        assertEquals("১.৫ ab", DateFormatBn.digits("1.5 ab"))
    }

    @Test fun gregorian_weekdayMonthYear() {
        // ২৭ অক্টোবর ২০২৫ ছিল সোমবার
        assertEquals("সোমবার, ২৭ অক্টোবর ২০২৫", DateFormatBn.gregorian(LocalDate.of(2025, 10, 27)))
    }

    @Test fun hijri_firstOfRamadan1446() {
        // উম্মুল কুরা পঞ্জিকায় ১ রমজান ১৪৪৬ = ১ মার্চ ২০২৫
        assertEquals("১ রমজান ১৪৪৬ হিজরি", DateFormatBn.hijri(LocalDate.of(2025, 3, 1)))
    }
}
