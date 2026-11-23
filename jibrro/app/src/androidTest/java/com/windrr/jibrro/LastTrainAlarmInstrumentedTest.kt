package com.windrr.jibrro

import android.app.Notification
import android.app.NotificationManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.data.repository.repositoryImpl.AlarmRepositoryImpl
import kotlinx.coroutines.runBlocking
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

        val deadline = SystemClock.elapsedRealtime() + if (expected == true) 12_000 else 4_000
        var delivered = false
        while (SystemClock.elapsedRealtime() < deadline) {
            val notification = manager.activeNotifications.firstOrNull {
                it.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() == message
            }
            if (notification != null) {
                delivered = true
                manager.cancel(notification.id)
                break
            }
            SystemClock.sleep(150)
        }
        assertEquals(expected, delivered)
    }
}
