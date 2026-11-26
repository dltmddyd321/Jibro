package com.windrr.jibrro.presentation.commute

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.commute.CommuteRepository
import com.windrr.jibrro.data.commute.RouteSearch
import com.windrr.jibrro.data.repository.datasource.SettingsLocalDataSource
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteRoute
import com.windrr.jibrro.domain.commute.Coordinates
import com.windrr.jibrro.domain.commute.HomeLocation
import com.windrr.jibrro.domain.repository.StationRepository
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.util.distanceInMeters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface CommuteUiState {
    data object Waiting : CommuteUiState
    data object Loading : CommuteUiState
    data object MissingKey : CommuteUiState
    data object NearHome : CommuteUiState
    data class Error(val message: String) : CommuteUiState
    data class Empty(val message: String) : CommuteUiState
    data class Ready(
        val routes: List<CommuteRoute>,
        val arrivals: Map<CommuteRoute, BoardingArrival?> = emptyMap(),
        val loadingArrivals: Boolean = true
    ) : CommuteUiState
}

data class HomeSearchState(
    val entries: List<HomeLocation> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class HomeRouteViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locations: LocationHelper,
    private val settings: SettingsLocalDataSource,
    private val stations: StationRepository,
    private val repository: CommuteRepository
) : ViewModel() {
    val home = settings.homeLocation.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _origin = MutableStateFlow<Coordinates?>(null)
    val origin = _origin.asStateFlow()
    private val _locating = MutableStateFlow(false)
    val locating = _locating.asStateFlow()
    private val _routes = MutableStateFlow<CommuteUiState>(CommuteUiState.Waiting)
    val routes = _routes.asStateFlow()
    private val _homeSearch = MutableStateFlow(HomeSearchState())
    val homeSearch = _homeSearch.asStateFlow()

    private val visible = MutableStateFlow(false)
    private val refresh = MutableStateFlow(0)
    private var locationJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(home, origin, refresh) { home, origin, _ -> home to origin }
                .collectLatest { (home, origin) ->
                    if (home == null || origin == null) {
                        _routes.value = CommuteUiState.Waiting
                        return@collectLatest
                    }
                    _routes.value = CommuteUiState.Loading
                    val result = try {
                        repository.findRoutes(origin, home.point)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        RouteSearch.Error("경로를 불러오지 못했어요. 연결 상태를 확인해 주세요.")
                    }
                    _routes.value = when (result) {
                        RouteSearch.MissingKey -> CommuteUiState.MissingKey
                        RouteSearch.NearHome -> CommuteUiState.NearHome
                        is RouteSearch.Error -> CommuteUiState.Error(result.message)
                        is RouteSearch.Empty -> CommuteUiState.Empty(result.message)
                        is RouteSearch.Found -> CommuteUiState.Ready(result.routes)
                    }
                    if (result is RouteSearch.Found) {
                        visible.collectLatest { active ->
                            if (active) {
                                while (true) {
                                    val current = _routes.value
                                    if (current is CommuteUiState.Ready && !current.loadingArrivals) {
                                        _routes.value = current.copy(loadingArrivals = true)
                                    }
                                    val arrivals = try {
                                        repository.firstArrivals(result.routes)
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (_: Exception) {
                                        emptyMap()
                                    }
                                    _routes.value = CommuteUiState.Ready(result.routes, arrivals, loadingArrivals = false)
                                    delay(45_000)
                                }
                            }
                        }
                    }
                }
        }
    }

    fun setVisible(active: Boolean) {
        visible.value = active
        if (!active) {
            locationJob?.cancel()
            _locating.value = false
        }
    }

    fun updateOrigin(hasPermission: Boolean, manual: Boolean = false) {
        locationJob?.cancel()
        if (!hasPermission) {
            _origin.value = null
            _locating.value = false
            return
        }
        locationJob = viewModelScope.launch {
            _locating.value = true
            try {
                val current = withTimeoutOrNull(15_000) { locations.currentCoordinates() }
                    ?.takeIf { it.isValid }
                val previous = _origin.value
                when {
                    current == null -> _origin.value = null
                    manual && previous == current -> refresh.value++
                    manual || previous == null || distanceInMeters(
                        previous.latitude, previous.longitude, current.latitude, current.longitude
                    ) >= 50 -> _origin.value = current
                }
            } finally {
                _locating.value = false
            }
        }
    }

    fun searchHomes(input: String) {
        searchJob?.cancel()
        val query = input.trim()
        if (query.isEmpty()) {
            _homeSearch.value = HomeSearchState(message = "주소나 지하철역 이름을 입력해 주세요.")
            return
        }
        searchJob = viewModelScope.launch {
            _homeSearch.value = HomeSearchState(loading = true)
            val local = withContext(Dispatchers.IO) {
                val stationQuery = query.removeSuffix("역").takeIf { it.isNotBlank() } ?: query
                stations.getSubwayStations()
                    .filter {
                        it.name.contains(stationQuery, ignoreCase = true) ||
                            it.station_nm_eng.orEmpty().contains(stationQuery, ignoreCase = true)
                    }
                    .sortedByDescending {
                        it.name.equals(stationQuery, ignoreCase = true) ||
                            it.station_nm_eng.equals(stationQuery, ignoreCase = true)
                    }
                    .map { HomeLocation("${it.name}역 · ${it.line}", Coordinates(it.lat, it.lng)) }
                    .filter { it.point.isValid }
                    .distinct()
                    .take(5)
            }
            _homeSearch.value = HomeSearchState(local, loading = true)
            val addresses = try {
                withTimeoutOrNull(5_000) { geocode(query) }
            } catch (_: IOException) {
                null
            }
            val results = (addresses.orEmpty().mapNotNull { address ->
                if (!address.hasLatitude() || !address.hasLongitude()) return@mapNotNull null
                val point = Coordinates(address.latitude, address.longitude)
                val label = (address.getAddressLine(0) ?: address.featureName)?.takeIf { it.isNotBlank() }
                if (address.countryCode != "KR" || !point.isValid || label == null) null else HomeLocation(label, point)
            } + local).distinctBy { it.point }

            _homeSearch.value = HomeSearchState(
                entries = results,
                message = when {
                    results.isNotEmpty() -> null
                    addresses == null -> "주소 검색을 사용할 수 없어요. 가까운 지하철역 이름으로 검색해 보세요."
                    else -> "검색 결과가 없어요. 도로명과 건물번호 또는 지하철역 이름을 입력해 보세요."
                }
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _homeSearch.value = HomeSearchState()
    }

    suspend fun saveHome(home: HomeLocation): Boolean = try {
        settings.setHomeLocation(home)
        true
    } catch (_: IOException) {
        false
    }

    @Suppress("DEPRECATION")
    private suspend fun geocode(query: String): List<Address>? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.KOREA)
        if (Build.VERSION.SDK_INT < 33) {
            return withContext(Dispatchers.IO) { geocoder.getFromLocationName(query, 5).orEmpty() }
        }
        return suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (continuation.isActive) continuation.resume(addresses)
                }

                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) continuation.resumeWithException(IOException())
                }
            })
        }
    }
}
