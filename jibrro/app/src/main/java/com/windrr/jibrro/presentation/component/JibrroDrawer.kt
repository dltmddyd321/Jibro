package com.windrr.jibrro.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun JibrroDrawer(
    onClose: () -> Unit,
    onFavorites: () -> Unit,
    onDestination: () -> Unit,
    onSettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(0.9f),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = colors.background
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Close, contentDescription = "메뉴 닫기")
            }
            FeatureHeader("Jibro")
            DrawerAction("즐겨찾기", Icons.Default.FavoriteBorder, onFavorites)
            DrawerAction("목적지 알림", Icons.Default.LocationOn, onDestination)
            DrawerAction("설정", Icons.Default.Settings, onSettings)
        }
    }
}

@Composable
private fun DrawerAction(title: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(11.dp), color = colors.primaryContainer) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer)
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
