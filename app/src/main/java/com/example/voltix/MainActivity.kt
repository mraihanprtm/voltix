package com.example.voltix

import SimulasiViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltix.ui.AppNavHost
import com.example.voltix.ui.MainScreen
import com.example.voltix.ui.theme.VoltixTheme
import com.example.voltix.viewmodel.ImageRecognitionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltixTheme {
//                val simulasiViewModel: SimulasiViewModel = viewModel()
//                val imageRecognitionViewModel: ImageRecognitionViewModel = viewModel()
//                AppNavHost(
//                    simulasiViewModel = simulasiViewModel,
//                    imageRecognitionViewModel = imageRecognitionViewModel
//                )
                MainScreen()
            }
        }
    }
}

//@Composable
//fun MyApp(
//    SimulasiViewModel: SimulasiViewModel = viewModel(),
//    ImageRecognitionViewModel: ImageRecognitionViewModel = viewModel()
//) {
////    var showSplash by remember { mutableStateOf(true) }
////
////    LaunchedEffect(Unit) {
////        delay(4000) // Tampilkan SplashScreen selama 4 detik
////        showSplash = false
////    }
//    AppNavHost(SimulasiViewModel = SimulasiViewModel, ImageRecognitionViewModel = ImageRecognitionViewModel)
////    if (showSplash) {
////        SplashScreen()
////    } else {
////        AppNavHost(dataViewModel = dataViewModel, lamaSekolahViewModel = lamaSekolahViewModel)
////    }
//}


