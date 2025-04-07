package com.example.voltix.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltix.ui.ImagePickerScreen
import com.example.voltix.ui.pages.OnboardingScreen
import com.example.voltix.ui.simulasi.InputPerangkatScreen
import com.example.voltix.ui.simulasi.SimulasiPage
import com.example.voltix.viewmodel.ImageRecognitionViewModel

sealed class Screen(val route: String, val title: String) {
    object Simulation : Screen("simulation", "Simulation")
    object ImagePicker : Screen("image_picker", "Image Picker")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Onboarding : Screen("onboarding", "Onboarding")
    object InputPerangkat : Screen("input_perangkat", "Input Perangkat")
}

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Simulation.route) {
            SimulasiPage(navController)
        }
        composable(Screen.ImagePicker.route) {
            ImagePickerScreen(navController)
        }
        composable(Screen.InputPerangkat.route) {
            InputPerangkatScreen(navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.Simulation.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true } // reset state navigasi agar user tidak bisa kembali ke onboarding dengan tombol back
                }
            })
        }
    }
}


