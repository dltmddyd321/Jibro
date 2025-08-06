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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.windrr.jibrro.R
import com.windrr.jibrro.data.util.Result
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.component.BannerAdView
import com.windrr.jibrro.presentation.component.LocationPermissionDialog
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.viewmodel.StationViewModel
import com.windrr.jibrro.presentation.viewmodel.SubwayArrivalDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper
    private val stationViewModel: StationViewModel by viewModels()
    private val subwayArrivalViewModel: SubwayArrivalDataViewModel by viewModels()
    private var lat = 0.0
    private var lng = 0.0

    private fun isGetLocationPermission(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
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

        enableEdgeToEdge()
        setContent {
            JibrroTheme {

                val lifecycleOwner = LocalLifecycleOwner.current
                val stationName by stationViewModel.closestStation.collectAsState()
                val isSubwayLoading by stationViewModel.isLoading.collectAsState()
                val arrivalState by subwayArrivalViewModel.arrivalState.collectAsState()
                var hasLocationPermission by remember { mutableStateOf(isGetLocationPermission()) }

                var currentLat by remember { mutableDoubleStateOf(lat) }
                var currentLng by remember { mutableDoubleStateOf(lng) }

                DisposableEffect(lifecycleOwner, stationName) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_START) {
                            hasLocationPermission = isGetLocationPermission()
                            if (hasLocationPermission) {
                                if (currentLat == 0.0 && currentLng == 0.0) {
                                    locationHelper.getLastLocation(
                                        onSuccess = { latResult, lngResult ->
                                            currentLat = latResult
                                            currentLng = lngResult
                                            stationViewModel.findClosestStation(
                                                latResult,
                                                lngResult
                                            )
                                            stationName?.let { subwayArrivalViewModel.getSubwayArrival(it) }
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "위치 정보를 가져오는 데 실패했습니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                } else {
                                    stationName?.let { subwayArrivalViewModel.getSubwayArrival(it) }
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
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
                        }
                    )
                }

                MainDrawerScreen(stationName) {
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

                            Text(
                                text = stationName ?: "지하철역을 찾을 수 없습니다.",
                                style = MaterialTheme.typography.titleLarge
                            )

                            when (arrivalState) {
                                is Result.Success -> {
                                    val arrivalList = (arrivalState.data ?: emptyList()).sortedBy {
                                        SubwayLineMap.getNameById(it.subwayId)
                                    }
                                    if (arrivalList.isEmpty()) {
                                        Text(
                                            text = "도착 예정 정보 없음",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Red
                                        )
                                    } else {
                                        LazyColumn {
                                            items(arrivalList) { arrival ->
                                                SubwayArrivalItem(
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

                                is Result.Error -> {
                                    Text(
                                        text = arrivalState.message ?: "도착 정보 불러오기 실패",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Red
                                    )
                                }

                                is Result.Loading -> {
                                    CircularProgressIndicator()
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
        stationName: String?,
        content: @Composable () -> Unit
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
                                        this@MainActivity,
                                        LikeStationActivity::class.java
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
                                        this@MainActivity,
                                        SettingsActivity::class.java
                                    )
                                )
                            }
                            .padding(16.dp))
                }
            }) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Image(
                                painter = painterResource(id = R.drawable.jibro_text),
                                contentDescription = "Jibro",
                                modifier = Modifier.fillMaxSize()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "메뉴 열기")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                stationName?.let {
                                    subwayArrivalViewModel.getSubwayArrival(it)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "새로고침"
                                )
                            }
                        }
                    )
                }) { innerPadding ->
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
    fun SubwayArrivalItem(
        subwayId: String,
        updnLine: String,
        arvlMsg2: String,
        trainLineNm: String,
        lstcarAt: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = SubwayLineMap.getNameById(subwayId),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val iconRes = if (updnLine == "상행") R.drawable.up_line else R.drawable.down_line
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "$updnLine 아이콘",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 12.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatArrivalMessage(arvlMsg2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = trainLineNm,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (lstcarAt == "1") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .border(2.dp, Color.Red, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "막차",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    private fun formatArrivalMessage(raw: String): String {
        var result = raw

        // 1. "[숫자]" 형식 괄호 제거
        val bracketPattern = Regex("""\[(\d+)]""")
        result = result.replace(bracketPattern) { it.groupValues[1] }

        // 2. " 후"로 끝나면 "도착 예정" 붙이기
        if (result.trim().endsWith("후")) {
            result += " 도착 예정"
        }

        return result
    }
}