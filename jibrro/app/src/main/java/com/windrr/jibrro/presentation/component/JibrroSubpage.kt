package com.windrr.jibrro.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.R
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun JibrroSubpage(title: String, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    JibrroTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            content = content
        )
    }
}

@Composable
internal fun FeatureHeader(title: String, description: String? = null) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF164D3E), contentColor = Color(0xFFFFFAE8)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            Image(
                painter = painterResource(R.drawable.jibro_train_cat),
                contentDescription = null,
                modifier = Modifier.weight(0.43f).heightIn(max = 95.dp)
            )
        }
    }
}
