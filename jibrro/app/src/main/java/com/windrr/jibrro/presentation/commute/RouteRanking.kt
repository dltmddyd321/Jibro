package com.windrr.jibrro.presentation.commute

import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteRoute

internal enum class RouteBadge(val label: String) {
    SOONEST_AND_SHORTEST("먼저 도착 · 최단 경로"),
    SOONEST("가장 먼저 도착"),
    SHORTEST("최단 경로")
}

internal data class RankedRoute(val route: CommuteRoute, val badge: RouteBadge?)

/**
 * Orders home routes for the main list:
 * 1. a route whose vehicle arrives first **and** is the shortest trip,
 * 2. otherwise the route whose vehicle arrives first (ties go to the shorter trip),
 * 3. everything else in its original order (shortest trip first),
 * with routes whose service has ended always at the bottom.
 *
 * Only arrivals with a known countdown that the rider can still catch (the vehicle arrives
 * no earlier than the walk to the boarding stop) take part in "arrives first"; while
 * real-time data is missing the list falls back to the original order.
 */
internal fun rankRoutes(
    routes: List<CommuteRoute>,
    arrivals: Map<CommuteRoute, BoardingArrival?>
): List<RankedRoute> {
    val (unsorted, ended) = routes.partition { arrivals[it]?.serviceEnded != true }
    val running = unsorted.sortedBy { it.minutes }
    val shortestMinutes = running.minOfOrNull { it.minutes }

    val soonest = running
        .mapNotNull { route -> catchableWait(route, arrivals[route])?.let { route to it } }
        .minWithOrNull(compareBy<Pair<CommuteRoute, Int>> { it.second }.thenBy { it.first.minutes })
        ?.first

    val lead = soonest?.let {
        RankedRoute(it, if (it.minutes == shortestMinutes) RouteBadge.SOONEST_AND_SHORTEST else RouteBadge.SOONEST)
    }
    var shortestTagged = lead?.badge == RouteBadge.SOONEST_AND_SHORTEST
    val rest = running.filter { it !== soonest }.map { route ->
        val isShortest = !shortestTagged && route.minutes == shortestMinutes
        if (isShortest) shortestTagged = true
        RankedRoute(route, if (isShortest) RouteBadge.SHORTEST else null)
    }
    return listOfNotNull(lead) + rest + ended.map { RankedRoute(it, null) }
}

private fun catchableWait(route: CommuteRoute, arrival: BoardingArrival?): Int? =
    arrival?.takeIf { !it.serviceEnded }?.seconds?.takeIf { it >= route.approachMinutes * 60 }
