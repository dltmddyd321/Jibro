package com.windrr.jibrro.presentation.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.presentation.activity.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.viewmodel.CheckStationViewModel
import com.windrr.jibrro.presentation.viewmodel.SettingsViewModel
import com.windrr.jibrro.presentation.viewmodel.StationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikeStationActivity : ComponentActivity() {
    private val stationViewModel: StationViewModel by viewModels()
    private val checkStationViewModel: CheckStationViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val destinationMode = intent?.getStringExtra("startMode") == "destination"

        setContent {
            JibrroTheme {

                LaunchedEffect(Unit) {
                    checkStationViewModel.fetchCheckedStationList()
                }

                val checkedStations by checkStationViewModel.checkStationList.collectAsState()
                val checkedStates = remember(checkedStations) {
                    mutableStateMapOf<String, Boolean>().apply {
                        checkedStations.forEach { put(it.id, true) }
                    }
                }
                val stationList by stationViewModel.stationList.collectAsState()
                val isLoading by stationViewModel.isLoading.collectAsState()
                var searchQuery by remember { mutableStateOf("") }
                val focusManager = LocalFocusManager.current

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "즐겨찾기",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "뒤로 가기"
                                    )
                                }
                            },
                            actions = {
                                TextButton(onClick = {
                                    val checkedStationList = stationList.filter {
                                        checkedStates[it.bldn_id] == true
                                    }.map {
                                        CheckStation(
                                            id = it.bldn_id,
                                            name = it.name,
                                            line = it.line
                                        )
                                    }
                                    val uncheckedStationList = stationList.filter {
                                        checkedStates[it.bldn_id] != true
                                    }.map {
                                        CheckStation(
                                            id = it.bldn_id,
                                            name = it.name,
                                            line = it.line
                                        )
                                    }

                                    checkStationViewModel.saveCheckedStationList(checkedStationList)
                                    checkStationViewModel.clearCheckedStationList(
                                        uncheckedStationList
                                    )

                                    Toast.makeText(
                                        this@LikeStationActivity,
                                        "저장되었습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }) {
                                    Text(
                                        text = "저장",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("지하철역 검색") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        // 검색 버튼 누르면 실행
                                        stationViewModel.findStationByName(searchQuery)
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                            Button(onClick = {
                                stationViewModel.findStationByName(searchQuery)
                            }) {
                                Text("검색")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            StationListScreen(
                                checkedStates = checkedStates,
                                stations = stationList,
                                destinationMode = destinationMode,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StationListScreen(
        checkedStates: MutableMap<String, Boolean>,
        stations: List<SubwayStation>,
        destinationMode: Boolean,
        modifier: Modifier = Modifier
    ) {
        val destination by settingsViewModel.destination.collectAsState()
        var selectedDestinationId by remember { mutableStateOf(destination) }

        LaunchedEffect(destination) {
            selectedDestinationId = destination
        }

        val combinedLinesByName = remember(stations) {
            stations
                .groupBy { it.name }
                .mapValues { (_, list) ->
                    list.map { it.line }
                        .flatMap { it.split('/').map(String::trim) }
                        .distinct()
                        .joinToString(" / ")
                }
        }

        val distinctStations = remember(stations) {
            stations.distinctBy { it.name }
        }

        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(distinctStations) { station ->
                val stationId = station.bldn_id
                val displayLine = combinedLinesByName[station.name] ?: station.line

                if (destinationMode) {
                    val isSelected = stationId == selectedDestinationId
                    StationListItem(
                        name = station.name,
                        line = displayLine,
                        checked = isSelected,
                        destinationMode = true,
                        onCheckedChange = {
                            selectedDestinationId = stationId
                            settingsViewModel.setDestination(stationId)
                        }
                    )
                } else {
                    val isChecked = checkedStates[stationId] ?: false
                    StationListItem(
                        name = station.name,
                        line = displayLine,
                        checked = isChecked,
                        destinationMode = false,
                        onCheckedChange = { checkedStates[stationId] = it }
                    )
                }
            }
        }
    }

    @Composable
    fun StationListItem(
        name: String,
        line: String,
        checked: Boolean,
        destinationMode: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = line,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (destinationMode) {
                RadioButton(
                    selected = checked,
                    onClick = { onCheckedChange(true) }
                )
            } else {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}