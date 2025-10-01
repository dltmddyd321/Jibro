package com.windrr.jibrro.presentation.widget

import SubwayLineMap
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.windrr.jibrro.R
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.state.LocationState
import com.windrr.jibrro.domain.state.Result
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.activity.SplashActivity
import com.windrr.jibrro.presentation.activity.formatArrivalMessage
import com.windrr.jibrro.presentation.ui.theme.AccentBlue
import com.windrr.jibrro.presentation.ui.theme.Bg
import com.windrr.jibrro.presentation.ui.theme.Danger
import com.windrr.jibrro.presentation.ui.theme.OnBg
import com.windrr.jibrro.presentation.ui.theme.Subtle
import com.windrr.jibrro.presentation.ui.theme.Surface
import com.windrr.jibrro.presentation.widget.action.RefreshAction
import com.windrr.jibrro.presentation.widget.di.WidgetEntryPoint
import dagger.hilt.EntryPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArrivalInfoWidget : GlanceAppWidget() {

    private val appOpenAction = actionStartActivity<SplashActivity>()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPoints.get(context, WidgetEntryPoint::class.java)
        val locationHelper = LocationHelper(context)
        var latLng = locationHelper.getLastLocationSuspend()

        if (latLng == null) {
            val lastLocation = withContext(Dispatchers.IO) {
                entryPoint.getLastLocationUseCase().invoke()
            }

            when (lastLocation) {
                is LocationState.Available -> {
                    val lat = lastLocation.lat
                    val lng = lastLocation.lng
                    latLng = Pair(lat, lng)
                }

                is LocationState.Empty -> latLng = null
            }
        }

        val getStationListUseCase = entryPoint.getClosestStationUseCase()
        val closestStation = latLng?.let { (lat, lng) ->
            getStationListUseCase.invoke(lat, lng)
        }

        Log.d("ArrivalInfoWidget", " $latLng Closest station: ${closestStation?.name}")

        val getArrivals = entryPoint.getSubwayArrivalDataUseCase()

        val arrival = closestStation?.let { station ->
            withContext(Dispatchers.IO) { getArrivals.execute(statnNm = station.name) }
        } ?: Result.Error("No closest station found")

        val fetchedTime: Long? = if (arrival is Result.Success) System.currentTimeMillis() else null

        provideContent {
            ArrivalInfoWidgetScreen(closestStation, arrival, fetchedTime)
        }
    }

    @Composable
    fun ArrivalInfoWidgetScreen(
        closestStation: SubwayStation?,
        arrivalData: Result<List<RealtimeArrival>>,
        fetchedTime: Long?
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Bg)
                .padding(10.dp)
                .clickable(onClick = appOpenAction)
        ) {
            HeaderSection(
                closestStation = closestStation,
                lastUpdatedText = when (arrivalData) {
                    is Result.Success -> formatLastUpdated(fetchedTime)
                    is Result.Loading -> "갱신 중…"
                    is Result.Error -> "오류 발생"
                }
            )

            Spacer(GlanceModifier.height(8.dp))

            ContentSection(arrivalData)

            Spacer(GlanceModifier.height(8.dp))
            FooterHint()
        }
    }

    @Composable
    private fun HeaderSection(closestStation: SubwayStation?, lastUpdatedText: String) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column {
                Text(
                    text = closestStation?.name ?: "위치 정보를 가져올 수 없습니다",
                    style = TextStyle(fontSize = 20.sp, color = OnBg)
                )
                Text(
                    text = lastUpdatedText,
                    style = TextStyle(fontSize = 11.sp, color = Subtle)
                )
            }
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = "새로고침",
                style = TextStyle(fontSize = 16.sp, color = AccentBlue),
                modifier = GlanceModifier.clickable(onClick = actionRunCallback<RefreshAction>())
            )
        }
    }

    @Composable
    private fun ContentSection(result: Result<List<RealtimeArrival>>) {
        when (result) {
            is Result.Loading -> {
                PlaceholderCard("도착 정보를 불러오는 중…")
            }

            is Result.Error -> {
                PlaceholderCard("연결 오류: ${result.message ?: "알 수 없는 오류"}")
            }

            is Result.Success -> {
                val list = result.data.orEmpty()
                if (list.isEmpty()) {
                    PlaceholderCard("표시할 도착 정보가 없습니다")
                    return
                }
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                ) {
                    items(list) { arrival ->
                        ArrivalCard(arrival)
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ArrivalCard(a: RealtimeArrival) {
        val upColor = ColorProvider(day = Color(0xFF1E88E5), night = Color(0xFF90CAF9))
        val downColor = ColorProvider(day = Color(0xFFD32F2F), night = Color(0xFFEF9A9A))
        val lineColor = if (a.updnLine == "상행") upColor else downColor

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Surface)
                .padding(10.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = SubwayLineMap.getNameById(a.subwayId),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = lineColor,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier
                        .background(ImageProvider(R.drawable.widget_chip_background))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Spacer(GlanceModifier.width(8.dp))

                Text(
                    text = a.updnLine,
                    style = TextStyle(fontSize = 12.sp, color = Subtle)
                )

                Spacer(GlanceModifier.defaultWeight())

                if (a.lstcarAt == "1") {
                    Text(
                        text = "막차",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Danger,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .background(ImageProvider(R.drawable.widget_badge_last_train))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(GlanceModifier.height(6.dp))

            Text(
                text = formatArrivalMessage(a.arvlMsg2),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnBg)
            )

            if (a.trainLineNm.isNotBlank()) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = a.trainLineNm,
                    style = TextStyle(fontSize = 12.sp, color = Subtle)
                )
            }
        }
    }

    @Composable
    private fun PlaceholderCard(text: String) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Surface)
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(text = text, style = TextStyle(fontSize = 13.sp, color = Subtle))
        }
    }

    @Composable
    private fun FooterHint() {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "탭하면 앱이 열립니다",
                style = TextStyle(fontSize = 11.sp, color = Subtle)
            )
        }
    }

    private fun formatLastUpdated(millis: Long?): String {
        if (millis == null) return "마지막 업데이트 없음"
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val fmt = java.text.SimpleDateFormat("a h시 m분", java.util.Locale.KOREAN)
        return "마지막 업데이트 • ${fmt.format(cal.time)}"
    }
}