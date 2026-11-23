package com.windrr.jibrro.presentation.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.infrastructure.AlertFeature
import com.windrr.jibrro.presentation.commute.TransitIcon
import com.windrr.jibrro.presentation.component.AlarmPermissionModal
import com.windrr.jibrro.presentation.component.FeatureHeader
import com.windrr.jibrro.presentation.component.JibrroSubpage
import com.windrr.jibrro.presentation.component.rememberAlertPermissions
import kotlinx.coroutines.launch

@Composable
internal fun DestinationAlertScreen(
    stations: List<SubwayStation>,
    destination: Destination?,
    onSave: suspend (Destination?) -> Boolean,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val permissions = rememberAlertPermissions(AlertFeature.DESTINATION)
    var query by rememberSaveable { mutableStateOf("") }
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    var requestingPermissions by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val groups = remember(stations) { stations.groupBy { it.name } }
    val keyword = query.trim().removeSuffix("역").ifBlank { query.trim() }
    val matches = remember(groups, keyword) {
        groups.entries.filter { (name, values) ->
            name.contains(keyword, ignoreCase = true) ||
                values.any { it.station_nm_eng.orEmpty().contains(keyword, ignoreCase = true) }
        }
    }

    fun save(value: Destination?) {
        saving = true
        failed = false
        scope.launch {
            if (onSave(value)) onBack()
            else {
                saving = false
                failed = true
            }
        }
    }

    val selectedStation = selectedName?.let { groups[it]?.firstOrNull() }
    val pending = selectedStation?.let { Destination(it.bldn_id, it.name, it.lat, it.lng) }
    JibrroSubpage("목적지 알림", onBack) { innerPadding ->
        if (requestingPermissions) {
            AlarmPermissionModal(
                feature = AlertFeature.DESTINATION,
                onGranted = {
                    requestingPermissions = false
                    (pending ?: destination)?.let { save(it) }
                },
                onDismiss = { requestingPermissions = false }
            )
        }

        val colors = MaterialTheme.colorScheme
        val focus = LocalFocusManager.current
        Column(Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FeatureHeader("내릴 역을 골라요", "2km · 1km · 500m 이내에\n들어오면 알려드려요.")
                }
                if (destination != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.outlineVariant)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                destination?.let {
                                    Text("현재 목적지", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                                    Text(it.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    if (!permissions.ready) {
                                        Text("권한을 허용하면 목적지 알림을 받을 수 있어요.", style = MaterialTheme.typography.bodySmall)
                                        TextButton(onClick = { requestingPermissions = true }, enabled = !saving) {
                                            Text("권한 설정")
                                        }
                                    }
                                    TextButton(onClick = { save(null) }, enabled = !saving) { Text("목적지 알림 끄기") }
                                }
                                if (failed) Text("목적지 설정을 저장하지 못했어요.", color = colors.error)
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        label = { Text("목적지 역 검색") }
                    )
                }
                if (matches.isEmpty()) item { Text("검색 결과가 없어요.", color = colors.onSurfaceVariant) }
                items(matches, key = { it.key }) { (name, values) ->
                    val line = values.flatMap { it.line.split('/') }.map(String::trim).distinct().joinToString(" / ")
                    val selected = (selectedName ?: destination?.name) == name
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = if (selected) colors.primaryContainer else colors.surface,
                        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = selected,
                                enabled = !saving,
                                role = Role.RadioButton,
                                onClick = {
                                    selectedName = name
                                    focus.clearFocus()
                                }
                            ).padding(horizontal = 13.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = colors.primaryContainer) {
                                Box(Modifier.size(33.dp), contentAlignment = Alignment.Center) {
                                    TransitIcon(TransitMode.SUBWAY, colors.onPrimaryContainer, Modifier.size(19.dp))
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(line, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            RadioButton(selected = selected, onClick = null)
                        }
                    }
                }
            }
            pending?.let { selected ->
                Surface(color = colors.surface, tonalElevation = 2.dp) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Button(
                            onClick = {
                                if (permissions.ready) save(selected)
                                else requestingPermissions = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !saving
                        ) { Text("${selected.name} 알림 시작") }
                        if (failed && destination == null) {
                            Text("목적지 설정을 저장하지 못했어요.", color = colors.error)
                        }
                    }
                }
            }
        }
    }
}
