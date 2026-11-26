package com.windrr.jibrro.presentation.widget

import com.google.gson.Gson
import com.windrr.jibrro.data.model.RealtimeArrival
import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetArrivalsTest {
    private val now = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul")).apply {
        clear()
        set(2026, 10, 24, 19, 0, 0)
    }.timeInMillis

    @Test
    fun prioritizesTheNextTrainInEachDirectionAndDropsStaleOrDepartedRows() {
        val rows = listOf(
            arrival("상행", "80"),
            arrival("상행", "95"),
            arrival("하행", "120", lastTrain = "1"),
            arrival("하행", "300", time = "18:55:00"),
            arrival("하행", "5", code = "2")
        )

        val arrivals = widgetArrivals(rows, now)

        assertEquals(listOf("↑ 상행", "↓ 하행", "↑ 상행"), arrivals.map { it.direction })
        assertEquals(listOf("1분 10초", "1분 50초", "1분 25초"), arrivals.map { it.eta })
        assertEquals("2정거장 전", arrivals.first().stops)
        assertEquals(listOf(false, true, false), arrivals.map { it.lastTrain })
    }

    private fun arrival(
        direction: String,
        seconds: String,
        time: String = "18:59:50",
        code: String = "99",
        lastTrain: String = "0"
    ): RealtimeArrival = Gson().fromJson(
        """{
            "subwayId":"1001","updnLine":"$direction","bstatnNm":"서울","arvlMsg2":"2정거장 전",
            "arvlCd":"$code","barvlDt":"$seconds","recptnDt":"2026-11-24 $time","lstcarAt":"$lastTrain"
        }""",
        RealtimeArrival::class.java
    )
}
