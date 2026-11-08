package com.islamshikhi.app.feature.adhan

import com.islamshikhi.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AdhanPrefsTest {
    @Test fun fajrUsesFajrAdhan() = assertEquals(R.raw.adhan_fajr, AdhanPrefs.rawFor("fajr"))

    @Test fun otherPrayersUseNormalAdhan() {
        for (id in listOf("dhuhr", "asr", "maghrib", "isha")) assertEquals(R.raw.adhan_normal, AdhanPrefs.rawFor(id))
        assertNotEquals(R.raw.adhan_fajr, R.raw.adhan_normal)
    }
}
