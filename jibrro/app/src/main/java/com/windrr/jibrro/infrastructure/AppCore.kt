package com.windrr.jibrro.infrastructure

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.windrr.jibrro.presentation.widget.ArrivalInfoWidget
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AppCore : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(LifeCycleCallback())
        val request = PeriodicWorkRequestBuilder<JibroWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "My15MinJob",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    class LifeCycleCallback : ActivityLifecycleCallbacks {
        private var started = 0
        private var changingConfig = false

        private fun refreshAllArrivalWidgets(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val manager = GlanceAppWidgetManager(context)
                val ids = manager.getGlanceIds(ArrivalInfoWidget::class.java)
                ids.forEach { ArrivalInfoWidget().update(context, it) }
            }
        }

        override fun onActivityStarted(activity: Activity) {
            if (!changingConfig) {
                if (started == 0) refreshAllArrivalWidgets(activity)
                started++
            }
        }

        override fun onActivityStopped(activity: Activity) {
            changingConfig = activity.isChangingConfigurations
            if (!changingConfig) {
                started--
                if (started == 0) refreshAllArrivalWidgets(activity)
            }
        }

        override fun onActivityCreated(a: Activity, b: Bundle?) {}
        override fun onActivityResumed(a: Activity) {}
        override fun onActivityPaused(a: Activity) {}
        override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
        override fun onActivityDestroyed(a: Activity) {}
    }
}