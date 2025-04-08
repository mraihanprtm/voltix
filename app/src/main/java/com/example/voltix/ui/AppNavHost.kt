package com.example.voltix.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.voltix.ui.pages.OnboardingScreen
import com.example.voltix.ui.pages.googlelens.SearchScreen
import com.example.voltix.ui.simulasi.InputPerangkatScreen
import com.example.voltix.ui.simulasi.SimulasiPage

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
            SearchScreen(navController)
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


