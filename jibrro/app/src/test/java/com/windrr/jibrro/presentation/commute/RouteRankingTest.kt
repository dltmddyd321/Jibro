package com.windrr.jibrro.presentation.commute

import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteLeg
import com.windrr.jibrro.domain.commute.CommuteRoute
import com.windrr.jibrro.domain.commute.TransitMode
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteRankingTest {
    private fun route(minutes: Int, start: String) =
        CommuteRoute(minutes, approachMinutes = 3, rides = listOf(CommuteLeg(TransitMode.SUBWAY, start, "집")))

    private fun arriving(seconds: Int?) = BoardingArrival(seconds = seconds, stopsAway = null)

    private val shortest = route(25, "A")
    private val middle = route(30, "B")
    private val longest = route(40, "C")

    @Test
    fun soonestArrivalThatIsAlsoShortestLeadsWithCombinedBadge() {
        val ranked = rankRoutes(
            listOf(longest, middle, shortest),
            mapOf(shortest to arriving(200), middle to arriving(260), longest to arriving(300))
        )
        assertEquals(listOf(shortest, middle, longest), ranked.map { it.route })
        assertEquals(listOf(RouteBadge.SOONEST_AND_SHORTEST, null, null), ranked.map { it.badge })
    }

    @Test
    fun soonestArrivalLeadsEvenWhenNotShortestAndRestKeepTripOrder() {
        val ranked = rankRoutes(
            listOf(shortest, middle, longest),
            mapOf(shortest to arriving(600), middle to arriving(400), longest to arriving(190))
        )
        assertEquals(listOf(longest, shortest, middle), ranked.map { it.route })
        assertEquals(listOf(RouteBadge.SOONEST, RouteBadge.SHORTEST, null), ranked.map { it.badge })
    }

    @Test
    fun tiedArrivalsPreferTheShorterTrip() {
        val ranked = rankRoutes(
            listOf(middle, longest),
            mapOf(middle to arriving(240), longest to arriving(240))
        )
        assertEquals(middle, ranked.first().route)
        assertEquals(RouteBadge.SOONEST_AND_SHORTEST, ranked.first().badge)
    }

    @Test
    fun endedServiceSinksAndUnknownCountdownsDoNotLead() {
        val ranked = rankRoutes(
            listOf(shortest, middle, longest),
            mapOf(
                shortest to BoardingArrival(seconds = 30, stopsAway = null, serviceEnded = true),
                middle to arriving(null),
                longest to arriving(500)
            )
        )
        assertEquals(listOf(longest, middle, shortest), ranked.map { it.route })
        assertEquals(listOf(RouteBadge.SOONEST, RouteBadge.SHORTEST, null), ranked.map { it.badge })
    }

    @Test
    fun withoutRealtimeDataFallsBackToShortestTripFirst() {
        val ranked = rankRoutes(listOf(longest, shortest, middle), emptyMap())
        assertEquals(listOf(shortest, middle, longest), ranked.map { it.route })
        assertEquals(listOf(RouteBadge.SHORTEST, null, null), ranked.map { it.badge })
    }

    @Test
    fun vehiclesLeavingBeforeTheRiderReachesTheStopDoNotLead() {
        // approachMinutes = 3 → anything under 180 s is uncatchable.
        val ranked = rankRoutes(
            listOf(shortest, middle, longest),
            mapOf(shortest to arriving(60), middle to arriving(400), longest to arriving(250))
        )
        assertEquals(listOf(longest, shortest, middle), ranked.map { it.route })
        assertEquals(listOf(RouteBadge.SOONEST, RouteBadge.SHORTEST, null), ranked.map { it.badge })
    }
}
