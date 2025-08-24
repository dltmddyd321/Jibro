package com.windrr.jibrro.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    speed: Float = 50f, // px/second
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val textWidth = remember { mutableStateOf(0f) }
    val containerWidth = remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .clipToBounds() // 박스 밖으로 나가는 텍스트는 잘림
            .onGloballyPositioned { containerWidth.value = it.size.width.toFloat() }
    ) {
        Text(
            text = text,
            style = textStyle,
            modifier = Modifier
                .onGloballyPositioned { textWidth.value = it.size.width.toFloat() }
                .offset { IntOffset(offsetX.value.toInt(), 0) }
        )
    }

    LaunchedEffect(textWidth.value, containerWidth.value) {
        if (textWidth.value <= containerWidth.value) return@LaunchedEffect
        val distance = textWidth.value + containerWidth.value
        offsetX.snapTo(containerWidth.value) // 시작 위치 (오른쪽 밖)
        while (true) {
            offsetX.animateTo(
                targetValue = -textWidth.value, // 왼쪽 끝까지
                animationSpec = tween(
                    durationMillis = ((distance / speed) * 1000).toInt(),
                    easing = LinearEasing
                )
            )
            offsetX.snapTo(containerWidth.value) // 다시 오른쪽으로
        }
    }
}