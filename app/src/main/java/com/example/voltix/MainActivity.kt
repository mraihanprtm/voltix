package com.example.voltix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.ui.MainScreen
import com.example.voltix.ui.components.AppNavigation
import com.example.voltix.ui.theme.VoltixAppTheme
import com.example.voltix.viewmodel.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltixAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val loginViewModel: LoginViewModel = hiltViewModel()
                    AppNavigation(loginViewModel = loginViewModel)
                    MainScreen()
                }
            }
        }
    }
}