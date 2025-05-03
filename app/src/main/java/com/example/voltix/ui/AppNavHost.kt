package com.example.voltix.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.voltix.ui.pages.ruangan.DaftarRuanganScreen
import com.example.voltix.ui.pages.OnboardingScreen
import com.example.voltix.ui.pages.googlelens.SearchScreen
import com.example.voltix.ui.pages.ruangan.DetailRuangan
//import com.example.voltix.ui.pages.simulasi.SimulasiPage
import com.example.voltix.ui.pages.ruangan.InputPerangkatScreen
import com.example.voltix.ui.screen.SimulasiBebasScreen
import com.example.voltix.ui.screen.SimulasiBerdasarkanRuanganScreen
import com.example.voltix.ui.screen.SimulasiScreen

sealed class Screen(val route: String, val title: String = "") {
    object SimulasiPage : Screen("simulasi", "Simulasi")
    object SimulasiBebas : Screen("simulasi_bebas", "Simulasi Bebas")
    object SimulasiBerdasarkanRuangan : Screen("simulasi_berdasarkan_ruangan", "Simulasi Berdasarkan Ruangan")
    object DaftarRuangan : Screen("daftar_ruangan", "Daftar Ruangan")
    object DetailRuangan : Screen("detail_ruangan/{ruanganId}", "Detail Ruangan") {
        fun createRoute(ruanganId: Int) = "detail_ruangan/$ruanganId"
    }
    object ImagePicker : Screen("image_picker/{ruanganId}", "Image Picker") {
        fun createRoute(ruanganId: Int): String {
            return "image_picker/$ruanganId"
        }
    }


    object Dashboard : Screen("dashboard", "Dashboard")
    object Onboarding : Screen("onboarding", "Onboarding")
    object InputPerangkat : Screen(
        "input_perangkat?ruanganId={ruanganId}&deviceName={deviceName}&wattage={wattage}",
        "Input Perangkat"
    ) {
        fun createRoute(
            ruanganId: Int,
            deviceName: String = "",
            wattage: String = ""
        ): String {
            return "input_perangkat?ruanganId=$ruanganId&deviceName=${Uri.encode(deviceName)}&wattage=${Uri.encode(wattage)}"
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {

        composable(Screen.DaftarRuangan.route) {
            DaftarRuanganScreen(navController = navController)
        }

        composable(Screen.SimulasiPage.route) {
            SimulasiScreen(
                onSimulasiBebasClick = {
                    navController.navigate(Screen.SimulasiBebas.route)
                },
                onSimulasiBerdasarkanRuanganClick = {
                    navController.navigate(Screen.SimulasiBerdasarkanRuangan.route)
                }
            )
        }

        composable(Screen.SimulasiBebas.route) {
            SimulasiBebasScreen(
                onPerangkatSelect = { /* Handle device selection if needed */ }
            )
        }

        composable(Screen.SimulasiBerdasarkanRuangan.route) {
            SimulasiBerdasarkanRuanganScreen(
                onRoomSelect = { roomName, devices ->
                    // Optional: Handle room selection, e.g., show a dialog or navigate elsewhere
                    println("Selected room: $roomName with ${devices.size} devices")
                }
            )
        }


        composable(Screen.ImagePicker.route) {
            SearchScreen(
                navController = navController)
        }

        composable(
            route = "input_perangkat?ruanganId={ruanganId}&deviceName={deviceName}&wattage={wattage}",
            arguments = listOf(
                navArgument("ruanganId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
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
        ) { backStackEntry ->
            val ruanganId = backStackEntry.arguments?.getInt("ruanganId") ?: 0
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: ""
            val wattage = backStackEntry.arguments?.getString("wattage") ?: ""

            InputPerangkatScreen(
                navController = navController,
                ruanganId = ruanganId,
                initialDeviceName = deviceName,
                initialWattage = wattage
            )
        }


        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.DaftarRuangan.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DetailRuangan.route,
            arguments = listOf(
                navArgument("ruanganId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val ruanganId = backStackEntry.arguments?.getInt("ruanganId") ?: 0
            DetailRuangan(
                navController = navController,
                ruanganId = ruanganId
            )
        }
    }
}
