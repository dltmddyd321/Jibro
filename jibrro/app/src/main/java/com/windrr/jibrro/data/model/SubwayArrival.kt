package com.windrr.jibrro.data.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "realtimeStationArrival", strict = false)
data class SubwayArrivalResponse(
    @ElementList(name = "row", inline = true, required = false)
    var rows: List<SubwayArrival>? = null,

    @Element(name = "RESULT", required = false)
    var result: Result? = null
)

@Root(name = "row", strict = false)
data class SubwayArrival(

    @Element(name = "rowNum", required = false)
    var rowNum: Int = 0,

    @Element(name = "selectedCount", required = false)
    var selectedCount: Int = 0,

    @Element(name = "totalCount", required = false)
    var totalCount: Int = 0,

    @Element(name = "subwayId", required = false)
    var subwayId: Int = 0,

    @Element(name = "updnLine", required = false)
    var updnLine: String = "",

    @Element(name = "trainLineNm", required = false)
    var trainLineNm: String = "",

    @Element(name = "statnFid", required = false)
    var statnFid: Long = 0,

    @Element(name = "statnTid", required = false)
    var statnTid: Long = 0,

    @Element(name = "statnId", required = false)
    var statnId: Long = 0,

    @Element(name = "statnNm", required = false)
    var statnNm: String = "",

    @Element(name = "trnsitCo", required = false)
    var trnsitCo: Int = 0,

    @Element(name = "ordkey", required = false)
    var ordkey: String = "",

    @Element(name = "subwayList", required = false)
    var subwayList: String = "",

    @Element(name = "statnList", required = false)
    var statnList: String = "",

    @Element(name = "btrainSttus", required = false)
    var btrainSttus: String = "",

    @Element(name = "barvlDt", required = false)
    var barvlDt: Int = 0,

    @Element(name = "btrainNo", required = false)
    var btrainNo: String = "",

    @Element(name = "bstatnId", required = false)
    var bstatnId: Long = 0,

    @Element(name = "bstatnNm", required = false)
    var bstatnNm: String = "",

    @Element(name = "recptnDt", required = false)
    var recptnDt: String = "",

    @Element(name = "arvlMsg2", required = false)
    var arvlMsg2: String = "",

    @Element(name = "arvlMsg3", required = false)
    var arvlMsg3: String = "",

    @Element(name = "arvlCd", required = false)
    var arvlCd: Int = 0,

    @Element(name = "lstcarAt", required = false)
    var lstcarAt: Int = 0
)

@Root(name = "RESULT", strict = false)
data class Result(
    @Element(name = "code")
    var code: String = "",

    @Element(name = "message")
    var message: String = "",

    @Element(name = "status")
    var status: String = ""
)

