package com.example.voltix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.voltix.R
import com.example.voltix.util.DataStoreUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isOnboardingCompleted by DataStoreUtil.isOnboardingCompleted(context).collectAsState(initial = false)
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    // Navigate based on onboarding status and Firebase auth
    LaunchedEffect(isOnboardingCompleted, firebaseUser) {
        if (firebaseUser == null) {
            // Pengguna belum login, arahkan ke LoginScreen
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isOnboardingCompleted) {
            // Onboarding belum selesai, arahkan ke OnboardingScreen
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // Onboarding selesai, arahkan ke Dashboard
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val bottomItems = listOf(
        Screen.Dashboard to R.drawable.ic_fa_home,
        Screen.DaftarRuangan to R.drawable.ic_fa_room,
        Screen.SimulasiPage to R.drawable.ic_fa_tag,
        Screen.Rekomendasi to R.drawable.ic_fa_bulb,
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != Screen.Login.route && currentRoute != Screen.Register.route && currentRoute != Screen.Onboarding.route) {
                NavigationBar {
                    bottomItems.forEach { (screen, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route ||
                                    (currentRoute?.startsWith("${screen.route}/") == true),
                            onClick = {
                                val targetRoute = if (screen == Screen.Rekomendasi) {
                                    "rekomendasi"
                                } else {
                                    screen.route
                                }
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(painter = painterResource(id = icon), contentDescription = screen.route) },
                            label = { Text(screen.title.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavHost(navController)
        }
    }
}