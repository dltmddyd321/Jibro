import com.windrr.jibrro.data.model.SubwayInfo

object SubwayLineMap {

    private val subwayList = listOf(
        SubwayInfo("1001", "1호선"),
        SubwayInfo("1002", "2호선"),
        SubwayInfo("1003", "3호선"),
        SubwayInfo("1004", "4호선"),
        SubwayInfo("1005", "5호선"),
        SubwayInfo("1006", "6호선"),
        SubwayInfo("1007", "7호선"),
        SubwayInfo("1008", "8호선"),
        SubwayInfo("1009", "9호선"),
        SubwayInfo("1063", "경의중앙선"),
        SubwayInfo("1065", "공항철도"),
        SubwayInfo("1067", "경춘선"),
        SubwayInfo("1075", "수인분당선"),
        SubwayInfo("1077", "신분당선"),
        SubwayInfo("1079", "의정부경전철"),
        SubwayInfo("1081", "경강선"),
        SubwayInfo("1082", "용인 에버라인"),
        SubwayInfo("1083", "우이신설선"),
        SubwayInfo("1084", "서해선"),
        SubwayInfo("1091", "인천 1호선"),
        SubwayInfo("1092", "인천 2호선"),
        SubwayInfo("1093", "인천공항 자기부상"),
        SubwayInfo("1094", "김포골드라인"),
        SubwayInfo("1095", "용인 에버라인"),
        SubwayInfo("1096", "부산 1호선"),
        SubwayInfo("1097", "부산 2호선"),
        SubwayInfo("1098", "부산 3호선"),
        SubwayInfo("1099", "부산 4호선")
    )

    private val idToNameMap = subwayList.associate { it.id to it.name }

    fun getNameById(id: String): String = idToNameMap[id] ?: ""
}
