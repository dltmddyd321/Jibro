package com.windrr.jibrro.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.presentation.activity.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.viewmodel.StationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikeStationActivity : ComponentActivity() {
    private val stationViewModel: StationViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationViewModel.fetchStationList()

        enableEdgeToEdge()
        setContent {
            JibrroTheme {

                val stationList by stationViewModel.allStation.collectAsState()
                val isLoading by stationViewModel.isLoading.collectAsState()

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
                            }
                        )
                    }
                ) { innerPadding ->
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        StationListScreen(
                            stations = stationList,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun StationListScreen(
        stations: List<SubwayStation>,
        modifier: Modifier = Modifier
    ) {
        val checkedStates = remember { mutableStateMapOf<String, Boolean>() }

        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(stations) { station ->
                val stationId = station.bldn_id
                val checked = checkedStates[stationId] ?: false

                StationListItem(
                    name = station.name,
                    line = station.line,
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        checkedStates[stationId] = isChecked
                    }
                )
            }
        }
    }

    @Composable
    fun StationListItem(
        name: String,
        line: String,
        checked: Boolean,
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
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = line,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}