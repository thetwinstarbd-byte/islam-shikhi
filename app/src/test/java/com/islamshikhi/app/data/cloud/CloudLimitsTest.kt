package com.islamshikhi.app.data.cloud

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudLimitsTest {
    @Test fun smallJsonFits() = assertTrue(CloudLimits.fits("{\"a\":1}"))
    @Test fun hugeJsonDoesNotFit() = assertFalse(CloudLimits.fits("x".repeat(CloudLimits.MAX_CHARS + 1)))
}
