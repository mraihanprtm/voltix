package com.example.voltix.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.voltix.ui.pages.OnboardingScreen
import com.example.voltix.ui.pages.auth.LoginScreen
import com.example.voltix.ui.pages.auth.RegisterScreen
import com.example.voltix.ui.pages.googlelens.SearchScreen
import com.example.voltix.ui.pages.rekomendasi.RekomendasiScreen
import com.example.voltix.ui.pages.ruangan.DaftarRuanganScreen
import com.example.voltix.ui.pages.ruangan.DetailRuangan
import com.example.voltix.ui.pages.ruangan.InputPerangkatScreen
import com.example.voltix.ui.pages.setting.SettingScreen
import com.example.voltix.ui.screen.DashboardScreen
import com.example.voltix.ui.screen.SimulasiBebasScreen
import com.example.voltix.ui.screen.SimulasiScreen
import com.example.voltix.ui.screen.SimulationComparisonScreen
import com.example.voltix.util.DataStoreUtil
import com.example.voltix.viewmodel.UserViewModel
import com.example.voltix.viewmodel.auth.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String = "") {
    object Login : Screen("login", "Login")
    object Setting : Screen("setting", "Setting")
    object Register : Screen("register", "Register")
    object SimulasiPage : Screen("simulasi", "Simulasi")
    object SimulasiBebas {
        const val route = "simulasi_bebas"
        fun createRoute(simulationId: Int? = null) = if (simulationId != null) {
            "simulasi_bebas?simulationId=$simulationId"
        } else {
            route
        }
    }
    object SimulationComparison : Screen("simulation_comparison", "Simulation Comparison")
    object DaftarRuangan : Screen("daftar_ruangan", "Ruangan")
    object DetailRuangan : Screen("detail_ruangan/{ruanganId}", "Detail Ruangan") {
        fun createRoute(ruanganId: Int) = "detail_ruangan/$ruanganId"
    }
    object Rekomendasi : Screen("rekomendasi?ruanganId={ruanganId}", "Rekomendasi") {
        fun createRoute(ruanganId: Int? = null): String =
            if (ruanganId != null && ruanganId != -1) "rekomendasi?ruanganId=$ruanganId" else "rekomendasi"
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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AppNavHost(navController: NavHostController, loginViewModel: LoginViewModel = hiltViewModel()) {
    val loginState by loginViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginViewModel.LoginState.Error -> {
                // Handled in LoginScreen
            }
            else -> {}
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Onboarding.route) {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            OnboardingScreen(
                onFinish = {
                    coroutineScope.launch {
                        DataStoreUtil.saveOnboardingCompleted(context, true)
                        navController.navigate(if (FirebaseAuth.getInstance().currentUser != null) Screen.Dashboard.route else Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                loginViewModel = loginViewModel,
                navigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                registerViewModel = hiltViewModel(),
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel = hiltViewModel(), navController = navController)
        }
        composable(
            route = "daftar_ruangan?openDialog={openDialog}",
            arguments = listOf(
                navArgument("openDialog") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val openDialog = backStackEntry.arguments?.getBoolean("openDialog") ?: false
            DaftarRuanganScreen(
                navController = navController,
                openDialog = openDialog
            )
        }
        composable(Screen.SimulasiPage.route) {
            SimulasiScreen(
                navController = navController,
                onSimulasiBebasClick = {
                    navController.navigate(Screen.SimulasiBebas.route)
                }
            )
        }
        composable(Screen.Setting.route) {
            val userViewModel: UserViewModel = hiltViewModel() // Inject UserViewModel
            SettingScreen(
                navController = navController,
                userViewModel = userViewModel, // Pass the injected ViewModel
                onLogOutClick = {
                    FirebaseAuth.getInstance().signOut()
                    loginViewModel.resetLoginState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.Rekomendasi.route,
            arguments = listOf(
                navArgument("ruanganId") {
                    type = NavType.IntType
                    defaultValue = -1
                    nullable = false
                }
            )
        ) { backStack ->
            val id = backStack.arguments?.getInt("ruanganId") ?: -1
            RekomendasiScreen(ruanganId = id, navController = navController)
        }
        composable(
            route = "${Screen.SimulasiBebas.route}?simulationId={simulationId}",
            arguments = listOf(
                navArgument("simulationId") {
                    type = NavType.IntType
                    defaultValue = -1
                    nullable = false
                }
            )
        ) { backStackEntry ->
            SimulasiBebasScreen(
                onDeviceSelect = { device -> println("Selected device: ${device.nama}") },
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
                navArgument("ruanganId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("deviceName") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("wattage") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("lumen") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("lampType") { type = NavType.StringType; defaultValue = ""; nullable = true }
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
        composable(
            route = Screen.DetailRuangan.route,
            arguments = listOf(
                navArgument("ruanganId") { type = NavType.IntType }
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