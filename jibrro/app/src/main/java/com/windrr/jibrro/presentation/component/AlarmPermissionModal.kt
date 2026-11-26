package com.windrr.jibrro.presentation.component

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.infrastructure.AlertFeature
import com.windrr.jibrro.infrastructure.AlertPermissions
import com.windrr.jibrro.infrastructure.alertPermissions

@Composable
internal fun rememberAlertPermissions(feature: AlertFeature): AlertPermissions {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var permissions by remember(feature) { mutableStateOf(context.alertPermissions(feature)) }
    DisposableEffect(lifecycle, feature) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissions = context.alertPermissions(feature)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return permissions
}

@Composable
internal fun AlarmPermissionModal(feature: AlertFeature, onGranted: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var permissions by remember(feature) { mutableStateOf(context.alertPermissions(feature)) }
    var locationRequested by remember { mutableStateOf(false) }
    var notificationRequested by remember { mutableStateOf(false) }
    val appUri = Uri.fromParts("package", context.packageName, null)
    val location = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions = context.alertPermissions(feature)
    }
    val notification = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissions = context.alertPermissions(feature)
    }
    val background = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissions = context.alertPermissions(feature)
    }

    DisposableEffect(lifecycle, feature) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissions = context.alertPermissions(feature)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(permissions.ready) {
        if (permissions.ready) onGranted()
    }
    if (permissions.ready) return

    JibrroPermissionDialog(
        title = "${feature.title} 권한",
        mode = if (feature == AlertFeature.LAST_TRAIN) TransitMode.SUBWAY else null,
        onDismiss = onDismiss
    ) {
        if (!permissions.preciseLocation) {
            PermissionAction(
                "정확한 위치",
                if (feature == AlertFeature.DESTINATION) "선택한 역까지 남은 거리를 확인해요."
                else "현재 위치에서 가까운 역을 찾아요.",
                "정확한 위치 허용"
            ) {
                if (locationRequested) {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri))
                } else {
                    locationRequested = true
                    location.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }
        }
        if (permissions.preciseLocation && !permissions.backgroundLocation && Build.VERSION.SDK_INT >= 29) {
            PermissionAction(
                "백그라운드 위치",
                "앱을 닫아도 가까운 역의 막차를 확인해요. 위치 권한에서 ‘항상 허용’을 선택해 주세요.",
                "백그라운드 위치 설정"
            ) {
                if (Build.VERSION.SDK_INT == 29) background.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                else context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri))
            }
        }
        if (!permissions.notifications) {
            PermissionAction(
                "알림",
                if (feature == AlertFeature.DESTINATION) "선택한 역에 가까워지면 휴대폰으로 알려드려요."
                else "가까운 역의 막차 정보를 휴대폰으로 알려드려요.",
                "알림 허용"
            ) {
                if (Build.VERSION.SDK_INT >= 33 && !notificationRequested &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationRequested = true
                    notification.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.openAlertSettings(feature, appUri)
                }
            }
        }
        if (!permissions.exactAlarm && Build.VERSION.SDK_INT >= 31) {
            PermissionAction("정확한 알람", "막차 알림을 예약한 시간에 보내요.", "정확한 알람 권한 설정") {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, appUri))
            }
        }
    }
}

private fun Context.openAlertSettings(feature: AlertFeature, appUri: Uri) {
    if (Build.VERSION.SDK_INT >= 26) {
        val channel = getSystemService(android.app.NotificationManager::class.java).getNotificationChannel(feature.channelId)
        startActivity(Intent(if (channel == null) Settings.ACTION_APP_NOTIFICATION_SETTINGS else Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            if (channel != null) putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
        })
    } else {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri))
    }
}
