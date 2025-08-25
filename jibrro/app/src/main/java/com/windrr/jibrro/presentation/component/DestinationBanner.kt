package com.windrr.jibrro.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun DestinationBanner(
    destinationName: String,
    modifier: Modifier = Modifier,
    speed: Float = 100f
) {
    val marqueeText = ">>>>>"
    val textWidth = remember { mutableFloatStateOf(0f) }
    val containerWidth = remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF76FF03))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clipToBounds()
            .onGloballyPositioned { containerWidth.floatValue = it.size.width.toFloat() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$destinationName 으로 이동 중",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds()
            ) {
                Text(
                    text = marqueeText,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .onGloballyPositioned { textWidth.floatValue = it.size.width.toFloat() }
                        .offset { IntOffset(offsetX.value.toInt(), 0) }
                )
            }
        }
    }

    LaunchedEffect(textWidth.floatValue, containerWidth.floatValue) {
        if (textWidth.floatValue == 0f || containerWidth.floatValue == 0f) return@LaunchedEffect
        offsetX.snapTo(containerWidth.floatValue)
        while (true) {
            offsetX.animateTo(
                targetValue = -textWidth.floatValue,
                animationSpec = tween(
                    durationMillis = ((containerWidth.floatValue + textWidth.floatValue) / speed * 1000).toInt(),
                    easing = LinearEasing
                )
            )
            offsetX.snapTo(containerWidth.floatValue)
        }
    }
}
