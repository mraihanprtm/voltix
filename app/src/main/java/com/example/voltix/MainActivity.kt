package com.example.voltix

import SimulasiViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.voltix.data.Perangkat
import com.example.voltix.data.SimulasiRepository
import com.example.voltix.data.SimulasiViewModelFactory
import com.example.voltix.ui.theme.AppNavHost
import com.example.voltix.ui.theme.VoltixTheme
import com.example.voltix.viewmodel.PerangkatViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltixTheme {
                val context = LocalContext.current
                val repository = SimulasiRepository.getInstance(context)
                val factory = SimulasiViewModelFactory(repository)
                val simulasiViewModel: SimulasiViewModel = viewModel(factory = factory)
                val perangkatViewModel: PerangkatViewModel = viewModel()
                AppNavHost(
                    perangkatViewModel = perangkatViewModel,
                    simulasiViewModel = simulasiViewModel
                )
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


