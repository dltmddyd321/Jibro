package com.windrr.jibrro.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.jibrro.R
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

                MainDrawerScreen(stationName) {
                    if (isSubwayLoading) {
                        CircularProgressIndicator()
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stationName ?: "지하철역을 찾을 수 없습니다.",
                                style = MaterialTheme.typography.titleLarge
                            )

                            when (arrivalState) {
                                is Result.Success -> {
                                    val arrivalList = arrivalState.data ?: emptyList()
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
                                                    lstcarAt = arrival.lstcarAt
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
                                // 동기화 동작 수행
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
}

@Composable
fun SubwayArrivalItem(
    updnLine: String,
    arvlMsg2: String,
    trainLineNm: String,
    lstcarAt: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = arvlMsg2,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (lstcarAt == "1") {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
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
            Text(
                text = trainLineNm,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}