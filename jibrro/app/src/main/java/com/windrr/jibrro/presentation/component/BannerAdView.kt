package com.windrr.jibrro.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    var isAdFailed by remember { mutableStateOf(false) }
    
    if (!isAdFailed) {
//        AndroidView(
//            modifier = modifier
//                .fillMaxWidth()
//                .height(50.dp),
//            factory = { context ->
//                AdView(context).apply {
//                    setAdSize(AdSize.BANNER)
//                    adUnitId = "ca-app-pub-2156013754929909/4254063400"
//                    adListener = object : AdListener() {
//                        override fun onAdLoaded() {
//                            Log.i("AdView", "Ad loaded")
//                            isAdFailed = false
//                        }
//
//                        override fun onAdFailedToLoad(error: LoadAdError) {
//                            Log.e("AdView", "Ad failed: ${error.message}")
//                            isAdFailed = true
//                        }
//                    }
//                    loadAd(AdRequest.Builder().build())
//                }
//            }
//        )
    }
}