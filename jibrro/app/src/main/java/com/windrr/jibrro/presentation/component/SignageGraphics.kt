package com.windrr.jibrro.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.presentation.ui.theme.Signage
import com.windrr.jibrro.presentation.ui.theme.onLineColor

/** Yellow tactile-paving strip, like the edge of a subway platform. */
@Composable
internal fun PlatformEdge(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(14.dp)) {
        drawRect(Signage.SafetyYellow)
        val dot = 3.dp.toPx()
        val gap = 9.dp.toPx()
        val bump = Signage.SafetyYellow.copy(red = 0.86f, green = 0.66f, blue = 0f)
        var x = gap / 2
        var row = 0
        while (x < size.width) {
            for (y in 0..1) {
                val offset = if ((row + y) % 2 == 0) 0f else gap / 2
                val cy = size.height * (0.3f + 0.42f * y)
                drawRect(bump, Offset(x + offset - dot / 2, cy - dot / 2), Size(dot, dot))
            }
            x += gap
            row++
        }
    }
}

/** Filled rounded badge in the official line color, e.g. "2호선". */
@Composable
internal fun LineBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(50), color = color, contentColor = onLineColor(color)) {
        Text(
            text,
            Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
