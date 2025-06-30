package com.windrr.jibrro.presentation.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.activity.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.component.PermissionRequiredDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.net.toUri

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper
    private val showPermissionDialog = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestLastLocation()
        } else {
            Toast.makeText(this, "권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JibrroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
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
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun requestLastLocation() {
        locationHelper.getLastLocation(
            onSuccess = { lat, lon ->
                Log.d("SplashActivity", "위치: lat=$lat, lon=$lon")
                start(lat, lon)
            },
            onFailure = {
                showPermissionDialog.value = true
            }
        )
    }

    private fun start(lat: Double, lon: Double) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lon", lon)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermissionAndFetch()
    }
}