package com.windrr.jibrro.data.commute

import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteLeg
import com.windrr.jibrro.domain.commute.CommuteRoute
import com.windrr.jibrro.domain.commute.TransitLine
import com.windrr.jibrro.domain.commute.TransitMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun RoutePath.toRoute(): CommuteRoute? {
    val summary = info ?: return null
    val minutes = summary.totalTime?.takeIf { it >= 0 } ?: return null
    val segments = subPath?.takeIf { it.isNotEmpty() } ?: return null
    val approachMinutes = segments.takeWhile { it.trafficType == 3 }
        .sumOf { it.sectionTime?.coerceAtLeast(0) ?: 0 }
    val rides = segments.mapNotNull { segment ->
        val mode = when (segment.trafficType) {
            1 -> TransitMode.SUBWAY
            2 -> TransitMode.BUS
            3 -> return@mapNotNull null
            else -> return null
        }
        val lines = segment.lane.orEmpty().mapNotNull { lane ->
            when (mode) {
                TransitMode.SUBWAY -> lane.name?.takeIf(String::isNotBlank)?.let { TransitLine(it) }
                TransitMode.BUS -> lane.busNo?.takeIf(String::isNotBlank)?.let {
                    TransitLine(it, lane.busLocalBlID, lane.busCityCode)
                }
            }
        }
        CommuteLeg(
            mode = mode,
            start = segment.startName?.takeIf(String::isNotBlank) ?: return null,
            end = segment.endName?.takeIf(String::isNotBlank) ?: return null,
            lines = lines,
            direction = segment.way?.takeIf(String::isNotBlank),
            directionCode = segment.wayCode,
            stationArsId = segment.startArsID,
            localStationId = segment.startLocalStationID,
            stations = segment.passStopList?.stations.orEmpty().mapNotNull { it.stationName }
        )
    }
    return rides.takeIf { it.isNotEmpty() }?.let { CommuteRoute(minutes, approachMinutes, it) }
}

internal fun rankRoutes(routes: List<CommuteRoute>): List<CommuteRoute> = routes.distinct()
    .sortedWith(compareBy<CommuteRoute> { it.minutes }.thenBy { it.transfers })

private val stopCountPattern = Regex("""\[?(\d+)\]?\s*(?:번째|정거장|정류장)\s*전""")
private val minutesPattern = Regex("""(\d+)\s*분""")
private val secondsPattern = Regex("""(\d+)\s*초""")
private val subwayForecastPattern = Regex("""^\s*(?=\d)(?:(\d+)\s*분\s*)?(?:(\d+)\s*초\s*)?후""")
private val stationQualifier = Regex("""\s*\([^)]*\)""")
private val subwayLineIds = mapOf(
    "경의중앙선" to "1063", "공항철도" to "1065", "경춘선" to "1067",
    "수인분당선" to "1075", "신분당선" to "1077", "경강선" to "1081",
    "우이신설선" to "1092", "서해선" to "1093", "신림선" to "1094", "GTXA" to "1032"
)

private fun subwayLineId(name: String): String? {
    val normalized = name.replace(Regex("""[\s·–-]"""), "").removePrefix("수도권").removePrefix("서울")
    val number = normalized.removeSuffix("호선").toIntOrNull()
    if (number != null && number in 1..9) return (1000 + number).toString()
    return subwayLineIds[normalized]
}

private fun normalizedStation(name: String?) = name.orEmpty().replace(stationQualifier, "")
    .trim().removeSuffix("역").replace(" ", "")

internal fun selectSubwayArrival(
    rows: List<RealtimeArrival>,
    leg: CommuteLeg,
    nowMillis: Long
): BoardingArrival? {
    val lineIds = leg.lines.mapNotNull { subwayLineId(it.name) }
    val boarding = normalizedStation(leg.start)
    val nextStation = leg.stations.dropWhile { normalizedStation(it) == boarding }
        .firstOrNull()?.let(::normalizedStation)
    val direction = when (leg.directionCode) {
        1 -> setOf("상행", "내선")
        2 -> setOf("하행", "외선")
        else -> emptySet()
    }
    val destination = normalizedStation(leg.end)

    return rows.asSequence()
        .filter { it.subwayId in lineIds && normalizedStation(it.statnNm) == boarding }
        .filter { direction.isEmpty() || it.updnLine in direction }
        .filter { arrival ->
            val terminal = normalizedStation(arrival.bstatnNm)
            val heading = arrival.trainLineNm.orEmpty().substringAfter(" - ", "")
                .removeSuffix("방면").let(::normalizedStation)
            val way = normalizedStation(leg.direction?.removeSuffix("방면")?.removeSuffix("행"))
            val endsBeforeDestination = terminal.isNotEmpty() && terminal != destination &&
                leg.stations.dropLast(1).any { normalizedStation(it) == terminal }
            val samePath = (terminal.isNotEmpty() && terminal == destination && direction.isNotEmpty()) ||
                (nextStation != null && heading == nextStation) ||
                (nextStation == null && way.isNotEmpty() && (heading == way || terminal == way))
            samePath && !endsBeforeDestination && arrival.btrainSttus.orEmpty().trim() in setOf("", "일반")
        }
        .mapNotNull { row ->
            subwayArrival(row, nowMillis)?.let { arrival ->
                val order = row.ordkey.orEmpty().getOrNull(1)?.digitToIntOrNull() ?: Int.MAX_VALUE
                order to arrival
            }
        }
        .minWithOrNull(compareBy<Pair<Int, BoardingArrival>> { it.first }.thenBy { it.second.seconds ?: Int.MAX_VALUE })
        ?.second
}

internal fun subwayArrival(arrival: RealtimeArrival, nowMillis: Long): BoardingArrival? {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
        isLenient = false
    }
    val receivedAt = arrival.recptnDt.orEmpty().takeIf { it.length == 19 }?.let {
        try {
            timestamp.parse(it)?.time
        } catch (_: java.text.ParseException) {
            null
        }
    } ?: return null
    val ageSeconds = (nowMillis - receivedAt) / 1000
    if (ageSeconds !in -30..180 || arrival.arvlCd == "2") return null

    val message = arrival.arvlMsg2.orEmpty()
    val arriving = arrival.arvlCd in setOf("0", "1")
    val stops = stopCountPattern.find(message)?.groupValues?.get(1)?.toIntOrNull()
        ?: arrival.ordkey.orEmpty().takeIf { it.length >= 5 }?.substring(2, 5)?.toIntOrNull()
        ?: if (arrival.arvlCd in setOf("3", "4", "5")) 1 else null
    val forecast = arrival.barvlDt.orEmpty().toIntOrNull()?.takeIf { it > 0 }
        ?: subwayForecastPattern.find(message)?.destructured?.let { (minutes, seconds) ->
            ((minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)).takeIf { it > 0 }
        }
    if (!arriving && forecast != null && forecast <= ageSeconds) return null
    val seconds = if (arriving) 0 else forecast?.minus(ageSeconds.coerceAtLeast(0).toInt())
    val status = if (arriving) message.ifBlank { "곧 도착" } else null
    if (seconds == null && (stops == null || stops <= 0)) return null
    return BoardingArrival(
        seconds = seconds,
        stopsAway = stops,
        status = status,
        line = arrival.bstatnNm.orEmpty().takeIf(String::isNotBlank)?.plus("행"),
        direction = arrival.updnLine.orEmpty().trim().takeIf(String::isNotBlank)
    )
}

internal fun busArrival(message: String, line: String): BoardingArrival? {
    if (message.contains("운행종료") || message.contains("출발대기") || message.contains("회차대기")) return null
    val minutes = minutesPattern.find(message)?.groupValues?.get(1)?.toIntOrNull()
    val seconds = secondsPattern.find(message)?.groupValues?.get(1)?.toIntOrNull()
    val stops = stopCountPattern.find(message)?.groupValues?.get(1)?.toIntOrNull()
    val arriving = message.contains("곧 도착") || message.contains("곧도착") || message == "도착"
    val eta = when {
        minutes != null || seconds != null -> (minutes ?: 0) * 60 + (seconds ?: 0)
        arriving -> 0
        else -> null
    }
    if (eta == null && stops == null) return null
    return BoardingArrival(eta, stops, if (arriving) "곧 도착" else null, line)
}

internal const val SEOUL_BUS_CITY = 1000
private val gyeonggiBusCities = 1010..1310 step 10
private val gyeonggiStationIdPattern = Regex("[1-9][0-9]{8}")

internal fun CommuteLeg.gyeonggiStationId(): String? = localStationId.orEmpty()
    .split('/').map(String::trim).singleOrNull { it.matches(gyeonggiStationIdPattern) }

internal fun CommuteLeg.servesGyeonggiBus(): Boolean = lines.any { it.cityCode in gyeonggiBusCities }

internal fun selectBusArrival(
    rows: List<BusArrival>,
    ride: CommuteLeg,
    gyeonggiRows: List<GyeonggiBusArrival> = emptyList()
): BoardingArrival? {
    val gyeonggiStationId = ride.gyeonggiStationId()
    val arrivals = ride.lines.map { line ->
        if (line.localId.isNullOrBlank()) return@map null
        if (line.cityCode in gyeonggiBusCities && gyeonggiStationId != null) {
            return@map gyeonggiRows.singleOrNull { it.routeId == line.localId && it.stationId == gyeonggiStationId }
                ?.toBoardingArrival(line.name)
        }
        if (line.cityCode != SEOUL_BUS_CITY) return@map null
        rows.singleOrNull { row ->
            row.busRouteId == line.localId && (ride.localStationId.isNullOrBlank() || ride.localStationId == row.stId)
        }?.let { row ->
            val messages = listOfNotNull(row.arrmsg1, row.arrmsg2).filter(String::isNotBlank)
            if (messages.isNotEmpty() && messages.all { it.replace(" ", "") == "운행종료" }) {
                BoardingArrival(null, null, status = "운행 종료", line = line.name, serviceEnded = true)
            } else {
                messages.firstNotNullOfOrNull { busArrival(it, line.name) }
            }
        }
    }
    val next = arrivals.filterNotNull().filterNot { it.serviceEnded }.minByOrNull { it.seconds ?: Int.MAX_VALUE }
    if (next != null) return next
    return if (arrivals.isNotEmpty() && arrivals.all { it?.serviceEnded == true }) {
        BoardingArrival(null, null, status = "운행 종료", serviceEnded = true)
    } else null
}

private fun GyeonggiBusArrival.toBoardingArrival(line: String): BoardingArrival? {
    if (flag == "STOP") return BoardingArrival(null, null, "운행 종료", line, serviceEnded = true)
    if (flag != "RUN" && flag != "PASS") return null

    fun vehicle(seconds: String?, minutes: String?, stops: String?): BoardingArrival? {
        val eta = seconds?.toIntOrNull()?.takeIf { it >= 0 }
            ?: minutes?.toIntOrNull()?.takeIf { it in 0..Int.MAX_VALUE / 60 }?.times(60)
        val stopsAway = stops?.toIntOrNull()?.takeIf { it > 0 }
        if (eta == null && stopsAway == null) return null
        return BoardingArrival(eta, stopsAway, line = line)
    }

    return vehicle(predictTimeSec1, predictTime1, locationNo1)
        ?: vehicle(predictTimeSec2, predictTime2, locationNo2)
}
