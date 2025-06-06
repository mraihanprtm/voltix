package com.example.voltix.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.voltix.data.remote.AuthManager
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
import kotlinx.coroutines.flow.first // Import first untuk mengambil nilai tunggal dari Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Sealed class untuk mendefinisikan rute navigasi dalam aplikasi.
 */
sealed class Screen(val route: String, val title: String = "") {
    data object Login : Screen("login", "Login")
    data object Setting : Screen("setting", "Setting")
    data object Register : Screen("register", "Register")
    data object SimulasiPage : Screen("simulasi", "Simulasi")
    data object SimulasiBebas {
        const val route = "simulasi_bebas"
        fun createRoute(simulationId: Int? = null) = if (simulationId != null) {
            "simulasi_bebas?simulationId=$simulationId"
        } else {
            route
        }
    }
    data object SimulationComparison : Screen("simulation_comparison", "Simulation Comparison")
    data object DaftarRuangan : Screen("daftar_ruangan", "Ruangan")
    data object DetailRuangan : Screen("detail_ruangan/{ruanganId}", "Detail Ruangan") {
        fun createRoute(ruanganId: Int) = "detail_ruangan/$ruanganId"
    }
    data object Rekomendasi : Screen("rekomendasi?ruanganId={ruanganId}", "Rekomendasi") {
        fun createRoute(ruanganId: Int? = null): String =
            if (ruanganId != null && ruanganId != -1) "rekomendasi?ruanganId=$ruanganId" else "rekomendasi"
    }
    data object ImagePicker : Screen("image_picker/{ruanganId}", "Image Picker") {
        fun createRoute(ruanganId: Int) = "image_picker/$ruanganId"
    }
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object InputPerangkat : Screen(
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

/**
 * Composable utama yang mendefinisikan struktur navigasi aplikasi.
 * Menentukan rute dan layar untuk setiap tujuan.
 */
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AppNavHost(navController: NavHostController, loginViewModel: LoginViewModel = hiltViewModel()) {
    val loginState by loginViewModel.loginState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // LaunchedEffect untuk menangani navigasi setelah login berhasil (dari LoginViewModel)
    LaunchedEffect(loginState) {
        Log.d("AppNavHost", "LaunchedEffect(loginState): Current state is $loginState")
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.isEmailVerified) {
                    val isOnboardingCompleted = DataStoreUtil.isOnboardingCompleted(context).first()
                    Log.d("AppNavHost", "LoginState.Success: Onboarding completed status: $isOnboardingCompleted")
                    val destination = if (isOnboardingCompleted) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Onboarding.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    Log.i("AppNavHost", "Navigating to $destination after successful login and email verified.")
                } else {
                    Log.w("AppNavHost", "LoginState.Success but email not verified or user null, staying on Login.")
                    loginViewModel.resetLoginState()
                }
            }
            is LoginViewModel.LoginState.Error -> {
                Log.e("AppNavHost", "LoginState.Error detected: ${(loginState as LoginViewModel.LoginState.Error).message}")
                // Pastikan tetap di LoginScreen
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {
                // Idle atau Loading, tidak ada navigasi
            }
        }
    }

    // Penentuan startDestination awal aplikasi saat pertama kali dibuka
    val startDestination = remember {
        val user = FirebaseAuth.getInstance().currentUser
        val initialRoute = if (user != null && user.isEmailVerified) {
            val isOnboardingCompleted = runBlocking { DataStoreUtil.isOnboardingCompleted(context).first() }
            if (isOnboardingCompleted) {
                Log.d("AppNavHost", "Initial startDestination: Firebase user found, email verified, onboarding completed. Navigating to Dashboard.")
                Screen.Dashboard.route
            } else {
                Log.d("AppNavHost", "Initial startDestination: Firebase user found, email verified, but onboarding not completed. Navigating to Onboarding.")
                Screen.Onboarding.route
            }
        } else {
            Log.d("AppNavHost", "Initial startDestination: No Firebase user or email not verified. Navigating to Login.")
            Screen.Login.route
        }
        initialRoute
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    coroutineScope.launch {
                        // Setelah onboarding selesai (baik dengan "Get Started" atau "Skip"
                        // yang sudah menyimpan data default dan menandai onboarding completed),
                        // navigasi ke Dashboard.
                        // FirebaseAuth.getInstance().currentUser seharusnya non-null di sini.
                        val destination = if (FirebaseAuth.getInstance().currentUser != null) {
                            Screen.Dashboard.route
                        } else {
                            // Ini seharusnya jarang terjadi jika alur login/register benar
                            Log.w("AppNavHost", "Onboarding finished but Firebase user is null, redirecting to Login.")
                            Screen.Login.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true } // Hapus Onboarding dari back stack
                            launchSingleTop = true
                        }
                        Log.i("AppNavHost", "Navigating to $destination after Onboarding finished.")
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                loginViewModel = loginViewModel,
                navigateToRegister = {
                    navController.navigate(Screen.Register.route)
                    Log.d("AppNavHost", "Navigating to Register.")
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                registerViewModel = hiltViewModel(),
                onRegisterSuccess = {
                    // Setelah register sukses, navigasi ke Onboarding
                    // (Karena register otomatis login, dan onboarding adalah langkah berikutnya)
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true } // Hapus Register dari back stack
                        popUpTo(Screen.Login.route) { inclusive = true } // Hapus Login juga jika ada
                        launchSingleTop = true
                    }
                    Log.i("AppNavHost", "Navigating to Onboarding after successful registration.")
                },
                navigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                    Log.d("AppNavHost", "Navigating to Login from Register.")
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel = hiltViewModel(), navController = navController)
            Log.d("AppNavHost", "Displaying DashboardScreen.")
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
            DaftarRuanganScreen(
                navController = navController,
            )
            Log.d("AppNavHost", "Displaying DaftarRuanganScreen.")
        }
        composable(Screen.SimulasiPage.route) {
            SimulasiScreen(
                navController = navController,
                onSimulasiBebasClick = {
                    navController.navigate(Screen.SimulasiBebas.route)
                    Log.d("AppNavHost", "Navigating to SimulasiBebas.")
                }
            )
            Log.d("AppNavHost", "Displaying SimulasiScreen.")
        }
        composable(Screen.Setting.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            SettingScreen(
                navController = navController,
                userViewModel = userViewModel,
                onLogOutClick = {
                    userViewModel.logout()
                    loginViewModel.resetLoginState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    Log.i("AppNavHost", "User logged out, navigating to Login.")
                }
            )
            Log.d("AppNavHost", "Displaying SettingScreen.")
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
            Log.d("AppNavHost", "Displaying RekomendasiScreen for ruanganId: $id.")
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
                onDeviceSelect = { device -> Log.d("AppNavHost", "Selected device: ${device.nama}") },
                navController = navController,
                simulationId = backStackEntry.arguments?.getInt("simulationId")?.takeIf { it != -1 },
                viewModel = hiltViewModel()
            )
            Log.d("AppNavHost", "Displaying SimulasiBebasScreen.")
        }
        composable(Screen.SimulationComparison.route) {
            SimulationComparisonScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
            Log.d("AppNavHost", "Displaying SimulationComparisonScreen.")
        }
        composable(
            route = Screen.ImagePicker.route,
            arguments = listOf(navArgument("ruanganId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ruanganId = backStackEntry.arguments?.getInt("ruanganId") ?: 0
            SearchScreen(navController = navController, ruanganId = ruanganId)
            Log.d("AppNavHost", "Displaying SearchScreen for ruanganId: $ruanganId.")
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
            Log.d("AppNavHost", "Displaying InputPerangkatScreen for ruanganId: $ruanganId.")
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
            Log.d("AppNavHost", "Displaying DetailRuangan for ruanganId: $ruanganId.")
        }
    }
}
