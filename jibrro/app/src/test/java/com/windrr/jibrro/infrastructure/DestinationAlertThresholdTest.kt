package com.windrr.jibrro.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DestinationAlertThresholdTest {
    @Test
    fun notifiesOncePerCloserThreshold() {
        val tracker = DestinationApproach()
        assertNull(tracker.update(2_001.0))
        assertEquals(2_000, tracker.update(2_000.0))
        assertNull(tracker.update(1_800.0))
        assertEquals(1_000, tracker.update(1_000.0))
        assertNull(tracker.update(1_050.0))
        assertEquals(500, tracker.update(499.0))
        assertNull(tracker.update(505.0))
    }

    @Test
    fun rearmsAfterLeavingTheBandAndIgnoresGpsJitter() {
        val tracker = DestinationApproach()
        assertEquals(500, tracker.update(100.0))
        assertNull(tracker.update(590.0))
        assertNull(tracker.update(490.0))
        assertNull(tracker.update(620.0))
        assertEquals(500, tracker.update(490.0))
        assertNull(tracker.update(2_101.0))
        assertEquals(2_000, tracker.update(1_900.0))
        assertNull(tracker.update(Double.NaN))
    }
}
