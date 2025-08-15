package com.windrr.jibrro.presentation.widget.action

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.windrr.jibrro.presentation.widget.ArrivalInfoWidget

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        ArrivalInfoWidget().update(context, glanceId)
    }
}