package com.example.voltix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.ui.component.AppNavigation
import com.example.voltix.ui.screen.SimulasiScreen
import com.example.voltix.ui.theme.VoltixTheme
import com.example.voltix.viewmodel.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltixTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val loginViewModel: LoginViewModel = hiltViewModel()
                    AppNavigation(loginViewModel = loginViewModel)
//                    SimulasiScreen(
//                        onSimulasiBebasClick = { println("Simulasi Bebas dipilih") },
//                        onSimulasiBerdasarkanRuanganClick = { println("Simulasi Berdasarkan Ruangan dipilih") }
//                    )

                }
            }
        }
    }
}