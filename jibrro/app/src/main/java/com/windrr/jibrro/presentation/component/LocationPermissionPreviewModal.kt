package com.windrr.jibrro.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.windrr.jibrro.R

@Composable
fun LocationPermissionPreviewModal(
    screenshots: List<Int>,
    title: String,
    body: String,
    agreeButtonText: String = "동의하고 계속",
    exitButtonText: String = "앱 종료",
    onAgree: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pageCount = screenshots.size.coerceAtLeast(1)
        val pagerState = rememberPagerState(pageCount = { pageCount })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 12.dp
        ) { page ->
            val resId = screenshots.getOrNull(page) ?: 0

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "기능 예시 스크린샷 ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0x11000000)))
                }
            }
        }

        // 페이지 인디케이터
        Row(
            Modifier.padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                val active = pagerState.currentPage == index
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(if (active) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) MaterialTheme.colorScheme.primary else Color(0x33000000))
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "명시적 공개 제목" }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "명시적 공개 본문" }
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f)
            ) { Text(exitButtonText) }

            Button(
                onClick = onAgree,
                modifier = Modifier.weight(1f)
            ) { Text(agreeButtonText) }
        }
    }
}

@Composable
fun DisclosureSample(
    onAgree: () -> Unit,
    onExit: () -> Unit,
) {
    LocationPermissionPreviewModal(
        screenshots = listOf(
            R.drawable.first,
            R.drawable.second,
            R.drawable.third
        ),
        title = "백그라운드 위치 사용 안내",
        body = "이 기능은 지오펜스 알림 제공을 위해 위치를 사용합니다. 앱을 사용하지 않을 때에도 주기적으로 기기 위치를 확인합니다. 설정에서 언제든 변경할 수 있습니다",
        onAgree = onAgree,
        onExit = onExit
    )
}

@Preview
@Composable
fun DisclosureTest() {
    DisclosureSample(onAgree = {

    }) { }
}