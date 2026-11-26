package com.windrr.jibrro.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.glance.color.ColorProvider

val Bg = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF121212))
val Surface = ColorProvider(day = Color(0xFFF7F8FA), night = Color(0xFF1E1E1E))
val OnBg = ColorProvider(day = Color(0xFF1B1B1B), night = Color(0xFFECECEC))
val Subtle = ColorProvider(day = Color(0xFF6B7280), night = Color(0xFFB0B7C3))
val AccentBlue = ColorProvider(day = Color(0xFF03A9F4), night = Color(0xFF81D4FA))
val Danger = ColorProvider(day = Color(0xFFD32F2F), night = Color(0xFFFFCDD2))

/** Fixed signage colors used regardless of light/dark mode (like real station signs). */
object Signage {
    val Navy = Color(0xFF14284A)
    val NavyDeep = Color(0xFF0C1B33)
    val Tunnel = Color(0xFF101C30)
    val Paper = Color(0xFFFDFBF3)
    val SafetyYellow = Color(0xFFFFCC1E)
    val OnSafetyYellow = Color(0xFF231A00)
    val SoftSky = Color(0xFFBFD6F2)
    val Bus = Color(0xFF2B6CC4)
}

private val lineColors = listOf(
    "신분당" to Color(0xFFD4003B),
    "수인분당" to Color(0xFFF5A200),
    "분당" to Color(0xFFF5A200),
    "인천1" to Color(0xFF7CA8D5),
    "인천2" to Color(0xFFED8B00),
    "경의중앙" to Color(0xFF77C4A3),
    "중앙" to Color(0xFF77C4A3),
    "공항" to Color(0xFF0090D2),
    "경춘" to Color(0xFF0C8E72),
    "경강" to Color(0xFF003DA5),
    "우이신설" to Color(0xFFB0CE18),
    "서해" to Color(0xFF8FC31F),
    "신림" to Color(0xFF6789CA),
    "김포" to Color(0xFFA17800),
    "의정부" to Color(0xFFFDA600),
    "에버라인" to Color(0xFF509F22),
    "GTX" to Color(0xFF9A6292),
    "1호선" to Color(0xFF0052A4),
    "2호선" to Color(0xFF00A84D),
    "3호선" to Color(0xFFEF7C1C),
    "4호선" to Color(0xFF00A5DE),
    "5호선" to Color(0xFF996CAC),
    "6호선" to Color(0xFFCD7C2F),
    "7호선" to Color(0xFF747F00),
    "8호선" to Color(0xFFE6186C),
    "9호선" to Color(0xFFBDB092)
)

/** Official Seoul-metro-area line color for a line name, or null when unknown. */
fun subwayLineColor(name: String?): Color? {
    val key = name?.replace(" ", "")?.takeIf(String::isNotEmpty) ?: return null
    if (key.startsWith("부산") || key.startsWith("대구") || key.startsWith("대전") || key.startsWith("광주")) return null
    return lineColors.firstOrNull { (token, _) -> key.contains(token) }?.second
}

/** Readable text color on top of a line color. */
fun onLineColor(color: Color): Color = if (color.luminance() > 0.45f) Color(0xFF1A1A1A) else Color.White
