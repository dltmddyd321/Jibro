package com.windrr.jibrro.presentation.component

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.infrastructure.AlertFeature
import com.windrr.jibrro.infrastructure.canPostAlerts
import com.windrr.jibrro.infrastructure.canScheduleReminders
import com.windrr.jibrro.infrastructure.isAlertChannelEnabled

@Composable
internal fun ImportantAlertPermissionOverlay() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val appUri = Uri.fromParts("package", context.packageName, null)
    var notifications by remember { mutableStateOf(true) }
    var reminders by remember { mutableStateOf(true) }
    var blockedChannels by remember { mutableStateOf(emptyList<AlertFeature>()) }
    var dismissed by remember { mutableStateOf(false) }
    var requested by rememberSaveable { mutableStateOf(false) }

    fun checkPermissions() {
        notifications = context.canPostAlerts()
        reminders = context.canScheduleReminders()
        blockedChannels = if (notifications) AlertFeature.entries.filterNot { context.isAlertChannelEnabled(it) }
            else emptyList()
    }

    val requestNotification = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        checkPermissions()
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dismissed = false
                checkPermissions()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    if (dismissed || (notifications && reminders && blockedChannels.isEmpty())) return

    JibrroPermissionDialog("중요 알림을 받을 수 없어요", TransitMode.SUBWAY, onDismiss = { dismissed = true }) {
        if (!notifications) {
            PermissionAction("알림 권한", "목적지에 가까워지거나 막차가 확인됐을 때 알림을 받으려면 허용해 주세요.", "알림 허용") {
                if (Build.VERSION.SDK_INT >= 33 && !requested &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    requested = true
                    requestNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (Build.VERSION.SDK_INT >= 26) {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                } else {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri))
                }
            }
        }
        if (!reminders && Build.VERSION.SDK_INT >= 31) {
            PermissionAction("알람 및 리마인더", "막차 알림을 받으려면 휴대폰 설정에서 허용해 주세요.", "알람 및 리마인더 설정") {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, appUri))
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            blockedChannels.forEach { feature ->
                PermissionAction(feature.title, "이 알림이 꺼져 있어요. 휴대폰 설정에서 허용해 주세요.", "${feature.title} 켜기") {
                    context.startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, feature.channelId))
                }
            }
        }
    }
}
