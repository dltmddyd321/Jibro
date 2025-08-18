package com.windrr.jibrro.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArrivalInfoWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArrivalInfoWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        requestImmediateRefresh(context)
    }

    private fun requestImmediateRefresh(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val m = GlanceAppWidgetManager(context)
            val ids = m.getGlanceIds(ArrivalInfoWidget::class.java)
            ids.forEach { glanceId ->
                glanceAppWidget.update(context, glanceId)
            }
        }
    }
}