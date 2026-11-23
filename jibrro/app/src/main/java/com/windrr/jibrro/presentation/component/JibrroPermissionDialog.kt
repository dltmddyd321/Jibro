package com.windrr.jibrro.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.presentation.commute.TransitIcon

@Composable
internal fun JibrroPermissionDialog(
    title: String,
    mode: TransitMode?,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.background,
        tonalElevation = 0.dp,
        icon = {
            Surface(shape = RoundedCornerShape(14.dp), color = colors.primaryContainer) {
                Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                    TransitIcon(mode, colors.onPrimaryContainer, Modifier.size(24.dp))
                }
            }
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) { content() }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }
    )
}

@Composable
internal fun PermissionAction(
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            TextButton(onClick = onClick, modifier = Modifier.align(Alignment.End)) { Text(action) }
        }
    }
}
