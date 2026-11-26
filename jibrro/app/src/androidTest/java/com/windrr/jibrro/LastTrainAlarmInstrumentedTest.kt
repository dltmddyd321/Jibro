package com.windrr.jibrro

import android.app.Notification
import android.app.NotificationManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.data.repository.repositoryImpl.AlarmRepositoryImpl
import com.windrr.jibrro.domain.repository.AlarmRepository
import com.windrr.jibrro.domain.repository.SettingsRepository
import com.windrr.jibrro.domain.usecase.GetLastTrainNotificationUseCase
import com.windrr.jibrro.domain.usecase.RegisterAlarmUseCase
import com.windrr.jibrro.infrastructure.LastTrainAlert
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LastTrainAlarmInstrumentedTest {
    @Test
    fun scheduledAlarmRespectsSavedToggle() = runBlocking {
        val expected = InstrumentationRegistry.getArguments().getString("expectLastTrainNotification")?.toBooleanStrictOrNull()
        assumeTrue("Run with an explicit expectation and preconfigured app permissions.", expected != null)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(NotificationManager::class.java)
        val message = "막차 알림 검증 ${SystemClock.elapsedRealtime()}"
        AlarmRepositoryImpl(context).registerAlarm(AlarmInfo(System.currentTimeMillis() + 250, message))

        assertEquals(expected, waitForNotification(manager, message, expected == true))
    }

    @Test
    fun freshProviderLastTrainSchedulesAndDeliversOnlyOnce() = runBlocking {
        val expected = InstrumentationRegistry.getArguments().getString("expectLastTrainNotification")
        assumeTrue("Requires the saved toggle and notification permissions.", expected == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(NotificationManager::class.java)
        val alarmRepository = AlarmRepositoryImpl(context)
        var scheduled = 0
        val countingRepository = object : AlarmRepository {
            override suspend fun registerAlarm(alarmInfo: AlarmInfo): Boolean {
                scheduled++
                return alarmRepository.registerAlarm(alarmInfo)
            }
        }
        val settings = object : SettingsRepository {
            override fun getLastTrainNotification() = flowOf(true)
            override suspend fun setLastTrainNotification(enabled: Boolean) = Unit
            override fun getDestination() = flowOf<Destination?>(null)
            override suspend fun setDestination(destination: Destination?) = Unit
            override fun getLastLat() = flowOf<Double?>(null)
            override suspend fun setLastLat(lat: Double) = Unit
            override fun getLastLng() = flowOf<Double?>(null)
            override suspend fun setLastLng(lng: Double) = Unit
        }
        val alert = LastTrainAlert(
            context, GetLastTrainNotificationUseCase(settings), RegisterAlarmUseCase(countingRepository)
        )
        val trainNumber = System.currentTimeMillis().toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }.format(Date())
        val row = Gson().fromJson(
            """{"subwayId":"1002","btrainNo":"$trainNumber","lstcarAt":"1","recptnDt":"$timestamp",
                "bstatnNm":"성수","trainLineNm":"성수행","updnLine":"내선","arvlCd":"99","barvlDt":"90"}""",
            RealtimeArrival::class.java
        )

        alert.check("강남", listOf(row))
        alert.check("강남", listOf(row))
        assertEquals(1, scheduled)
        assertEquals(true, waitForNotification(manager, "강남역 · 성수행", true))
    }

    private fun waitForNotification(manager: NotificationManager, message: String, expected: Boolean): Boolean {
        val deadline = SystemClock.elapsedRealtime() + if (expected) 12_000 else 4_000
        while (SystemClock.elapsedRealtime() < deadline) {
            val notification = manager.activeNotifications.firstOrNull {
                it.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() == message
            }
            if (notification != null) {
                manager.cancel(notification.id)
                return true
            }
            SystemClock.sleep(150)
        }
        return false
    }
}
