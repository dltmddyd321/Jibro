package com.windrr.jibrro.presentation.commute

import com.windrr.jibrro.presentation.ui.theme.JibroDisplay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.presentation.component.LineBadge
import com.windrr.jibrro.presentation.ui.theme.Signage
import com.windrr.jibrro.presentation.ui.theme.subwayLineColor

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

/** Color used for a ride on the route map: official line color for subways, bus blue for buses. */
@Composable
internal fun rideColor(mode: TransitMode?, line: String?): Color {
    val colors = MaterialTheme.colorScheme
    return when (mode) {
        TransitMode.SUBWAY -> subwayLineColor(line) ?: colors.primary
        TransitMode.BUS -> Signage.Bus
        null -> colors.outline
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
    incomingColor: Color? = null,
    content: @Composable () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val lineColor = rideColor(mode, line)
    val upper = incomingColor ?: colors.outlineVariant
    val ring = if (mode == null) upper else lineColor
    Row(
        modifier = Modifier.fillMaxWidth().drawBehind {
            val centerX = 16.dp.toPx()
            val centerY = 19.dp.toPx()
            val stroke = 5.dp.toPx()
            if (hasPrevious) {
                drawLine(upper, Offset(centerX, 0f), Offset(centerX, centerY), stroke)
            }
            if (hasNext) {
                drawLine(lineColor, Offset(centerX, centerY), Offset(centerX, size.height), stroke)
            }
        },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Subway-map style station marker: white disc with a thick line-colored ring.
        Box(
            modifier = Modifier.padding(top = 3.dp).size(32.dp)
                .background(ring, CircleShape)
                .padding(4.dp)
                .background(colors.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            TransitIcon(mode, ring, Modifier.size(15.dp))
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = if (hasNext) 15.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.titleSmall, fontFamily = JibroDisplay, fontWeight = FontWeight.Bold)
            if (line != null) LineBadge(line, lineColor)
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
internal fun ArrivalDisplay(
    arrival: BoardingArrival?,
    loading: Boolean = false,
    hint: String? = null,
    onDetails: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    if (loading) {
        TransitLoadingCard(TransitLoadingContent.ARRIVAL)
        return
    }
    val live = arrival != null && !arrival.serviceEnded
    val stopCount = arrival?.stopsAway?.takeIf { it > 0 }
    val forecast = arrival?.takeIf { it.status == null }?.seconds?.takeIf { it > 0 }
    val timeLabel = forecast?.let(::formatArrivalDuration)
    val openDetails = onDetails?.takeIf { live }
    val action = if (openDetails == null) Modifier else Modifier.clickable(
        role = Role.Button,
        onClickLabel = "도착 상세 보기",
        onClick = openDetails
    )
    Surface(
        modifier = Modifier.fillMaxWidth().then(action),
        shape = RoundedCornerShape(13.dp),
        color = if (live) colors.primaryContainer else colors.surfaceVariant,
        contentColor = if (live) colors.onPrimaryContainer else colors.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val mainLabel = when {
                arrival == null -> "실시간 도착 정보 없음"
                else -> arrival.status ?: timeLabel ?: if (arrival.seconds == 0) "곧 도착" else "도착 예정시간 미제공"
            }
            if (forecast != null) {
                Text("도착까지", style = MaterialTheme.typography.labelSmall)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    mainLabel,
                    modifier = Modifier.weight(1f),
                    style = if (forecast != null) MaterialTheme.typography.titleLarge
                        else MaterialTheme.typography.titleSmall,
                    fontFamily = if (forecast != null || arrival?.status != null) JibroDisplay else null,
                    fontWeight = FontWeight.Bold
                )
                if (openDetails != null) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            val description = hint?.takeIf(String::isNotBlank) ?: arrival?.line
            if (stopCount != null) {
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
