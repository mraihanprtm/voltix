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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.voltix.R
import com.example.voltix.util.DataStoreUtil
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isOnboardingCompleted by DataStoreUtil.isOnboardingCompleted(context).collectAsState(initial = false)
    val authState by produceState(initialValue = FirebaseAuth.getInstance().currentUser) {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            value = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
        awaitDispose { FirebaseAuth.getInstance().removeAuthStateListener(authListener) }
    }

    // Navigate based on auth state and onboarding status
    LaunchedEffect(authState, isOnboardingCompleted) {
        val targetRoute = when {
            authState == null -> Screen.Login.route
            !isOnboardingCompleted -> Screen.Onboarding.route
            else -> Screen.Dashboard.route
        }
        navController.navigate(targetRoute) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val bottomItems = listOf(
        Screen.Dashboard to R.drawable.ic_fa_home,
        Screen.DaftarRuangan to R.drawable.ic_fa_room,
        Screen.SimulasiPage to R.drawable.ic_fa_tag,
        Screen.Rekomendasi to R.drawable.ic_fa_bulb,
        Screen.Setting to R.drawable.ic_fa_setting,
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
                                    (currentRoute?.startsWith("${screen.route}/") == true) ||
                                    (screen == Screen.Rekomendasi && currentRoute?.startsWith("rekomendasi") == true),
                            onClick = {
                                val targetRoute = when (screen) {
                                    Screen.Rekomendasi -> Screen.Rekomendasi.createRoute()
                                    else -> screen.route
                                }
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(painter = painterResource(id = icon), contentDescription = screen.title) },
                            label = { Text(screen.title.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavHost(navController = navController)
        }
    }
}