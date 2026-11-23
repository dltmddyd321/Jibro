package com.windrr.jibrro.presentation.activity

import SubwayLineMap
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.windrr.jibrro.data.commute.subwayArrival
import com.windrr.jibrro.presentation.commute.HomeLocationPicker
import com.windrr.jibrro.presentation.commute.HomeRouteViewModel
import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.domain.state.Result
import com.windrr.jibrro.presentation.commute.ArrivalDisplay
import com.windrr.jibrro.presentation.commute.DirectionBadge
import com.windrr.jibrro.presentation.commute.TransitIcon
import com.windrr.jibrro.presentation.commute.homeRouteItems
import com.windrr.jibrro.infrastructure.LocationForegroundService
import com.windrr.jibrro.infrastructure.AlertFeature
import com.windrr.jibrro.infrastructure.alertPermissions
import com.windrr.jibrro.presentation.component.BannerAdView
import com.windrr.jibrro.presentation.component.DestinationBanner
import com.windrr.jibrro.presentation.component.JibrroDrawer
import com.windrr.jibrro.presentation.component.JibrroPermissionDialog
import com.windrr.jibrro.presentation.component.PermissionAction
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.viewmodel.CheckStationViewModel
import com.windrr.jibrro.presentation.viewmodel.SettingsViewModel
import com.windrr.jibrro.presentation.viewmodel.StationViewModel
import com.windrr.jibrro.presentation.viewmodel.SubwayArrivalDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val stationViewModel: StationViewModel by viewModels()
    private val subwayArrivalViewModel: SubwayArrivalDataViewModel by viewModels()
    private val checkStationViewModel: CheckStationViewModel by viewModels()
    private val homeRouteViewModel: HomeRouteViewModel by viewModels()

    private fun hasFineLocation() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasLocation() = hasFineLocation() || ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsViewModel.destination.collect { destination ->
                    val service = Intent(this@MainActivity, LocationForegroundService::class.java)
                    if (destination != null && alertPermissions(AlertFeature.DESTINATION).ready) {
                        try {
                            ContextCompat.startForegroundService(this@MainActivity, service)
                        } catch (_: SecurityException) {
                            stopService(service)
                        } catch (_: IllegalStateException) {
                            stopService(service)
                        }
                    } else {
                        stopService(service)
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            JibrroTheme {
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                val home by homeRouteViewModel.home.collectAsState()
                val origin by homeRouteViewModel.origin.collectAsState()
                val locating by homeRouteViewModel.locating.collectAsState()
                val routes by homeRouteViewModel.routes.collectAsState()
                val homeSearch by homeRouteViewModel.homeSearch.collectAsState()
                val stationName by stationViewModel.closestStation.collectAsState()
                val destination by settingsViewModel.destination.collectAsState()
                val arrivalMap by subwayArrivalViewModel.arrivalMap.collectAsState()
                val favorites by checkStationViewModel.checkStationList.collectAsState()

                var locationGranted by remember { mutableStateOf(hasLocation()) }
                var requestedLocation by rememberSaveable { mutableStateOf(false) }
                var showLocationGuide by rememberSaveable { mutableStateOf(false) }
                var showHomePicker by rememberSaveable { mutableStateOf(false) }
                val locationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    locationGranted = hasLocation()
                    homeRouteViewModel.updateOrigin(locationGranted)
                }

                val closest = stationName.takeIf { locationGranted && origin != null }
                val sections = remember(closest, favorites) {
                    (listOfNotNull(closest) + favorites.map { it.name }).distinct()
                }
                val refresh: () -> Unit = {
                    locationGranted = hasLocation()
                    homeRouteViewModel.updateOrigin(locationGranted, manual = true)
                    subwayArrivalViewModel.refreshStations(sections)
                }

                DisposableEffect(lifecycle) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                locationGranted = hasLocation()
                                homeRouteViewModel.setVisible(true)
                                homeRouteViewModel.updateOrigin(locationGranted)
                                checkStationViewModel.fetchCheckedStationList()
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                homeRouteViewModel.origin.value?.let {
                                    settingsViewModel.updateLocation(it.latitude, it.longitude)
                                }
                                homeRouteViewModel.setVisible(false)
                            }
                            else -> Unit
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose {
                        lifecycle.removeObserver(observer)
                        homeRouteViewModel.setVisible(false)
                    }
                }

                LaunchedEffect(origin) {
                    origin?.let { stationViewModel.findClosestStation(it.latitude, it.longitude) }
                }
                LaunchedEffect(lifecycle, sections) {
                    if (sections.isNotEmpty()) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            while (true) {
                                subwayArrivalViewModel.refreshStations(sections)
                                delay(45_000)
                            }
                        }
                    }
                }

                if (showLocationGuide && !locationGranted) {
                    val openSettings = requestedLocation && !shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    JibrroPermissionDialog(
                        title = "현재 위치 권한",
                        mode = null,
                        onDismiss = { showLocationGuide = false }
                    ) {
                        PermissionAction(
                            "위치",
                            "현재 위치에서 집으로 가는 경로와 가까운 역을 찾아요.",
                            if (openSettings) "휴대폰 설정 열기" else "위치 권한 허용"
                        ) {
                            showLocationGuide = false
                            if (openSettings) {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                })
                            } else {
                                requestedLocation = true
                                locationPermission.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }
                        }
                    }
                }

                MainDrawerScreen(destination, refresh) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        homeRouteItems(
                            home, locationGranted, origin, closest, locating, routes,
                            onEditHome = { showHomePicker = true },
                            onRequestLocation = { showLocationGuide = true },
                            onRefresh = refresh
                        )
                        sections.forEach { name ->
                            item(key = "station:$name") {
                                Column(Modifier.padding(top = 12.dp)) {
                                    Text(
                                        if (name == closest) "가까운 지하철역" else "즐겨찾기",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(name, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                            when (val state = arrivalMap[name]) {
                                null, is Result.Loading -> item {
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                                }
                                is Result.Error -> item {
                                    Text(
                                        state.message ?: "실시간 도착 정보를 불러오지 못했어요.",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                is Result.Success -> {
                                    val now = System.currentTimeMillis()
                                    val arrivals = state.data.orEmpty().mapNotNull { row ->
                                        subwayArrival(row, now)?.let { row to it }
                                    }.sortedBy { it.first.subwayId }
                                    if (arrivals.isEmpty()) item { Text("실시간 도착 정보 없음") }
                                    items(arrivals) { (row, arrival) ->
                                        SubwayArrivalItemCard(
                                            arrival = arrival,
                                            trainLineNm = row.trainLineNm,
                                            lstcarAt = row.lstcarAt,
                                            subwayId = row.subwayId
                                        )
                                    }
                                }
                            }
                        }
                        item { BannerAdView() }
                    }
                }

                if (showHomePicker) {
                    HomeLocationPicker(
                        state = homeSearch,
                        origin = origin,
                        onSearch = homeRouteViewModel::searchHomes,
                        onQueryChange = homeRouteViewModel::clearSearch,
                        onSave = homeRouteViewModel::saveHome,
                        onDismiss = {
                            showHomePicker = false
                            homeRouteViewModel.clearSearch()
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainDrawerScreen(
        destination: Destination?,
        onRefresh: () -> Unit,
        content: @Composable () -> Unit
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        var backPressedOnce by remember { mutableStateOf(false) }

        BackHandler {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else {
                if (backPressedOnce) {
                    finish()
                } else {
                    backPressedOnce = true
                    Toast.makeText(this@MainActivity, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        delay(2000)
                        backPressedOnce = false
                    }
                }
            }
        }

        fun openPage(intent: Intent) {
            scope.launch { drawerState.close() }
            startActivity(intent)
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                JibrroDrawer(
                    onClose = { scope.launch { drawerState.close() } },
                    onFavorites = { openPage(Intent(this, LikeStationActivity::class.java)) },
                    onDestination = {
                        openPage(Intent(this, LikeStationActivity::class.java).putExtra("startMode", "destination"))
                    },
                    onSettings = { openPage(Intent(this, SettingsActivity::class.java)) }
                )
            }
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                        title = {
                            Text(
                                "Jibro",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "메뉴 열기")
                            }
                        },
                        actions = {
                            IconButton(onClick = onRefresh) {
                                Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                            }
                        }
                    )
                }) { innerPadding ->


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    destination?.name?.let { name ->
                        DestinationBanner(destinationName = name) {
                            startActivity(Intent(this@MainActivity, LikeStationActivity::class.java).apply {
                                putExtra("startMode", "destination")
                            })
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    private fun SubwayArrivalItemCard(
        subwayId: String,
        arrival: BoardingArrival,
        trainLineNm: String,
        lstcarAt: String
    ) {
        val colors = MaterialTheme.colorScheme
        Surface(shape = RoundedCornerShape(17.dp), color = colors.surface) {
            Column(
                Modifier.fillMaxWidth().padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        Modifier.size(32.dp).background(colors.primaryContainer, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        TransitIcon(TransitMode.SUBWAY, colors.onPrimaryContainer, Modifier.size(19.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            SubwayLineMap.getNameById(subwayId).ifBlank { "지하철" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DirectionBadge(arrival.direction)
                    if (lstcarAt == "1") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.errorContainer,
                            contentColor = colors.onErrorContainer
                        ) {
                            Text("막차", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                ArrivalDisplay(arrival, hint = trainLineNm)
            }
        }
    }

}

fun formatArrivalMessage(raw: String): String {
    var result = raw

    val bracketPattern = Regex("""\[(\d+)]""")
    result = result.replace(bracketPattern) { it.groupValues[1] }

    if (result.trim().endsWith("후")) {
        result += " 도착 예정"
    }

    return result
}
