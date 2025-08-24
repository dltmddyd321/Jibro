package com.windrr.jibrro.presentation.activity

import SubwayLineMap
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.windrr.jibrro.R
import com.windrr.jibrro.data.util.Result
import com.windrr.jibrro.infrastructure.LocationForegroundService
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.component.BannerAdView
import com.windrr.jibrro.presentation.component.LocationPermissionDialog
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.viewmodel.CheckStationViewModel
import com.windrr.jibrro.presentation.viewmodel.SettingsViewModel
import com.windrr.jibrro.presentation.viewmodel.StationViewModel
import com.windrr.jibrro.presentation.viewmodel.SubwayArrivalDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val stationViewModel: StationViewModel by viewModels()
    private val subwayArrivalViewModel: SubwayArrivalDataViewModel by viewModels()
    private val checkStationViewModel: CheckStationViewModel by viewModels()
    private var lat = 0.0
    private var lng = 0.0

    private fun isGetLocationPermission(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> true

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> false

            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lat = intent.getDoubleExtra("lat", 0.0)
        lng = intent.getDoubleExtra("lng", 0.0)
        stationViewModel.findClosestStation(lat, lng)

        val isTrackingDestination = settingsViewModel.destination.value != null
        if (isTrackingDestination) {
            val intent = Intent(applicationContext, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(applicationContext, intent)
        } else {
            val intent = Intent(applicationContext, LocationForegroundService::class.java)
            applicationContext.stopService(intent)
        }

        enableEdgeToEdge()
        setContent {
            JibrroTheme {

                val lifecycleOwner = LocalLifecycleOwner.current
                val stationName by stationViewModel.closestStation.collectAsState()
                val isSubwayLoading by stationViewModel.isLoading.collectAsState()
                var hasLocationPermission by remember { mutableStateOf(isGetLocationPermission()) }

                var currentLat by remember { mutableDoubleStateOf(lat) }
                var currentLng by remember { mutableDoubleStateOf(lng) }

                val arrivalMap by subwayArrivalViewModel.arrivalMap.collectAsState()
                val saveStationNameList by checkStationViewModel.checkStationList.map { list -> list.map { it.name } }
                    .collectAsState(initial = emptyList())
                val distinctFavorites = remember(saveStationNameList) {
                    saveStationNameList.distinct()
                }
                val filteredFavorites = remember(distinctFavorites, stationName) {
                    distinctFavorites.filter { it != stationName }
                }
                val sections = remember(stationName, filteredFavorites) {
                    buildList {
                        stationName?.let { add(it) }
                        addAll(filteredFavorites)
                    }.distinct()
                }

                Log.d("섹션 검사", "$sections")
                DisposableEffect(lifecycleOwner, stationName) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_START) {
                            checkStationViewModel.fetchCheckedStationList()
                            hasLocationPermission = isGetLocationPermission()
                            if (hasLocationPermission) {
                                if (currentLat == 0.0 && currentLng == 0.0) {
                                    locationHelper.getLastLocation(onSuccess = { latResult, lngResult ->
                                        currentLat = latResult
                                        currentLng = lngResult
                                        stationViewModel.findClosestStation(
                                            latResult, lngResult
                                        )
                                        stationName?.let { name ->
                                            val all =
                                                buildList { add(name); addAll(filteredFavorites) }
                                            subwayArrivalViewModel.refreshStations(all)
                                        }
                                    }, onFailure = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "위치 정보를 가져오는 데 실패했습니다",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                                } else {
                                    stationName?.let { name ->
                                        val all = buildList { add(name); addAll(filteredFavorites) }
                                        subwayArrivalViewModel.refreshStations(all)
                                    }
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(sections, hasLocationPermission) {
                    if (hasLocationPermission && sections.isNotEmpty()) {
                        subwayArrivalViewModel.refreshStations(sections)
                    }
                }

                if (!hasLocationPermission) {
                    LocationPermissionDialog(
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                            startActivity(intent)
                        })
                }

                MainDrawerScreen(filteredFavorites, isTrackingDestination, stationName) {
                    if (isSubwayLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            BannerAdView()
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            BannerAdView()
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn {
                                sections.forEachIndexed { index, sectionName ->
                                    item {
                                        if (index != 0) Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            text = sectionName,
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    }

                                    when (val state = arrivalMap[sectionName]) {
                                        null, is Result.Loading -> {
                                            item {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.padding(
                                                        8.dp
                                                    )
                                                )
                                            }
                                        }

                                        is Result.Error -> {
                                            item {
                                                Text(
                                                    text = state.message ?: "도착 정보 불러오기 실패",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Red,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }

                                        is Result.Success -> {
                                            val arrivalList = (state.data ?: emptyList())
                                                .sortedBy { SubwayLineMap.getNameById(it.subwayId) }

                                            if (arrivalList.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = "도착 예정 정보 없음",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.Red,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            } else {
                                                itemsIndexed(arrivalList) { _, arrival ->
                                                    SubwayArrivalItemCard(
                                                        updnLine = arrival.updnLine,
                                                        arvlMsg2 = arrival.arvlMsg2,
                                                        trainLineNm = arrival.trainLineNm,
                                                        lstcarAt = arrival.lstcarAt,
                                                        subwayId = arrival.subwayId
                                                    )
                                                    Divider()
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainDrawerScreen(
        filteredFavorites: List<String>,
        isTrackingDestination: Boolean,
        stationName: String?, content: @Composable () -> Unit
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        var backPressedOnce by remember { mutableStateOf(false) }

        BackHandler {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else {
                if (backPressedOnce) {
                    finish()
                } else {
                    backPressedOnce = true
                    Toast.makeText(context, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        delay(2000)
                        backPressedOnce = false
                    }
                }
            }
        }

        DismissibleNavigationDrawer(
            drawerState = drawerState, drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp * 2 / 3)
                ) {
                    Text(
                        "즐겨찾기", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                startActivity(
                                    Intent(
                                        this@MainActivity, LikeStationActivity::class.java
                                    )
                                )
                            }
                            .padding(16.dp))
                    Text(
                        "설정", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                startActivity(
                                    Intent(
                                        this@MainActivity, SettingsActivity::class.java
                                    )
                                )
                            }
                            .padding(16.dp))
                }
            }) {
            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Image(
                            painter = painterResource(id = R.drawable.jibro_text),
                            contentDescription = "Jibro",
                            modifier = Modifier.fillMaxSize()
                        )
                    }, navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "메뉴 열기")
                        }
                    }, actions = {
                        IconButton(onClick = {
                            stationName?.let { name ->
                                val all = buildList { add(name); addAll(filteredFavorites) }
                                subwayArrivalViewModel.refreshStations(all)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh, contentDescription = "새로고침"
                            )
                        }
                    })
                }, floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            val intent = Intent(context, LikeStationActivity::class.java).apply {
                                putExtra("startMode", "destination")
                            }
                            context.startActivity(intent)
                        },
                        containerColor = Color(0xFF76FF03), // 밝은 형광 노랑
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "목적지 추가",
                            tint = Color.Black
                        )
                    }
                }) { innerPadding ->
                val destinationName = settingsViewModel.destination.collectAsState().value?.name
                if (isTrackingDestination && destinationName != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF76FF03))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "$destinationName 으로 이동 중",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    @Composable
    fun SubwayArrivalItemCard(
        subwayId: String,
        updnLine: String,
        arvlMsg2: String,
        trainLineNm: String,
        lstcarAt: String,
        modifier: Modifier = Modifier
    ) {
        val surface = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        val onBg = MaterialTheme.colorScheme.onBackground
        val subtle = MaterialTheme.colorScheme.onSurfaceVariant
        val upColor = Color(0xFF1E88E5)
        val downColor = Color(0xFFD32F2F)
        val lineColor = if (updnLine == "상행") upColor else downColor

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LineChip(
                        text = SubwayLineMap.getNameById(subwayId),
                        textColor = lineColor
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = updnLine,
                        style = MaterialTheme.typography.labelMedium.copy(color = subtle)
                    )

                    Spacer(Modifier.weight(1f))

                    if (lstcarAt == "1") {
                        LastTrainBadge()
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = formatArrivalMessage(arvlMsg2),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = onBg
                    )
                )

                if (trainLineNm.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = trainLineNm,
                        style = MaterialTheme.typography.bodySmall.copy(color = subtle)
                    )
                }
            }
        }
    }

    @Composable
    private fun LineChip(
        text: String,
        textColor: Color,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = textColor.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }

    @Composable
    private fun LastTrainBadge(modifier: Modifier = Modifier) {
        val danger = Color(0xFFD32F2F)
        Box(
            modifier = modifier
                .background(
                    color = danger.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(6.dp)
                )
                .border(
                    width = 1.dp,
                    color = danger,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "막차",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = danger,
                    fontWeight = FontWeight.Bold
                )
            )
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