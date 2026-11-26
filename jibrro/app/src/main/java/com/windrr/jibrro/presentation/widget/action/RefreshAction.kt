package com.windrr.jibrro.presentation.widget.action

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.windrr.jibrro.presentation.widget.ArrivalInfoWidget
import com.windrr.jibrro.presentation.widget.WidgetStop

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        ArrivalInfoWidget().refresh(context, glanceId)
    }
}

class ChangeWidgetStationAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val name = parameters[StationName] ?: return
        val favorite = parameters[IsFavorite] ?: return
        ArrivalInfoWidget().refresh(context, glanceId, WidgetStop(name, favorite))
    }

    companion object {
        val StationName = ActionParameters.Key<String>("widget_station_name")
        val IsFavorite = ActionParameters.Key<Boolean>("widget_station_is_favorite")
    }
}
