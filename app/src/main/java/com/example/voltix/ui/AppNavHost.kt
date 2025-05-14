package com.example.voltix.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.voltix.ui.pages.ruangan.DaftarRuanganScreen
import com.example.voltix.ui.pages.OnboardingScreen
import com.example.voltix.ui.pages.googlelens.SearchScreen
import com.example.voltix.ui.pages.rekomendasi.RekomendasiScreen
import com.example.voltix.ui.pages.ruangan.DetailRuangan
import com.example.voltix.ui.pages.ruangan.InputPerangkatScreen
import com.example.voltix.ui.screen.DashboardScreen
import com.example.voltix.ui.screen.SavedSimulationsScreen
import com.example.voltix.ui.screen.SimulasiBebasScreen
import com.example.voltix.ui.screen.SimulasiScreen
import com.example.voltix.ui.screen.SimulationComparisonScreen
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class Screen(val route: String, val title: String = "") {
    object SimulasiPage : Screen("simulasi", "Simulasi")
    object SavedSimulations {
        const val route = "saved_simulations"
    }
    object SimulasiBebas {
        const val route = "simulasi_bebas"
        fun createRoute(simulationId: Int? = null) = if (simulationId != null) {
            "simulasi_bebas?simulationId=$simulationId"
        } else {
            route
        }
    }

    object SimulationComparison {
        const val route = "simulation_comparison"
        fun createRoute(simulationIds: String) = "$route/$simulationIds"
    }
    object DaftarRuangan : Screen("daftar_ruangan", "Daftar Ruangan")
    object DetailRuangan : Screen("detail_ruangan/{ruanganId}", "Detail Ruangan") {
        fun createRoute(ruanganId: Int) = "detail_ruangan/$ruanganId"
    }

    object Rekomendasi : Screen("rekomendasi", "Rekomendasi") {
        fun createRoute(ruanganId: Int): String = "rekomendasi/$ruanganId"
    }

    object ImagePicker : Screen("image_picker/{ruanganId}", "Image Picker") {
        fun createRoute(ruanganId: Int) = "image_picker/$ruanganId"
    }
    object Dashboard : Screen("dashboard", "Dashboard")
    object Onboarding : Screen("onboarding", "Onboarding")
    object InputPerangkat : Screen(
        "input_perangkat/{ruanganId}?deviceName={deviceName}&wattage={wattage}&lumen={lumen}&lampType={lampType}"
    ) {
        fun createRoute(
            ruanganId: Int,
            deviceName: String = "",
            wattage: String = "",
            lumen: String = "",
            lampType: String = ""
        ) = "input_perangkat/$ruanganId?deviceName=$deviceName&wattage=$wattage&lumen=$lumen&lampType=$lampType"
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel = hiltViewModel(), navController = navController)
        }
        composable(Screen.DaftarRuangan.route) {
            DaftarRuanganScreen(navController = navController)
        }

        composable(Screen.SimulasiPage.route) {
            SimulasiScreen(
                navController = navController,
                onSimulasiBebasClick = {
                    navController.navigate(Screen.SimulasiBebas.route)
                }
            )
        }

        composable(Screen.SavedSimulations.route) {
            SavedSimulationsScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = "${Screen.Rekomendasi.route}/{ruanganId}",
            arguments = listOf(navArgument("ruanganId") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("ruanganId") ?: 0
            RekomendasiScreen(ruanganId = id, navController = navController)
        }

        composable(
            route = Screen.SimulasiBebas.route + "?simulationId={simulationId}",
            arguments = listOf(
                navArgument("simulationId") {
                    type = NavType.IntType
                    defaultValue = -1
                    nullable = false
                }
            )
        ) { backStackEntry ->
            SimulasiBebasScreen(
                onDeviceSelect = { device ->
                    println("Selected device: ${device.nama}")
                },
                navController = navController,
                simulationId = backStackEntry.arguments?.getInt("simulationId")?.takeIf { it != -1 },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.SimulationComparison.route) {
            SimulationComparisonScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }

        composable(
            route = Screen.ImagePicker.route,
            arguments = listOf(navArgument("ruanganId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ruanganId = backStackEntry.arguments?.getInt("ruanganId") ?: 0
            SearchScreen(navController = navController, ruanganId = ruanganId)
        }

        composable(
            route = Screen.InputPerangkat.route,
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
                },
                navArgument("lumen") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("lampType") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val ruanganId = backStackEntry.arguments?.getInt("ruanganId") ?: 0
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: ""
            val wattage = backStackEntry.arguments?.getString("wattage") ?: ""
            val lumen = backStackEntry.arguments?.getString("lumen") ?: ""
            val lampType = backStackEntry.arguments?.getString("lampType") ?: ""
            InputPerangkatScreen(
                navController = navController,
                ruanganId = ruanganId,
                initialDeviceName = deviceName,
                initialWattage = wattage,
                initialLumen = lumen,
                initialLampType = lampType
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Dashboard.route) {
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