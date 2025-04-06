package com.example.voltix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.voltix.ui.pages.auth.LoginScreen
import com.example.voltix.ui.theme.VoltixAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltixAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
//                    LoginScreen()
                }
            }
        }
    }
}