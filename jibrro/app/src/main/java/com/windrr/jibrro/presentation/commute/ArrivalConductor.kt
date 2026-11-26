package com.windrr.jibrro.presentation.commute

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.R
import com.windrr.jibrro.presentation.ui.theme.Signage
import kotlin.math.roundToInt

@Composable
internal fun ArrivalConductor() {
    val train = ImageBitmap.imageResource(R.drawable.jibro_train_cat)
    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasEntered = true }
    val approach = animateFloatAsState(if (hasEntered) 1f else 0.48f, tween(1350), label = "Train approach")
    val wave = rememberInfiniteTransition(label = "Conductor wave").animateFloat(
        initialValue = -11f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(460), RepeatMode.Reverse),
        label = "Flag swing"
    )

    Canvas(
        Modifier.fillMaxWidth().height(164.dp)
            .clip(RoundedCornerShape(22.dp)).background(Signage.Tunnel)
    ) {
        val center = size.width / 2
        val horizon = size.height * 0.54f
        val mint = Signage.SoftSky
        val rail = Signage.SafetyYellow
        drawRect(Signage.NavyDeep, Offset(0f, horizon), Size(size.width, size.height - horizon))
        for (side in listOf(-1, 1)) {
            drawLine(
                rail.copy(alpha = 0.75f),
                Offset(center + side * size.height * 0.14f, horizon),
                Offset(center + side * size.height * 0.70f, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
        for (index in 1..5) {
            val depth = index * index / 25f
            val span = size.height * (0.15f + 0.54f * depth)
            val y = horizon + (size.height - horizon) * depth
            drawLine(mint.copy(alpha = 0.2f), Offset(center - span, y), Offset(center + span, y), 2.dp.toPx())
        }
        listOf(0.12f to 0.31f, 0.24f to 0.19f, 0.78f to 0.28f, 0.87f to 0.45f).forEach { (x, y) ->
            drawRect(mint.copy(alpha = 0.5f), Offset(size.width * x, size.height * y), Size(3.dp.toPx(), 3.dp.toPx()))
        }

        val height = (size.height * 0.94f * approach.value).roundToInt().coerceAtLeast(1)
        val width = (height.toFloat() * train.width / train.height).roundToInt().coerceAtLeast(1)
        val left = (center - width / 2f).roundToInt()
        val top = (horizon + size.height * 0.43f * approach.value - height).roundToInt()
        drawImage(train, dstOffset = IntOffset(left, top), dstSize = IntSize(width, height), filterQuality = FilterQuality.None)

        val grip = Offset(left + width * 0.645f, top + height * 0.53f)
        val pixel = height / 47f
        rotate(wave.value, pivot = grip) {
            val pole = Offset(grip.x + pixel, grip.y - 14 * pixel)
            drawRect(Signage.NavyDeep, pole - Offset(pixel, pixel), Size(4 * pixel, 15 * pixel))
            drawRect(Color(0xFFFFE7AB), pole, Size(pixel * 1.4f, pixel * 13f))
            drawRect(Color(0xFF642F30), pole + Offset(pixel, -pixel), Size(12 * pixel, 8 * pixel))
            drawRect(Color(0xFFFF916F), pole + Offset(2 * pixel, 0f), Size(10 * pixel, 6 * pixel))
            drawRect(Color(0xFFFFC395), pole + Offset(2 * pixel, pixel), Size(8 * pixel, pixel))
        }
    }
}
