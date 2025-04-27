package com.example.voltix.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.voltix.ui.pages.OnboardingScreen
import com.example.voltix.ui.pages.googlelens.SearchScreen
import com.example.voltix.ui.simulasi.InputPerangkatScreen
import com.example.voltix.ui.simulasi.SimulasiPage
import com.example.voltix.viewmodel.googlelens.SearchViewModel

sealed class Screen(val route: String, val title: String) {
    object Simulation : Screen("simulation", "Simulation")
    object ImagePicker : Screen("image_picker", "Image Picker")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Onboarding : Screen("onboarding", "Onboarding")
    object InputPerangkat : Screen("input_perangkat?deviceName={deviceName}&wattage={wattage}", "Input Perangkat") {
        fun createRoute(deviceName: String, wattage: String): String {
            return "input_perangkat?deviceName=${deviceName}&wattage=${wattage}"
        }
    }
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
        composable(Screen.ImagePicker.route) { backStackEntry ->
            val viewModel: SearchViewModel = hiltViewModel(backStackEntry)
            SearchScreen(navController, viewModel)
        }

        composable(
            route = Screen.InputPerangkat.route,
            arguments = listOf(
                navArgument("deviceName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("wattage") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            InputPerangkatScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.InputPerangkat.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true } // reset state navigasi agar user tidak bisa kembali ke onboarding dengan tombol back
                }
            })
        }
    }
}


