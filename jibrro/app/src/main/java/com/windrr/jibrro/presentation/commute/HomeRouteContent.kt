package com.windrr.jibrro.presentation.commute

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextOverflow
import com.windrr.jibrro.presentation.ui.theme.JibroDisplay
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
import com.windrr.jibrro.presentation.component.PlatformEdge
import com.windrr.jibrro.presentation.ui.theme.Signage
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

internal fun LazyListScope.homeRouteItems(
    home: HomeLocation?,
    hasPermission: Boolean,
    origin: Coordinates?,
    nearestStation: String?,
    locating: Boolean,
    state: CommuteUiState,
    onEditHome: () -> Unit,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit,
    onArrivalDetails: ((ArrivalDetails) -> Unit)? = null
) {
    item(key = "commute-home") {
        HomeHero(home, hasPermission, origin, nearestStation, locating, onEditHome, onRequestLocation, onRefresh)
    }
    if (home == null || !hasPermission) return
    if (origin == null) {
        if (locating) item(key = "commute-status") { TransitLoadingCard(TransitLoadingContent.LOCATION) }
        return
    }

    when (state) {
        CommuteUiState.Waiting, CommuteUiState.Loading -> item(key = "commute-status") {
            TransitLoadingCard(TransitLoadingContent.ROUTES)
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
                    isFastest = index == 0 && (!state.loadingArrivals || state.arrivals.isNotEmpty()) &&
                        arrival?.serviceEnded != true,
                    arrival = arrival,
                    loadingArrival = state.loadingArrivals && arrival == null,
                    onArrivalDetails = onArrivalDetails
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
    val accent = Signage.SoftSky
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Signage.Navy,
        contentColor = Signage.Paper
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 172.dp)) {
            Column(Modifier.matchParentSize()) {
                Spacer(Modifier.weight(1f))
                PlatformEdge()
            }
            // The train rolls in on the right and stops on the platform edge.
            Image(
                painter = painterResource(R.drawable.jibro_train_cat),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 9.dp)
                    .height(140.dp)
            )
            Column(
                Modifier.fillMaxWidth()
                    .padding(start = 19.dp, end = 158.dp, top = 14.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "HOME BOUND",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = JibroDisplay,
                    fontWeight = FontWeight.Bold,
                    color = Signage.SafetyYellow,
                    letterSpacing = 1.5.sp
                )
                Text("집으로", style = MaterialTheme.typography.headlineLarge, fontFamily = JibroDisplay, fontWeight = FontWeight.Bold)
                if (home == null) {
                    Text("집을 설정하면 귀가 교통편을 한눈에 볼 수 있어요.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        home.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (nearestStation != null && hasPermission && !locating && origin != null) {
                        StationNameplate(nearestStation)
                    } else {
                        Text(
                            when {
                                !hasPermission -> "현재 위치 권한이 필요해요."
                                locating -> "현재 위치 확인 중"
                                origin == null -> "현재 위치를 확인할 수 없어요."
                                else -> "현재 위치에서 출발"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = accent
                        )
                    }
                }
                FlowRow(
                    Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val buttonColors = ButtonDefaults.buttonColors(
                        containerColor = Signage.SafetyYellow,
                        contentColor = Signage.OnSafetyYellow
                    )
                    val textColors = ButtonDefaults.textButtonColors(contentColor = accent)
                    val compact = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    if (home == null) {
                        Button(onClick = onEditHome, colors = buttonColors, contentPadding = compact) { Text("집 설정", fontWeight = FontWeight.Bold) }
                        if (!hasPermission) {
                            TextButton(onClick = onRequestLocation, colors = textColors, contentPadding = compact) { Text("위치 권한 허용") }
                        }
                    } else {
                        if (!hasPermission) {
                            Button(onClick = onRequestLocation, colors = buttonColors, contentPadding = compact) { Text("위치 권한 허용", fontWeight = FontWeight.Bold) }
                        } else if (origin == null && !locating) {
                            Button(onClick = onRefresh, colors = buttonColors, contentPadding = compact) { Text("위치 다시 확인", fontWeight = FontWeight.Bold) }
                        }
                        TextButton(onClick = onEditHome, colors = textColors, contentPadding = compact) { Text("집 변경") }
                    }
                }
            }
        }
    }
}

/** White station nameplate, like the sign hanging over a platform. */
@Composable
private fun StationNameplate(station: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Signage.Paper, contentColor = Signage.NavyDeep) {
        Row(
            Modifier.padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(shape = RoundedCornerShape(5.dp), color = Signage.Navy) {
                TransitIcon(TransitMode.SUBWAY, Signage.Paper, Modifier.padding(3.dp).size(13.dp))
            }
            Text("가까운 역", style = MaterialTheme.typography.labelSmall, color = Signage.Navy.copy(alpha = 0.7f))
            Text("${station}역", style = MaterialTheme.typography.labelLarge, fontFamily = JibroDisplay, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RouteMessage(text: String, onRetry: (() -> Unit)? = null) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text)
            if (onRetry != null) TextButton(onClick = onRetry) { Text("다시 조회") }
        }
    }
}

@Composable
private fun RouteCard(
    route: CommuteRoute,
    isFastest: Boolean,
    arrival: BoardingArrival?,
    loadingArrival: Boolean,
    onArrivalDetails: ((ArrivalDetails) -> Unit)?
) {
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
                    if (isFastest) {
                        Surface(shape = RoundedCornerShape(5.dp), color = Signage.SafetyYellow, contentColor = Signage.OnSafetyYellow) {
                            Text(
                                "가장 빠른 경로",
                                Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                        fontFamily = JibroDisplay,
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
                    ArrivalDisplay(
                        arrival, loadingArrival, hint = direction,
                        onDetails = onArrivalDetails?.let { open ->
                            arrival?.let { prediction ->
                                {
                                    open(ArrivalDetails.from(
                                        prediction, firstRide.mode, firstRide.start,
                                        line = if (firstRide.mode == TransitMode.BUS) prediction.line ?: firstLine else firstLine,
                                        destination = prediction.line.takeIf { firstRide.mode == TransitMode.SUBWAY },
                                        direction = firstRide.direction,
                                        alightingStop = firstRide.end
                                    ))
                                }
                            }
                        }
                    )
                    if (arrival?.seconds?.let { it < route.approachMinutes * 60 } == true) {
                        Text(
                            "탑승지에 도착하기 전에 출발할 수 있어요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
                fun lineOf(ride: com.windrr.jibrro.domain.commute.CommuteLeg) =
                    ride.lines.joinToString(" / ") { it.name }.ifBlank { ride.mode.label }
                route.rides.zipWithNext().forEach { (previous, next) ->
                    val station = if (previous.end == next.start) next.start else "${previous.end} → ${next.start}"
                    TransitStop(
                        "환승", station, lineOf(next), next.mode, hasPrevious = true, hasNext = true,
                        incomingColor = rideColor(previous.mode, lineOf(previous))
                    )
                }
                val lastRide = route.rides.last()
                TransitStop(
                    "하차", lastRide.end, null, null, hasPrevious = true, hasNext = false,
                    incomingColor = rideColor(lastRide.mode, lineOf(lastRide))
                )
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
                if (state.loading) TransitLoadingCard(TransitLoadingContent.HOME_SEARCH)
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
