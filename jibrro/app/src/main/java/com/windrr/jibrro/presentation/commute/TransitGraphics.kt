package com.windrr.jibrro.presentation.commute

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.TransitMode

private val trainPixels = listOf(
    "..#####..",
    ".#######.",
    ".#.....#.",
    ".#.....#.",
    ".#######.",
    ".##.#.##.",
    "..#####..",
    "..#...#..",
    ".#.....#."
)

private val busPixels = listOf(
    ".#######.",
    "#########",
    "##.....##",
    ".#.....#.",
    ".#######.",
    ".##.#.##.",
    ".#######.",
    "..#...#..",
    "..#...#.."
)

private val homePixels = listOf(
    "....#....",
    "...###...",
    "..#####..",
    ".###.###.",
    "#########",
    "..#####..",
    "..##.##..",
    "..##.##..",
    "..##.##.."
)

@Composable
internal fun TransitIcon(mode: TransitMode?, tint: Color, modifier: Modifier = Modifier) {
    val pixels = when (mode) {
        TransitMode.SUBWAY -> trainPixels
        TransitMode.BUS -> busPixels
        null -> homePixels
    }
    Canvas(modifier) {
        val unit = minOf(size.width, size.height) / 9f
        val pixel = unit.toInt().coerceAtLeast(1).toFloat()
        val left = ((size.width - pixel * 9) / 2).toInt()
        val top = ((size.height - pixel * 9) / 2).toInt()
        pixels.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                if (cell == '#') {
                    drawRect(tint, Offset(left + x * pixel, top + y * pixel), Size(pixel, pixel))
                }
            }
        }
    }
}

@Composable
internal fun TransitStop(
    label: String,
    name: String,
    line: String?,
    mode: TransitMode?,
    hasPrevious: Boolean,
    hasNext: Boolean,
    content: @Composable () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val marker = when (mode) {
        TransitMode.SUBWAY -> colors.primaryContainer to colors.onPrimaryContainer
        TransitMode.BUS -> colors.tertiaryContainer to colors.onTertiaryContainer
        null -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    Row(
        modifier = Modifier.fillMaxWidth().drawBehind {
            val centerX = 16.dp.toPx()
            val centerY = 19.dp.toPx()
            if (hasPrevious || hasNext) {
                drawLine(
                    color = colors.outlineVariant,
                    start = Offset(centerX, if (hasPrevious) 0f else centerY),
                    end = Offset(centerX, if (hasNext) size.height else centerY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(top = 3.dp).size(32.dp)
                .background(marker.first, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            TransitIcon(mode, marker.second, Modifier.size(19.dp))
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = if (hasNext) 15.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (line != null) {
                Surface(shape = RoundedCornerShape(6.dp), color = marker.first, contentColor = marker.second) {
                    Text(line, Modifier.padding(horizontal = 7.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            content()
        }
    }
}

@Composable
internal fun DirectionBadge(direction: String?) {
    val label = direction?.trim()?.takeIf(String::isNotEmpty) ?: return
    val dark = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val (background, foreground) = when (label) {
        "상행", "내선" -> if (dark) Color(0xFF1C3A5B) to Color(0xFFC6E2FF)
            else Color(0xFFE2F0FF) to Color(0xFF1B4E7B)
        "하행", "외선" -> if (dark) Color(0xFF462D57) to Color(0xFFF2D7FF)
            else Color(0xFFF3E6FC) to Color(0xFF663584)
        else -> colors.surfaceVariant to colors.onSurfaceVariant
    }
    val display = when (label) {
        "상행" -> "↑ 상행"
        "하행" -> "↓ 하행"
        else -> label
    }
    Surface(shape = RoundedCornerShape(8.dp), color = background, contentColor = foreground) {
        Text(
            display,
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun ArrivalDisplay(arrival: BoardingArrival?, loading: Boolean = false, hint: String? = null) {
    val colors = MaterialTheme.colorScheme
    val live = arrival != null && !arrival.serviceEnded
    val stopCount = arrival?.stopsAway?.takeIf { it > 0 }
    val forecast = arrival?.takeIf { it.status == null }?.seconds?.takeIf { it > 0 }
    val timeLabel = forecast?.let {
        val minutes = it / 60
        val seconds = it % 60
        when {
            minutes == 0 -> "약 ${seconds}초"
            seconds == 0 -> "약 ${minutes}분"
            else -> "약 ${minutes}분 ${seconds}초"
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = if (live) colors.primaryContainer else colors.surfaceVariant,
        contentColor = if (live) colors.onPrimaryContainer else colors.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val mainLabel = when {
                loading -> "실시간 도착 확인 중"
                arrival == null -> "실시간 도착 정보 없음"
                else -> arrival.status ?: timeLabel ?: if (arrival.seconds == 0) "곧 도착" else "도착 예정시간 미제공"
            }
            if (forecast != null && !loading) {
                Text("도착까지", style = MaterialTheme.typography.labelSmall)
            }
            Text(
                mainLabel,
                style = if (forecast != null && !loading) MaterialTheme.typography.titleLarge
                    else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            val description = hint?.takeIf(String::isNotBlank) ?: arrival?.line
            if (stopCount != null && !loading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Canvas(Modifier.size(width = 34.dp, height = 8.dp)) {
                        val radius = 2.dp.toPx()
                        val spacing = (size.width - radius * 2) / 4
                        for (index in 0..4) {
                            drawCircle(
                                color = if (index < stopCount) colors.primary else colors.outlineVariant,
                                radius = radius,
                                center = Offset(radius + index * spacing, size.height / 2)
                            )
                        }
                    }
                    Text("${stopCount}정거장 전", style = MaterialTheme.typography.labelMedium)
                }
            }
            description?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
        }
    }
}
