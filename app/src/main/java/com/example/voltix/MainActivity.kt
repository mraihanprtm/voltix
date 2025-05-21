package com.example.voltix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.voltix.ui.AppNavHost
import com.example.voltix.ui.MainScreen
import com.example.voltix.ui.screen.SimulasiScreen
import com.example.voltix.ui.theme.VoltixTheme
import com.example.voltix.viewmodel.auth.LoginViewModel
import com.example.voltix.viewmodel.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltixTheme {
                MainScreen()
            }
        }
    }
}