package com.windrr.jibrro.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.windrr.jibrro.presentation.ui.theme.JibroDisplay
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.R
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme
import com.windrr.jibrro.presentation.ui.theme.Signage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun JibrroSubpage(title: String, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    JibrroTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(title, fontFamily = JibroDisplay, fontWeight = FontWeight.Bold) },
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
    Surface(shape = RoundedCornerShape(20.dp), color = Signage.Navy, contentColor = Signage.Paper) {
        Box(Modifier.fillMaxWidth().heightIn(min = 104.dp)) {
            Column(Modifier.matchParentSize()) {
                Spacer(Modifier.weight(1f))
                PlatformEdge()
            }
            Image(
                painter = painterResource(R.drawable.jibro_train_cat),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 9.dp).height(84.dp)
            )
            Column(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 112.dp, top = 15.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontFamily = JibroDisplay, fontWeight = FontWeight.Bold)
                description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Signage.SoftSky) }
            }
        }
    }
}
