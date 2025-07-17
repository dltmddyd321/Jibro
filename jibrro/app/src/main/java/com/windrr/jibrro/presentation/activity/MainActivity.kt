package com.windrr.jibrro.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissibleNavigationDrawer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.data.util.Result
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.viewmodel.StationViewModel
import com.windrr.jibrro.presentation.viewmodel.SubwayArrivalDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val stationViewModel: StationViewModel by viewModels()
    private val subwayArrivalViewModel: SubwayArrivalDataViewModel by viewModels()
    private var lat = 0.0
    private var lng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lat = intent.getDoubleExtra("lat", 0.0)
        lng = intent.getDoubleExtra("lng", 0.0)
        stationViewModel.findClosestStation(lat, lng)

        enableEdgeToEdge()
        setContent {
            JibrroTheme {

                val stationName by stationViewModel.closestStation.collectAsState()
                val isSubwayLoading by stationViewModel.isLoading.collectAsState()
                val arrivalState by subwayArrivalViewModel.arrivalState.collectAsState()

                LaunchedEffect(stationName) {
                    stationName?.let { subwayArrivalViewModel.getSubwayArrival(it) }
                }

                MainDrawerScreen {
                    if (isSubwayLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            text = stationName ?: "지하철역을 찾을 수 없습니다.",
                            style = MaterialTheme.typography.titleLarge
                        )

                        when (arrivalState) {
                            is Result.Success -> {
                                val arrival = arrivalState.data?.firstOrNull()
                                Text(
                                    text = arrival?.toString() ?: "도착 정보 없음",
                                    style = MaterialTheme.typography.bodyMedium
                                )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainDrawerScreen(
        content: @Composable () -> Unit
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
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
                            }
                            .padding(16.dp))
                }
            }) {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("드로어 예제") }, navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "메뉴 열기")
                        }
                    })
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
}