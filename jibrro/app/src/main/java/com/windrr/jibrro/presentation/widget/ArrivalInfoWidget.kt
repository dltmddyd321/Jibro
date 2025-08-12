package com.windrr.jibrro.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.windrr.jibrro.R
import com.windrr.jibrro.presentation.activity.formatArrivalMessage
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.data.respository.repositoryImpl.SubwayStationRepositoryImpl
import com.windrr.jibrro.data.respository.datasource.StationAssetDataSource
import com.windrr.jibrro.data.model.SubwayStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

class ArrivalInfoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val locationHelper = LocationHelper(context)
        val latLng = withContext(Dispatchers.IO) {
            locationHelper.getLastLocationSuspend()
        }
        val assetDataSource = StationAssetDataSource(context)
        val stationRepo = SubwayStationRepositoryImpl(assetDataSource)
        val getStationListUseCase = GetStationListUseCase(stationRepo)
        val stations = getStationListUseCase().filter { it.lat != 0.0 && it.lng != 0.0 }
        val closestStation = latLng?.let { (lat, lng) ->
            stations.minByOrNull { station ->
                distanceInMeters(lat, lng, station.lat, station.lng)
            }
        }
        provideContent {
            ArrivalInfoWidgetScreen(latLng, closestStation)
        }
    }

    @Composable
    fun ArrivalInfoWidgetScreen(latLng: Pair<Double, Double>?, closestStation: SubwayStation?) {
        Column {
            if (latLng == null) {
                Text("위치 정보를 가져올 수 없습니다")
            } else {
                Text("위도: ${latLng.first}, 경도: ${latLng.second}")
            }
            Spacer(GlanceModifier.height(8.dp))
            if (closestStation != null) {
                Text("가장 가까운 역: ${closestStation.name}")
            } else {
                Text("가까운 역 정보를 찾을 수 없습니다")
            }
        }
    }

    @Composable
    fun SubwayArrivalItemGlance(
        subwayId: String,
        updnLine: String,
        arvlMsg2: String,
        trainLineNm: String,
        lstcarAt: String,
        modifier: GlanceModifier = GlanceModifier
    ) {
        val labelColor = ColorProvider(day = Color(0xFF424242), night = Color(0xFFEEEEEE))
        val upColor = ColorProvider(day = Color(0xFF1E88E5), night = Color(0xFF90CAF9))
        val downColor = ColorProvider(day = Color(0xFFD32F2F), night = Color(0xFFEF9A9A))
        val lineColor = if (updnLine == "상행") upColor else downColor

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = SubwayLineMap.getNameById(subwayId),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (lstcarAt == "1") {
                    Text(
                        text = "막차",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(
                                day = Color(0xFFD32F2F),
                                night = Color(0xFFFFCDD2)
                            )
                        ),
                        modifier = GlanceModifier
                            .background(ImageProvider(R.drawable.widget_badge_last_train))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = formatArrivalMessage(arvlMsg2),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )

            Text(
                text = trainLineNm,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = lineColor
                )
            )
        }
    }

    private fun distanceInMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}