package com.windrr.jibrro.presentation.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityWithIntent
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.windrr.jibrro.R
import com.windrr.jibrro.presentation.activity.SplashActivity
import com.windrr.jibrro.presentation.commute.ArrivalDetails
import com.windrr.jibrro.presentation.widget.action.ChangeWidgetStationAction
import com.windrr.jibrro.presentation.widget.action.RefreshAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val widgetText = ColorProvider(day = Color(0xFF131B26), night = Color(0xFFE3E9F1))
private val widgetSubtle = ColorProvider(day = Color(0xFF4A5666), night = Color(0xFFB6C1CF))
private val widgetAccent = ColorProvider(day = Color(0xFF1D3557), night = Color(0xFFFFCD3C))

@Composable
internal fun ArrivalWidgetContent(snapshot: WidgetSnapshot, widgetIdentity: String) {
    val size = LocalSize.current
    val expanded = size.height >= 184.dp
    val shownRows = when {
        size.height >= 260.dp -> 4
        expanded -> 3
        size.height >= 124.dp -> 2
        else -> 1
    }

    Column(
        modifier = GlanceModifier.fillMaxSize().appWidgetBackground()
            .background(ImageProvider(R.drawable.widget_panel))
            .cornerRadius(24.dp).padding(8.dp)
    ) {
        WidgetHeader(snapshot)
        Spacer(GlanceModifier.height(4.dp))
        when {
            snapshot.loading -> WidgetLoading()
            snapshot.failed -> WidgetMessage("도착 정보를 불러오지 못했어요.")
            snapshot.stop == null -> WidgetMessage("앱에서 위치를 확인하거나 즐겨찾기 역을 추가해 주세요.")
            snapshot.arrivals.isEmpty() -> WidgetMessage("현재 표시할 도착 정보가 없어요.")
            else -> snapshot.arrivals.take(shownRows).forEachIndexed { index, arrival ->
                val details = ArrivalDetails(
                    stop = snapshot.stop.name,
                    line = arrival.line,
                    direction = arrival.direction.trimStart('↑', '↓', '↻', '↺', ' '),
                    destination = arrival.destination,
                    eta = arrival.eta,
                    stopsAway = arrival.stops,
                    lastTrain = arrival.lastTrain,
                    fetchedAt = snapshot.fetchedAt
                )
                WidgetArrivalRow(arrival, expanded, details, "$widgetIdentity/$index")
                Spacer(GlanceModifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun WidgetHeader(snapshot: WidgetSnapshot) {
    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            GlanceModifier.defaultWeight().clickable(actionStartActivity<SplashActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(ImageProvider(R.drawable.jibro_train_cat), null, GlanceModifier.size(38.dp))
            Spacer(GlanceModifier.width(6.dp))
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    snapshot.stop?.name ?: "지하철 도착",
                    style = TextStyle(color = widgetText, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                val source = snapshot.stop?.let { if (it.favorite) "즐겨찾기" else "가까운 역" }
                val time = snapshot.fetchedAt?.let {
                    SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(it)) + " 조회"
                }
                Text(
                    listOfNotNull(source, time).joinToString(" · ").ifBlank { "Jibro" },
                    style = TextStyle(color = widgetSubtle, fontSize = 11.sp),
                    maxLines = 1
                )
            }
        }
        if (snapshot.stops.size > 1) {
            val index = snapshot.stops.indexOf(snapshot.stop)
            val next = snapshot.stops[(index + 1) % snapshot.stops.size]
            WidgetIcon(
                R.drawable.widget_switch_station, "다음 역 ${next.name}",
                GlanceModifier.clickable(
                    actionRunCallback<ChangeWidgetStationAction>(
                        actionParametersOf(
                            ChangeWidgetStationAction.StationName to next.name,
                            ChangeWidgetStationAction.IsFavorite to next.favorite
                        )
                    )
                )
            )
        }
        WidgetIcon(R.drawable.widget_refresh, "도착 정보 새로고침", GlanceModifier.clickable(actionRunCallback<RefreshAction>()))
    }
}

@Composable
private fun WidgetIcon(icon: Int, label: String, action: GlanceModifier) {
    Box(
        GlanceModifier.size(42.dp).then(action),
        contentAlignment = Alignment.Center
    ) {
        Box(
            GlanceModifier.size(32.dp).background(ImageProvider(R.drawable.widget_chip_background)),
            contentAlignment = Alignment.Center
        ) {
            Image(ImageProvider(icon), label, GlanceModifier.size(18.dp), colorFilter = ColorFilter.tint(widgetAccent))
        }
    }
}

@Composable
private fun WidgetArrivalRow(arrival: WidgetArrival, expanded: Boolean, details: ArrivalDetails, rowKey: String) {
    val directionColor = when {
        arrival.direction.startsWith("↑") || arrival.direction.startsWith("↻") ->
            ColorProvider(day = Color(0xFF155685), night = Color(0xFFA3D9FF))
        arrival.direction.startsWith("↓") || arrival.direction.startsWith("↺") ->
            ColorProvider(day = Color(0xFF8B371D), night = Color(0xFFFFBEAA))
        else -> widgetSubtle
    }
    val arrivalText = arrival.eta ?: arrival.stops.orEmpty()
    Row(
        GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_card))
            .padding(horizontal = 8.dp, vertical = if (expanded) 5.dp else 4.dp)
            .clickable(actionStartActivityWithIntent(details.intent(LocalContext.current, rowKey))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(GlanceModifier.defaultWeight()) {
            val route = listOf(arrival.line, arrival.direction, if (arrival.lastTrain) "막차" else "")
                .filter { it.isNotBlank() }.joinToString(" · ")
            Text(
                route,
                style = TextStyle(color = directionColor, fontWeight = FontWeight.Medium, fontSize = 11.sp),
                maxLines = 1
            )
            if (expanded && arrival.destination.isNotBlank()) {
                Text(arrival.destination, style = TextStyle(color = widgetSubtle, fontSize = 11.sp), maxLines = 1)
            }
        }
        Spacer(GlanceModifier.width(6.dp))
        val stopHint = arrival.stops?.takeIf { arrival.eta != null }
        val text = if (!expanded && stopHint != null && LocalSize.current.width >= 278.dp) {
            "$arrivalText · $stopHint"
        } else arrivalText
        Column(horizontalAlignment = Alignment.End) {
            Text(text, style = TextStyle(color = widgetText, fontSize = 13.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            if (expanded && stopHint != null) {
                Text(stopHint, style = TextStyle(color = widgetSubtle, fontSize = 11.sp), maxLines = 1)
            }
        }
        Spacer(GlanceModifier.width(5.dp))
        Text("›", style = TextStyle(color = widgetAccent, fontSize = 16.sp))
    }
}

@Composable
private fun WidgetLoading() {
    Column(
        GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_card))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text("도착 정보 수신 중", style = TextStyle(color = widgetSubtle, fontSize = 11.sp))
        Spacer(GlanceModifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(GlanceModifier.defaultWeight().height(8.dp).background(ImageProvider(R.drawable.widget_chip_background)))
            Spacer(GlanceModifier.width(12.dp))
            Spacer(GlanceModifier.width(52.dp).height(8.dp).background(ImageProvider(R.drawable.widget_chip_background)))
        }
    }
}

@Composable
private fun WidgetMessage(message: String) {
    Text(
        message,
        modifier = GlanceModifier.fillMaxWidth().background(ImageProvider(R.drawable.widget_card)).padding(8.dp),
        style = TextStyle(color = widgetSubtle, fontSize = 12.sp),
        maxLines = 2
    )
}
