package com.windrr.jibrro.presentation.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.activity.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.component.AlarmPermissionModal
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private val showAlarmPermissionModal = mutableStateOf(false)
    private var lat: Double? = null
    private var lng: Double? = null
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    requestLastLocation()
                } else {
                    if (!isAlarmPermissionGranted()) {
                        showAlarmPermissionModal.value = true
                    } else {
                        navigateIfReady()
                    }
                }
            }

        setContent {
            JibrroTheme {
                if (showAlarmPermissionModal.value) {
                    AlarmPermissionModal(
                        onGranted = {
                            showAlarmPermissionModal.value = false
                            navigateIfReady()
                        }
                    )
                }
            }
        }

        checkLocationPermissionAndFetch()
    }

    override fun onResume() {
        super.onResume()
        if (isAlarmPermissionGranted()) {
            showAlarmPermissionModal.value = false
            navigateIfReady()
        }
    }

    private fun navigateIfReady() {
        if (hasNavigated) return
        val ready = isAlarmPermissionGranted() && lat != null && lng != null
        if (ready) {
            hasNavigated = true
            start(lat!!, lng!!)
        }
    }

    private fun checkLocationPermissionAndFetch() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestLastLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun requestLastLocation() {
        locationHelper.getLastLocation(
            onSuccess = { lat, lng ->
                Log.d("SplashActivity", "위치: lat=$lat, lng=$lng")
                if (lat == 0.0 && lng == 0.0) {
                    Toast.makeText(this, "위치 정보를 가져오는 데 실패했습니다", Toast.LENGTH_SHORT).show()
                } else {
                    this.lat = lat
                    this.lng = lng
                    if (!isAlarmPermissionGranted()) {
                        showAlarmPermissionModal.value = true
                    } else {
                        navigateIfReady()
                    }
                }
            },
            onFailure = {
                Toast.makeText(this, "위치 정보를 가져오는 데 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun isAlarmPermissionGranted(): Boolean {
        val hasNotificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true

        val hasExactAlarmPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else true

        return hasNotificationPermission && hasExactAlarmPermission
    }

    private fun start(lat: Double, lng: Double) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("lat", lat)
            putExtra("lng", lng)
        }
        startActivity(intent)
        finish()
    }
}