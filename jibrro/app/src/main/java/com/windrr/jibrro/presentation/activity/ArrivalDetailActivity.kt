package com.windrr.jibrro.presentation.activity

import com.windrr.jibrro.presentation.ui.theme.JibroDisplay
import com.windrr.jibrro.presentation.ui.theme.Signage
import com.windrr.jibrro.presentation.component.LineBadge
import com.windrr.jibrro.presentation.commute.rideColor
import com.windrr.jibrro.domain.commute.TransitMode
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import com.windrr.jibrro.presentation.commute.ArrivalConductor
import com.windrr.jibrro.presentation.commute.ArrivalDetails
import com.windrr.jibrro.presentation.commute.DirectionBadge
import com.windrr.jibrro.presentation.component.ImportantAlertPermissionOverlay
import com.windrr.jibrro.presentation.component.JibrroSubpage
import java.text.DateFormat
import java.util.Date

class ArrivalDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val details = IntentCompat.getSerializableExtra(intent, ArrivalDetails.EXTRA, ArrivalDetails::class.java)
            ?: run { finish(); return }
        enableEdgeToEdge()
        setContent {
            JibrroSubpage("도착 상세", ::finish) { padding ->
                ArrivalDetailContent(details, Modifier.padding(padding))
                if (intent.data?.scheme == "jibro") ImportantAlertPermissionOverlay()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ArrivalDetailContent(details: ArrivalDetails, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ArrivalConductor()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (details.bus) "탑승 정류장" else "탑승역",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
            Text(details.stop, style = MaterialTheme.typography.headlineSmall, fontFamily = JibroDisplay, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LineBadge(details.line, rideColor(if (details.bus) TransitMode.BUS else TransitMode.SUBWAY, details.line))
                DirectionBadge(details.direction)
                if (details.lastTrain) {
                    Surface(shape = RoundedCornerShape(8.dp), color = colors.tertiaryContainer) {
                        Text(
                            "막차", Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArrivalMetric("도착까지", details.eta ?: "시간 미제공", Modifier.weight(1f), true)
            ArrivalMetric("남은 정거장", details.stopsAway ?: "정보 없음", Modifier.weight(1f), false)
        }

        if (!details.destination.isNullOrBlank() || !details.alightingStop.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.outlineVariant),
                color = colors.surface
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    details.destination?.takeIf(String::isNotBlank)?.let { ArrivalFact("행선지", it) }
                    details.alightingStop?.takeIf(String::isNotBlank)?.let {
                        ArrivalFact(if (details.bus) "하차 정류장" else "하차역", it)
                    }
                }
            }
        }

        Text(
            details.fetchedAt?.let { DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it)) + " 조회 기준" }
                ?: "조회 시점 기준",
            modifier = Modifier.align(Alignment.End),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ArrivalMetric(label: String, value: String, modifier: Modifier, primary: Boolean) {
    // Departure-board style panel: dark board, LED-colored pixel digits.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Signage.NavyDeep,
        contentColor = Signage.SoftSky,
        border = BorderStroke(2.dp, Signage.Navy)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JibroDisplay,
                fontWeight = FontWeight.Bold,
                color = if (primary) Signage.SafetyYellow else Signage.Paper
            )
        }
    }
}

@Composable
private fun ArrivalFact(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
