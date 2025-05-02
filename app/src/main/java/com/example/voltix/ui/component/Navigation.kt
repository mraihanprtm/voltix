package com.example.voltix.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voltix.data.remote.response.AuthResponse
import com.example.voltix.ui.MainScreen
import com.example.voltix.ui.pages.auth.LoginScreen
import com.example.voltix.ui.pages.auth.RegisterScreen
import com.example.voltix.ui.pages.home.HomeScreen
import com.example.voltix.viewmodel.auth.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "main" else "login"

    val loginState by loginViewModel.loginState.collectAsState()

    // Observe login state changes
    LaunchedEffect(loginState) {
        if (loginState is AuthResponse.Success) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                loginViewModel = loginViewModel,
                navigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen()
        }

        composable("home") {
            HomeScreen(
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}
