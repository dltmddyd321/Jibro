package com.windrr.jibrro.presentation.commute

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal enum class TransitLoadingContent(val label: String) {
    LOCATION("집으로 가는 경로를 준비 중이에요."),
    ROUTES("집으로 가는 경로를 찾는 중이에요."),
    ARRIVAL("실시간 도착 확인 중"),
    STATION("실시간 도착 정보 수신 중"),
    HOME_SEARCH("주소와 역을 검색 중이에요.")
}

@Composable
internal fun TransitLoadingCard(content: TransitLoadingContent) {
    val colors = MaterialTheme.colorScheme
    val pulse = rememberInfiniteTransition(label = "Loading skeleton pulse").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "Skeleton opacity"
    )
    val inline = content == TransitLoadingContent.ARRIVAL
    val shape = RoundedCornerShape(if (inline) 13.dp else 18.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = if (inline) colors.surfaceVariant else colors.surface,
        border = if (inline) null else BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(
            Modifier.padding(if (inline) 11.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                content.label,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
            when (content) {
                TransitLoadingContent.LOCATION, TransitLoadingContent.ROUTES -> repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBlock(pulse, Modifier.size(32.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SkeletonBlock(pulse, Modifier.fillMaxWidth(0.8f).height(13.dp))
                            SkeletonBlock(pulse, Modifier.fillMaxWidth(0.47f).height(9.dp))
                        }
                    }
                }
                TransitLoadingContent.STATION -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBlock(pulse, Modifier.size(32.dp))
                        SkeletonBlock(pulse, Modifier.fillMaxWidth(0.4f).height(13.dp))
                    }
                    SkeletonBlock(pulse, Modifier.fillMaxWidth(0.62f).height(20.dp))
                    SkeletonBlock(pulse, Modifier.fillMaxWidth(0.35f).height(10.dp))
                }
                TransitLoadingContent.ARRIVAL -> {
                    SkeletonBlock(pulse, Modifier.fillMaxWidth(0.65f).height(19.dp))
                    SkeletonBlock(pulse, Modifier.fillMaxWidth(0.38f).height(10.dp))
                }
                TransitLoadingContent.HOME_SEARCH -> {
                    SkeletonBlock(pulse, Modifier.fillMaxWidth(0.82f).height(12.dp))
                    SkeletonBlock(pulse, Modifier.fillMaxWidth(0.59f).height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(pulse: State<Float>, modifier: Modifier) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier.drawBehind {
        drawRoundRect(color = tint.copy(alpha = 0.2f * pulse.value), cornerRadius = CornerRadius(5.dp.toPx()))
    })
}
