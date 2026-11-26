package com.windrr.jibrro.presentation.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.windrr.jibrro.BuildConfig
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.infrastructure.AlertFeature
import com.windrr.jibrro.infrastructure.enqueueLastTrainCheck
import com.windrr.jibrro.presentation.commute.TransitIcon
import com.windrr.jibrro.presentation.component.AlarmPermissionModal
import com.windrr.jibrro.presentation.component.FeatureHeader
import com.windrr.jibrro.presentation.component.JibrroSubpage
import com.windrr.jibrro.presentation.component.rememberAlertPermissions
import com.windrr.jibrro.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JibrroSubpage("설정", ::finish) { innerPadding ->
                SettingsContent(Modifier.padding(innerPadding))
            }
        }
    }

    @Composable
    private fun SettingsContent(modifier: Modifier) {
        val enabled by settingsViewModel.isLastTrainEnabled.collectAsState()
        val destination by settingsViewModel.destination.collectAsState()
        val permissions = rememberAlertPermissions(AlertFeature.LAST_TRAIN)
        val scope = rememberCoroutineScope()
        var showPermissions by rememberSaveable { mutableStateOf(false) }
        var saving by remember { mutableStateOf(false) }
        var failed by remember { mutableStateOf(false) }

        fun save(value: Boolean) {
            saving = true
            failed = false
            scope.launch {
                try {
                    failed = !settingsViewModel.saveLastTrainEnabled(value)
                    if (!failed && value) enqueueLastTrainCheck(this@SettingsActivity)
                } finally {
                    saving = false
                }
            }
        }

        if (showPermissions) {
            AlarmPermissionModal(
                feature = AlertFeature.LAST_TRAIN,
                onGranted = {
                    showPermissions = false
                    if (!enabled) save(true)
                },
                onDismiss = { showPermissions = false }
            )
        }

        Column(
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureHeader("내 알림 관리", "귀갓길에 필요한 알림을 설정해 보세요.")

            SettingsCard(
                title = "막차 알림",
                subtitle = "가까운 역의 실시간 막차 정보를 주기적으로 확인해 알려드려요.",
                mode = TransitMode.SUBWAY,
                action = {
                    Switch(
                        checked = enabled,
                        enabled = !saving,
                        modifier = Modifier.semantics { contentDescription = "막차 알림" },
                        onCheckedChange = {
                            if (it && !permissions.ready) showPermissions = true
                            else save(it)
                        }
                    )
                }
            ) {
                if (enabled && !permissions.ready) {
                    Text("권한을 확인해 주세요.", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { showPermissions = true }) { Text("막차 알림 권한 설정") }
                }
                if (failed) Text("막차 알림 설정을 저장하지 못했어요.", color = MaterialTheme.colorScheme.error)
            }

            SettingsCard(
                title = "목적지 알림",
                subtitle = destination?.name?.let { "저장된 역 · $it" } ?: "저장된 목적지가 없어요.",
                mode = null
            ) {
                TextButton(onClick = {
                    startActivity(Intent(this@SettingsActivity, LikeStationActivity::class.java).apply {
                        putExtra("startMode", "destination")
                    })
                }) { Text("목적지 알림 설정") }
            }

            SettingsCard(
                title = "앱 정보",
                subtitle = "설치 버전 · ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                mode = null
            ) {
                TextButton(onClick = ::openPlayStore) { Text("Play 스토어에서 보기") }
            }
        }
    }

    private fun openPlayStore() {
        val store = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        val web = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
        try {
            startActivity(store)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(web)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Play 스토어 페이지를 열 수 없어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    mode: TransitMode?,
    action: @Composable () -> Unit = {},
    details: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = RoundedCornerShape(9.dp), color = colors.primaryContainer) {
                    Box(Modifier.size(37.dp), contentAlignment = Alignment.Center) {
                        TransitIcon(mode, colors.onPrimaryContainer, Modifier.size(21.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                action()
            }
            details()
        }
    }
}
