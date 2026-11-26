package com.windrr.jibrro.data.commute

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface RouteApi {
    @GET("v1/api/searchPubTransPathT")
    suspend fun routes(
        @Query("apiKey") key: String,
        @Query("SX") originLongitude: Double,
        @Query("SY") originLatitude: Double,
        @Query("EX") homeLongitude: Double,
        @Query("EY") homeLatitude: Double,
        @Query("SearchType") searchType: Int = 0,
        @Query("lang") language: Int = 0
    ): Response<RouteResponse>
}

internal data class RouteResponse(
    val result: RouteResult?,
    @field:JsonAdapter(RouteErrorAdapter::class) val error: RouteError?
)

internal data class RouteError(
    val code: String?,
    @SerializedName(value = "message", alternate = ["msg"]) val message: String?
)

internal class RouteErrorAdapter : JsonDeserializer<RouteError> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RouteError? {
        val error = when {
            json.isJsonObject -> json
            json.isJsonArray -> json.asJsonArray.firstOrNull { it.isJsonObject }
            else -> null
        } ?: return null
        return context.deserialize(error, typeOfT)
    }
}

internal data class RouteResult(val searchType: Int?, val path: List<RoutePath>?)
internal data class RoutePath(val info: RouteInfo?, val subPath: List<RouteLeg>?)
internal data class RouteInfo(val totalTime: Int?)
internal data class RouteLeg(
    val trafficType: Int?,
    val sectionTime: Int?,
    val lane: List<RouteLane>?,
    val startName: String?,
    val endName: String?,
    val way: String?,
    val wayCode: Int?,
    val startArsID: String?,
    val startLocalStationID: String?,
    val passStopList: PassStops?
)
internal data class RouteLane(val name: String?, val busNo: String?, val busLocalBlID: String?, val busCityCode: Int?)
internal data class PassStops(val stations: List<RouteStation>?)
internal data class RouteStation(val stationName: String?)

internal interface SeoulBusApi {
    @GET("api/rest/stationinfo/getStationByUid")
    suspend fun arrivals(
        @Query("serviceKey") key: String,
        @Query("arsId") arsId: String,
        @Query("resultType") resultType: String = "json"
    ): Response<BusResponse>
}

internal data class BusResponse(val msgHeader: BusHeader?, val msgBody: BusBody?)
internal data class BusHeader(val headerCd: String?)
internal data class BusBody(@SerializedName("itemList") val arrivals: List<BusArrival>?)
internal data class BusArrival(
    val busRouteId: String?,
    val stId: String?,
    val arrmsg1: String?,
    val arrmsg2: String?
)

internal interface GyeonggiBusApi {
    @GET("6410000/busarrivalservice/v2/getBusArrivalListv2")
    suspend fun arrivals(
        @Query("serviceKey") key: String,
        @Query("stationId") stationId: String,
        @Query("format") format: String = "json"
    ): Response<GyeonggiBusEnvelope>
}

internal data class GyeonggiBusEnvelope(val response: GyeonggiBusResponse?)
internal data class GyeonggiBusResponse(val msgHeader: GyeonggiBusHeader?, val msgBody: GyeonggiBusBody?)
internal data class GyeonggiBusHeader(val resultCode: Int?)
internal data class GyeonggiBusBody(val busArrivalList: List<GyeonggiBusArrival>?)
internal data class GyeonggiBusArrival(
    val stationId: String?,
    val routeId: String?,
    val flag: String?,
    // The API sends numbers for available values and empty strings for missing ones.
    val predictTimeSec1: String?,
    val predictTimeSec2: String?,
    val predictTime1: String?,
    val predictTime2: String?,
    val locationNo1: String?,
    val locationNo2: String?
)
