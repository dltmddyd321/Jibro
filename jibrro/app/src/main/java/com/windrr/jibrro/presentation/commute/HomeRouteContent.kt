package com.windrr.jibrro.presentation.commute

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.R
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteRoute
import com.windrr.jibrro.domain.commute.Coordinates
import com.windrr.jibrro.domain.commute.HomeLocation
import com.windrr.jibrro.domain.commute.TransitMode
import kotlinx.coroutines.launch

fun LazyListScope.homeRouteItems(
    home: HomeLocation?,
    hasPermission: Boolean,
    origin: Coordinates?,
    nearestStation: String?,
    locating: Boolean,
    state: CommuteUiState,
    onEditHome: () -> Unit,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit
) {
    item(key = "commute-home") {
        HomeHero(home, hasPermission, origin, nearestStation, locating, onEditHome, onRequestLocation, onRefresh)
    }
    if (home == null || !hasPermission || origin == null) return

    when (state) {
        CommuteUiState.Waiting, CommuteUiState.Loading -> item(key = "commute-status") {
            RouteMessage("집으로 가는 경로를 찾는 중이에요.", loading = true)
        }
        CommuteUiState.MissingKey -> item(key = "commute-status") {
            RouteMessage("경로 서비스를 설정하면 추천 경로를 볼 수 있어요.")
        }
        CommuteUiState.NearHome -> item(key = "commute-status") {
            RouteMessage("집 근처예요. 가까운 거리에서는 대중교통 경로가 제공되지 않아요.")
        }
        is CommuteUiState.Error -> item(key = "commute-status") { RouteMessage(state.message, onRetry = onRefresh) }
        is CommuteUiState.Empty -> item(key = "commute-status") { RouteMessage(state.message, onRetry = onRefresh) }
        is CommuteUiState.Ready -> {
            item(key = "commute-heading") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "대중교통 비교",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "예상 소요시간순",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            val ordered = state.routes.sortedBy { state.arrivals[it]?.serviceEnded == true }
            itemsIndexed(ordered) { index, route ->
                val arrival = state.arrivals[route]
                RouteCard(
                    route,
                    isFastest = index == 0 && !state.loadingArrivals && arrival?.serviceEnded != true,
                    arrival = arrival,
                    loadingArrival = state.loadingArrivals
                )
            }
            item(key = "commute-attribution") {
                Text(
                    "경로 · ODsay LAB   실시간 · 서울시·경기도",
                    Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeHero(
    home: HomeLocation?,
    hasPermission: Boolean,
    origin: Coordinates?,
    nearestStation: String?,
    locating: Boolean,
    onEditHome: () -> Unit,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit
) {
    val mint = Color(0xFFB7F0D8)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF164D3E),
        contentColor = Color(0xFFFFFAE8)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 19.dp, end = 14.dp, top = 13.dp, bottom = 11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("집으로", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    if (home == null) {
                        Text("집을 설정하면 귀가 교통편을\n한눈에 볼 수 있어요.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(home.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (nearestStation != null && hasPermission && !locating) {
                                TransitIcon(TransitMode.SUBWAY, mint, Modifier.size(14.dp))
                            }
                            Text(
                                when {
                                    !hasPermission -> "현재 위치 권한이 필요해요."
                                    locating -> "현재 위치 확인 중"
                                    origin == null -> "현재 위치를 확인할 수 없어요."
                                    nearestStation != null -> "가까운 역 · ${nearestStation}역"
                                    else -> "현재 위치에서 출발"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = mint
                            )
                        }
                    }
                }
                Image(
                    painter = painterResource(R.drawable.jibro_train_cat),
                    contentDescription = null,
                    modifier = Modifier.weight(0.58f).heightIn(max = 130.dp).padding(start = 3.dp)
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val buttonColors = ButtonDefaults.buttonColors(containerColor = mint, contentColor = Color(0xFF133E32))
                val textColors = ButtonDefaults.textButtonColors(contentColor = mint)
                if (home == null) {
                    Button(onClick = onEditHome, colors = buttonColors) { Text("집 설정") }
                    if (!hasPermission) {
                        TextButton(onClick = onRequestLocation, colors = textColors) { Text("위치 권한 허용") }
                    }
                } else {
                    if (!hasPermission) {
                        Button(onClick = onRequestLocation, colors = buttonColors) { Text("위치 권한 허용") }
                    } else if (origin == null && !locating) {
                        Button(onClick = onRefresh, colors = buttonColors) { Text("위치 다시 확인") }
                    }
                    TextButton(onClick = onEditHome, colors = textColors) { Text("집 변경") }
                }
            }
        }
    }
}

@Composable
private fun RouteMessage(text: String, onRetry: (() -> Unit)? = null, loading: Boolean = false) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(text)
            if (onRetry != null) TextButton(onClick = onRetry) { Text("다시 조회") }
        }
    }
}

@Composable
private fun RouteCard(route: CommuteRoute, isFastest: Boolean, arrival: BoardingArrival?, loadingArrival: Boolean) {
    val firstRide = route.firstRide ?: return
    val colors = MaterialTheme.colorScheme
    val modes = route.rides.map { it.mode }.distinct().joinToString(" + ") { it.label }
    val transfers = if (route.transfers == 0) "환승 없음" else "환승 ${route.transfers}회"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(if (isFastest) 1.5.dp else 1.dp, if (isFastest) colors.primary else colors.outlineVariant)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (isFastest) Text("가장 빠른 경로", style = MaterialTheme.typography.labelLarge, color = colors.primary)
                    Text("${modes} · ${transfers}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = if (isFastest) colors.primary else colors.surfaceVariant,
                    contentColor = if (isFastest) colors.onPrimary else colors.onSurfaceVariant
                ) {
                    Text(
                        "약 ${route.minutes}분",
                        Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                val firstLine = firstRide.lines.joinToString(" / ") { it.name }.ifBlank { firstRide.mode.label }
                TransitStop("탑승", firstRide.start, firstLine, firstRide.mode, hasPrevious = false, hasNext = true) {
                    DirectionBadge(arrival?.direction)
                    val direction = arrival?.line?.let { line ->
                        if (firstRide.mode == TransitMode.SUBWAY) "같은 방면 · $line" else line
                    }
                    ArrivalDisplay(arrival, loadingArrival, hint = direction)
                    if (arrival?.seconds?.let { it < route.approachMinutes * 60 } == true) {
                        Text(
                            "탑승지에 도착하기 전에 출발할 수 있어요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
                route.rides.zipWithNext().forEach { (previous, next) ->
                    val station = if (previous.end == next.start) next.start else "${previous.end} → ${next.start}"
                    val line = next.lines.joinToString(" / ") { it.name }.ifBlank { next.mode.label }
                    TransitStop("환승", station, line, next.mode, hasPrevious = true, hasNext = true)
                }
                TransitStop("하차", route.rides.last().end, null, null, hasPrevious = true, hasNext = false)
            }
        }
    }
}

@Composable
fun HomeLocationPicker(
    state: HomeSearchState,
    origin: Coordinates?,
    onSearch: (String) -> Unit,
    onQueryChange: () -> Unit,
    onSave: suspend (HomeLocation) -> Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<HomeLocation?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("집 위치 설정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        selected = null
                        onQueryChange()
                    },
                    label = { Text("도로명 주소 또는 지하철역") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focus.clearFocus()
                        onSearch(query)
                    })
                )
                Row {
                    TextButton(
                        onClick = {
                            focus.clearFocus()
                            onSearch(query)
                        },
                        enabled = query.isNotBlank() && !saving
                    ) { Text("검색") }
                    if (origin != null) {
                        TextButton(
                            onClick = { selected = HomeLocation("현재 위치", origin) },
                            enabled = !saving
                        ) { Text("현재 위치 선택") }
                    }
                }
                if (state.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                if (state.entries.isNotEmpty()) {
                    LazyColumn(Modifier.heightIn(max = 200.dp)) {
                        items(state.entries) { home ->
                            TextButton(onClick = { selected = home }, enabled = !saving) {
                                Text(home.label, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
                selected?.let { Text("선택한 위치 · ${it.label}", fontWeight = FontWeight.SemiBold) }
                if (saveFailed) Text("집 위치를 저장하지 못했어요.", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("취소") } },
        confirmButton = {
            Button(
                enabled = selected != null && !saving,
                onClick = {
                    val home = selected ?: return@Button
                    saving = true
                    scope.launch {
                        if (onSave(home)) onDismiss() else {
                            saveFailed = true
                            saving = false
                        }
                    }
                }
            ) { Text("저장") }
        }
    )
}
