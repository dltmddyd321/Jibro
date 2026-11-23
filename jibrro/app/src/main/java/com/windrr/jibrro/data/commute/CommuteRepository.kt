package com.windrr.jibrro.data.commute

import com.windrr.jibrro.BuildConfig
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteRoute
import com.windrr.jibrro.domain.commute.Coordinates
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import com.windrr.jibrro.util.distanceInMeters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RouteSearch {
    data object MissingKey : RouteSearch
    data object NearHome : RouteSearch
    data class Empty(val message: String) : RouteSearch
    data class Error(val message: String) : RouteSearch
    data class Found(val routes: List<CommuteRoute>) : RouteSearch
}

@Singleton
class CommuteRepository @Inject constructor(
    client: OkHttpClient,
    private val subwayArrivals: GetSubwayArrivalDataUseCase
) {
    private val commuteClient = client.newBuilder().callTimeout(12, TimeUnit.SECONDS).build()
    private val routes = retrofit("https://api.odsay.com/").create(RouteApi::class.java)
    private val buses = retrofit("http://ws.bus.go.kr/").create(SeoulBusApi::class.java)
    private val gyeonggiBuses = retrofit("https://apis.data.go.kr/").create(GyeonggiBusApi::class.java)

    suspend fun findRoutes(origin: Coordinates, home: Coordinates): RouteSearch {
        if (distanceInMeters(origin.latitude, origin.longitude, home.latitude, home.longitude) <= 100.0) {
            return RouteSearch.NearHome
        }
        val key = BuildConfig.ODSAY_API_KEY
        if (key.isBlank()) return RouteSearch.MissingKey
        val response = routes.routes(key, origin.longitude, origin.latitude, home.longitude, home.latitude)
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return RouteSearch.Error("경로 서비스에 연결할 수 없어요.")
        }
        val error = body.error
        if (error != null) return when (error.code) {
            "-98" -> RouteSearch.NearHome
            "3", "4", "5", "6", "-99" -> RouteSearch.Empty("이 위치에서 집으로 가는 대중교통 경로를 찾지 못했어요.")
            else -> RouteSearch.Error(
                if (error.message.orEmpty().contains("ApiKey", ignoreCase = true))
                    "경로 서비스 인증을 확인해 주세요."
                else "경로를 불러오지 못했어요. 잠시 후 다시 시도해 주세요."
            )
        }
        val result = body.result ?: return RouteSearch.Error("경로 데이터 형식을 확인할 수 없어요.")
        if (result.searchType != 0) {
            return RouteSearch.Empty("현재 시내 대중교통 경로를 안내해요.")
        }
        val candidates = result.path.orEmpty().mapNotNull { it.toRoute() }
        if (candidates.isEmpty()) return RouteSearch.Empty("이 위치에서 집으로 가는 대중교통 경로를 찾지 못했어요.")
        return RouteSearch.Found(rankRoutes(candidates))
    }

    suspend fun firstArrivals(routes: List<CommuteRoute>): Map<CommuteRoute, BoardingArrival?> = coroutineScope {
        val rides = routes.mapNotNull { it.firstRide }
        val requests = Semaphore(4)
        val subway = rides.filter { it.mode == TransitMode.SUBWAY }.map { it.start }.distinct()
            .associateWith { station ->
                async {
                    requests.withPermit {
                        val query = if (station == "서울역") "서울" else station
                        available { subwayArrivals.execute(statnNm = query).data }.orEmpty()
                    }
                }
            }
        val busRides = rides.filter { it.mode == TransitMode.BUS }
        val bus = busRides.filter { it.lines.any { line -> line.cityCode == SEOUL_BUS_CITY } }
            .mapNotNull { it.stationArsId }
            .filter { it.matches(Regex("[0-9]{5}")) && it != "00000" }.distinct()
            .associateWith { arsId -> async { requests.withPermit { available { busRows(arsId) }.orEmpty() } } }
        val gyeonggiBus = busRides.filter { it.servesGyeonggiBus() }
            .mapNotNull { it.gyeonggiStationId() }.distinct()
            .associateWith { stationId ->
                async { requests.withPermit { available { gyeonggiBusRows(stationId) }.orEmpty() } }
            }

        routes.associateWith { route ->
            val ride = route.firstRide ?: return@associateWith null
            when (ride.mode) {
                TransitMode.SUBWAY -> selectSubwayArrival(
                    subway[ride.start]?.await().orEmpty(), ride, System.currentTimeMillis()
                )
                TransitMode.BUS -> selectBusArrival(
                    bus[ride.stationArsId]?.await().orEmpty(), ride,
                    gyeonggiBus[ride.gyeonggiStationId()]?.await().orEmpty()
                )
            }
        }
    }

    private fun retrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(commuteClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private suspend fun busRows(arsId: String): List<BusArrival> {
        val key = BuildConfig.SEOUL_BUS_API_KEY
        if (key.isBlank()) return emptyList()
        val response = buses.arrivals(key, arsId)
        val body = response.body() ?: return emptyList()
        return if (response.isSuccessful && body.msgHeader?.headerCd == "0") {
            body.msgBody?.arrivals.orEmpty()
        } else emptyList()
    }

    private suspend fun gyeonggiBusRows(stationId: String): List<GyeonggiBusArrival> {
        val key = BuildConfig.GYEONGGI_BUS_API_KEY.ifBlank { BuildConfig.SEOUL_BUS_API_KEY }
        if (key.isBlank()) return emptyList()
        val response = gyeonggiBuses.arrivals(key, stationId)
        val body = response.body()?.response
        return if (response.isSuccessful && body?.msgHeader?.resultCode == 0) {
            body.msgBody?.busArrivalList.orEmpty()
        } else emptyList()
    }

    private suspend fun <T> available(block: suspend () -> T): T? = try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }
}
