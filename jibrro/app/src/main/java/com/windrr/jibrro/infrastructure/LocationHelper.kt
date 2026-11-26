package com.windrr.jibrro.infrastructure

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.windrr.jibrro.domain.commute.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @android.annotation.SuppressLint("MissingPermission")
    fun preciseLocationUpdates(): Flow<Coordinates> = callbackFlow {
        if (!context.hasPreciseLocationPermission()) {
            close(SecurityException("Precise location permission required"))
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_500L)
            .setMaxUpdateAgeMillis(30_000L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val point = Coordinates(location.latitude, location.longitude)
                    if (point.isValid) trySend(point)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }
            .addOnSuccessListener { if (!isActive) fusedLocationClient.removeLocationUpdates(callback) }
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    @android.annotation.SuppressLint("MissingPermission")
    suspend fun currentCoordinates(): Coordinates? = suspendCancellableCoroutine { continuation ->
        val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val cancellation = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellation.cancel() }
        val request = CurrentLocationRequest.Builder()
            .setMaxUpdateAgeMillis(30_000L)
            .setDurationMillis(12_000L)
            .setPriority(if (fine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        try {
            fusedLocationClient.getCurrentLocation(request, cancellation.token)
                .addOnCompleteListener { task ->
                    if (continuation.isActive) {
                        continuation.resume(
                            if (task.isSuccessful) task.result?.let { Coordinates(it.latitude, it.longitude) }
                            else null
                        )
                    }
                }
        } catch (_: SecurityException) {
            continuation.resume(null)
        }
    }

    fun getLastLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onFailure("권한 없음")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    onFailure("위치를 가져올 수 없습니다.")
                }
            }
            .addOnFailureListener {
                onFailure("오류: ${it.message}")
            }
    }

    suspend fun getLastLocationSuspend(): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(location.latitude to location.longitude)
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
}
