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

class ArrivalInfoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { ArrivalInfoWidgetScreen() }
    }

    @Composable
    fun ArrivalInfoWidgetScreen() {

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
}