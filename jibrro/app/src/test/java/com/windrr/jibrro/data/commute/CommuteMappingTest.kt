package com.windrr.jibrro.data.commute

import com.google.gson.Gson
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.domain.commute.CommuteLeg
import com.windrr.jibrro.domain.commute.TransitLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.GregorianCalendar
import java.util.TimeZone

class CommuteMappingTest {
    private val gson = Gson()
    private val now = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul")).apply {
        clear()
        set(2026, 10, 22, 17, 26, 30)
    }.timeInMillis

    @Test
    fun readsNearDestinationErrorObject() {
        val response = gson.fromJson(
            """{"error":{"code":-98,"msg":"출, 도착지가 700m이내입니다."}}""",
            RouteResponse::class.java
        )
        assertEquals("-98", response.error?.code)
        assertEquals("출, 도착지가 700m이내입니다.", response.error?.message)
    }

    @Test
    fun readsAuthenticationErrorArray() {
        val response = gson.fromJson(
            """{"error":[{"code":"500","message":"[ApiKeyAuthFailed] ApiKey authentication failed."}]}""",
            RouteResponse::class.java
        )
        assertEquals("500", response.error?.code)
        assertEquals("[ApiKeyAuthFailed] ApiKey authentication failed.", response.error?.message)
    }

    @Test
    fun ranksTransitRoutesAndKeepsTransferStops() {
        val transit = gson.fromJson(
            """{"result":{"searchType":0,"path":[
                {"info":{"totalTime":34,"totalWalk":230,"payment":1500},"subPath":[
                    {"trafficType":3,"sectionTime":3},
                    {"trafficType":2,"sectionTime":15,"startName":"강남역","endName":"시청 앞",
                        "startArsID":"22010","startLocalStationID":"122000500",
                        "lane":[{"busNo":"146","busLocalBlID":"100100020","busCityCode":1000}]},
                    {"trafficType":3,"sectionTime":2},
                    {"trafficType":1,"sectionTime":14,"startName":"시청","endName":"집 근처",
                        "lane":[{"name":"2호선"}]}
                ]},
                {"info":{"totalTime":15,"totalWalk":350,"payment":1550},"subPath":[
                    {"trafficType":1,"sectionTime":15,"startName":"강남","endName":"교대",
                        "wayCode":1,"lane":[{"name":"수도권 2호선"}]}
                ]}
            ]}}""",
            RouteResponse::class.java
        )
        val candidates = rankRoutes(transit.result?.path.orEmpty().mapNotNull { it.toRoute() })

        assertEquals(listOf(15, 34), candidates.map { it.minutes })
        assertEquals(listOf(TransitMode.SUBWAY, TransitMode.BUS), candidates.map { it.firstRide?.mode })
        val busRoute = candidates.last()
        assertEquals(3, busRoute.approachMinutes)
        assertEquals(1, busRoute.transfers)
        assertEquals("시청 앞", busRoute.rides[0].end)
        assertEquals("시청", busRoute.rides[1].start)
        assertEquals("22010", candidates.last().firstRide?.stationArsId)
        assertEquals("100100020", candidates.last().firstRide?.lines?.single()?.localId)
    }

    @Test
    fun selectsMatchingUpcomingSubwayAndAdjustsForProviderTimestamp() {
        val leg = gson.fromJson(
            """{"info":{"totalTime":15},"subPath":[{"trafficType":1,"sectionTime":15,
                "startName":"강남","endName":"서초","wayCode":1,"lane":[{"name":"수도권 2호선"}],
                "passStopList":{"stations":[{"stationName":"강남"},{"stationName":"교대"},{"stationName":"서초"}]}
            }]}""", RoutePath::class.java
        ).toRoute()?.firstRide ?: error("Missing train leg")

        val wrongDirection = train(direction = "외선")
        val shortTurning = train(terminal = "교대")
        val departed = train(code = "2")
        val expected = train()
        val arrival = selectSubwayArrival(listOf(wrongDirection, shortTurning, departed, expected), leg, now)

        assertNotNull(arrival)
        assertEquals(100, arrival?.seconds)
        assertEquals(2, arrival?.stopsAway)
        assertEquals("홍대입구행", arrival?.line)
        assertNull(selectSubwayArrival(listOf(wrongDirection, shortTurning, departed), leg, now))
        assertNull(subwayArrival(expected, now + 120_000))
    }

    @Test
    fun usesArrivalMessageWhenSubwayForecastIsMissing() {
        val arrival = subwayArrival(
            train(direction = "하행", forecast = "0", message = "4분 30초 후 (삼각지)", orderKey = ""),
            now
        )
        assertEquals("하행", arrival?.direction)
        assertEquals(250, arrival?.seconds)
    }

    @Test
    fun keepsBracketedSubwayStopCountWithoutInventingAnEta() {
        val arrival = subwayArrival(
            train(direction = "상행", forecast = "0", message = "[4]번째 전역 (효창공원앞)", orderKey = ""),
            now
        )
        assertEquals("상행", arrival?.direction)
        assertEquals(4, arrival?.stopsAway)
        assertNull(arrival?.seconds)
    }

    @Test
    fun readsBusEtaAndStopsIndependently() {
        val arrival = busArrival("3분25초후[4번째 전]", "146")
        assertEquals(205, arrival?.seconds)
        assertEquals(4, arrival?.stopsAway)
        assertEquals(0, busArrival("곧 도착", "146")?.seconds)
        assertNull(busArrival("운행종료", "146"))
    }

    @Test
    fun onlyMatchesSeoulBusUsingStationAndRouteIds() {
        val route = CommuteLeg(
            TransitMode.BUS, start = "강남역", end = "시청 앞", localStationId = "122000500",
            lines = listOf(TransitLine("146", "100100020", 1000), TransitLine("55", "111100000", 1100))
        )
        val wrongStation = BusArrival("100100020", "122000600", "곧 도착", null)
        val differentNetwork = BusArrival("111100000", "122000500", "곧 도착", null)
        val expected = BusArrival("100100020", "122000500", "3분25초후[4번째 전]", null)

        assertNull(selectBusArrival(listOf(wrongStation, differentNetwork), route))
        val arrival = selectBusArrival(listOf(wrongStation, differentNetwork, expected), route)
        assertEquals("146", arrival?.line)
        assertEquals(205, arrival?.seconds)
    }

    @Test
    fun matchesGyeonggiStationAndRouteAndPrefersSecondsOverRoundedMinutes() {
        val ride = CommuteLeg(
            TransitMode.BUS, "승차 정류장", "하차 정류장", localStationId = "07574/206000629/ICB165000355",
            lines = listOf(TransitLine("55", "241219006", 1010))
        )
        val rows = gson.fromJson(
            """{"response":{"msgHeader":{"resultCode":0},"msgBody":{"busArrivalList":[
                {"stationId":206000629,"routeId":241225005,"flag":"PASS",
                    "predictTime1":"","predictTimeSec1":"","locationNo1":""},
                {"stationId":206000629,"routeId":241219006,"flag":"PASS",
                    "predictTime1":3,"predictTimeSec1":145,"locationNo1":4}
            ]}}}""",
            GyeonggiBusEnvelope::class.java
        ).response?.msgBody?.busArrivalList.orEmpty()
        val expected = rows.last()
        val wrongStation = expected.copy(stationId = "206000631")
        val wrongRoute = expected.copy(routeId = "241255002")

        assertEquals("206000629", ride.gyeonggiStationId())
        assertNull(selectBusArrival(emptyList(), ride, listOf(wrongStation, wrongRoute)))
        val arrival = selectBusArrival(emptyList(), ride, rows + listOf(wrongStation, wrongRoute))
        assertEquals(145, arrival?.seconds)
        assertEquals(4, arrival?.stopsAway)
        assertEquals("55", arrival?.line)
    }

    @Test
    fun usesSecondGyeonggiBusWhenFirstVehicleIsMissingAndKeepsServiceStatus() {
        val ride = CommuteLeg(
            TransitMode.BUS, "승차 정류장", "하차 정류장", localStationId = "206000629",
            lines = listOf(TransitLine("55", "241219006", 1010))
        )
        val row = GyeonggiBusArrival("206000629", "241219006", "RUN", "", "-1", "", "5", "", "6")

        val arrival = selectBusArrival(emptyList(), ride, listOf(row))
        assertEquals(300, arrival?.seconds)
        assertEquals(6, arrival?.stopsAway)
        assertNull(selectBusArrival(emptyList(), ride, listOf(row.copy(flag = "WAIT"))))
        assertEquals(true, selectBusArrival(emptyList(), ride, listOf(row.copy(flag = "STOP")))?.serviceEnded)
    }

    @Test
    fun marksBusServiceEndedOnlyWhenAllAlternativesConfirmIt() {
        val earlyBus = TransitLine("새벽A741", "100000028", 1000)
        val dayBus = TransitLine("470", "100100073", 1000)
        val ride = CommuteLeg(TransitMode.BUS, "강남역", "광화문역", lines = listOf(earlyBus))
        val ended = BusArrival(earlyBus.localId, null, "운행종료", "운행종료")
        val arriving = BusArrival(dayBus.localId, null, "4분후[1번째 전]", null)
        val withAlternative = ride.copy(lines = listOf(earlyBus, dayBus))

        assertEquals(true, selectBusArrival(listOf(ended), ride)?.serviceEnded)
        assertNull(selectBusArrival(listOf(ended), withAlternative))
        assertEquals("470", selectBusArrival(listOf(ended, arriving), withAlternative)?.line)
    }

    private fun train(
        direction: String = "내선",
        terminal: String = "홍대입구",
        code: String = "99",
        forecast: String = "120",
        message: String = "2번째 전",
        orderKey: String = "01002${terminal}0"
    ) =
        gson.fromJson(
            """{"statnNm":"강남","subwayId":"1002","updnLine":"$direction",
                "bstatnNm":"$terminal","trainLineNm":"${terminal}행 - 교대방면",
                "btrainSttus":"일반","recptnDt":"2026-11-22 17:26:10","barvlDt":"$forecast",
                "ordkey":"$orderKey","arvlMsg2":"$message","arvlCd":"$code"}""",
            RealtimeArrival::class.java
        )
}
