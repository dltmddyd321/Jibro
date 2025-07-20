package com.windrr.jibrro.presentation.component

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun AlarmPermissionModal() {
    val context = LocalContext.current

    // POST_NOTIFICATIONS 런타임 권한 (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            // granted == true면 알림 권한 OK
        }
    )

    // SCHEDULE_EXACT_ALARM은 런타임 권한이 아니라 Settings로 이동 필요 (Android 12+)
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }

    Column {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(onClick = {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }) {
                Text("알림 권한 요청")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Button(onClick = {
                requestExactAlarmPermission()
            }) {
                Text("정확한 알람 권한(설정 이동)")
            }
        }
    }
}