package com.windrr.jibrro.presentation.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.activity.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.component.AlarmPermissionModal
import com.windrr.jibrro.presentation.component.PermissionRequiredDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        if (isAlarmPermissionGranted()) {
            showAlarmPermissionModal.value = false
            start(lat, lng)
        }
    }

    private fun isAlarmPermissionGranted(): Boolean {
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true

        return hasNotificationPermission && hasExactAlarmPermission
    }

    @Inject
    lateinit var locationHelper: LocationHelper
    private val showPermissionDialog = mutableStateOf(false)
    private val showAlarmPermissionModal = mutableStateOf(false)
    private var lat = 0.0
    private var lng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JibrroTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                if (showPermissionDialog.value) {
                    val context = LocalContext.current
                    val settingsLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) {
                        checkLocationPermissionAndFetch()
                    }
                    PermissionRequiredDialog(
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                            settingsLauncher.launch(intent)
                        }
                    )
                }
                if (showAlarmPermissionModal.value) {
                    AlarmPermissionModal(
                        onGranted = {
                            showAlarmPermissionModal.value = false
                            start(lat, lng)
                        }
                    )
                }
            }
        }
        checkLocationPermissionAndFetch()
    }

    private fun checkLocationPermissionAndFetch() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestLastLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                showPermissionDialog.value = true
            }

            else -> {
                showPermissionDialog.value = true
            }
        }
    }

    private fun requestLastLocation() {
        locationHelper.getLastLocation(
            onSuccess = { lat, lng ->
                Log.d("SplashActivity", "위치: lat=$lat, lng=$lng")
                if (lat == 0.0 && lng == 0.0) {
                    showPermissionDialog.value = true
                } else {
                    if (!isAlarmPermissionGranted()) {
                        showAlarmPermissionModal.value = true
                    } else {
                        start(lat, lng)
                    }
                    this.lat = lat
                    this.lng = lng
                }
            },
            onFailure = {
                showPermissionDialog.value = true
            }
        )
    }

    private fun start(lat: Double, lng: Double) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
        finish()
    }
}