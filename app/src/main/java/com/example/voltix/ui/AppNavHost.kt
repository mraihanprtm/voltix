package com.example.voltix.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import SimulasiViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltix.ui.ImagePickerScreen
import com.example.voltix.ui.simulasi.InputPerangkatScreen
import com.example.voltix.ui.simulasi.SimulasiPage
import com.example.voltix.viewmodel.ImageRecognitionViewModel

sealed class Screen(val route: String, val title: String) {
    object Simulation : Screen("simulation", "Simulation")
    object ImagePicker : Screen("image_picker", "Image Picker")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Onboarding : Screen("onboarding", "Onboarding")
    object InputData : Screen("inputdata", "Input Data")
}

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.InputData.route
    ) {
        composable(Screen.Simulation.route) {
            SimulasiPage(navController)
        }
        composable(Screen.ImagePicker.route) {
            ImagePickerScreen(navController )
        }
        composable(Screen.InputData.route){
            InputPerangkatScreen(navController)
        }
    }
}


