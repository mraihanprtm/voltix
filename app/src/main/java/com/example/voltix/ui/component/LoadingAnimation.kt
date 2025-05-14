package com.example.voltix.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltix.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun LoadingAnimationSection(isLoading: Boolean) {
    val loadingAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))

    if (isLoading) {
        LottieAnimation(
            composition = loadingAnimation,
            modifier = Modifier.size(100.dp), // Adjust size as needed
            iterations = LottieConstants.IterateForever // Loop the animation
        )
    }
}