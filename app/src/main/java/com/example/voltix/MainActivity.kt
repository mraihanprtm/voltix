package com.example.voltix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.voltix.ui.pages.auth.LoginScreen
import com.example.voltix.ui.pages.home.HomeScreen
import com.example.voltix.ui.theme.VoltixAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            VoltixTheme {
//                val context = LocalContext.current
//                val repository = SimulasiRepository.getInstance(context)
//                val factory = SimulasiViewModelFactory(repository)
//                val simulasiViewModel: SimulasiViewModel = viewModel(factory = factory)
//                val perangkatViewModel: PerangkatViewModel = viewModel()
//                AppNavHost(
//                    perangkatViewModel = perangkatViewModel,
//                    simulasiViewModel = simulasiViewModel
//                )
//                MainScreen()
            VoltixAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val loginViewModel: LoginViewModel = hiltViewModel()
                    AppNavigation(loginViewModel = loginViewModel)
                }
            }
        }
    }
}