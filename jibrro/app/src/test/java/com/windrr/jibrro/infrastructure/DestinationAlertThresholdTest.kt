package com.windrr.jibrro.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DestinationAlertThresholdTest {
    @Test
    fun notifiesOncePerCloserThreshold() {
        assertNull(nextDestinationAlertThreshold(2_001.0, null))
        assertEquals(2_000, nextDestinationAlertThreshold(2_000.0, null))
        assertNull(nextDestinationAlertThreshold(1_800.0, 2_000))
        assertEquals(1_000, nextDestinationAlertThreshold(1_000.0, 2_000))
        assertNull(nextDestinationAlertThreshold(1_050.0, 1_000))
        assertEquals(500, nextDestinationAlertThreshold(499.0, 1_000))
        assertNull(nextDestinationAlertThreshold(505.0, 500))
    }

    @Test
    fun startsAtNearestThresholdAfterSkippingBands() {
        assertEquals(500, nextDestinationAlertThreshold(100.0, null))
        assertNull(nextDestinationAlertThreshold(Double.NaN, null))
    }
}
