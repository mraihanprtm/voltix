package com.example.voltix.ui.theme

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
import com.example.voltix.ui.simulasi.InputPerangkatScreen
import com.example.voltix.ui.simulasi.SimulasiPage
import com.example.voltix.viewmodel.PerangkatViewModel

sealed class Screen(val route: String, val title: String) {
    object Simulation : Screen("simulation", "Simulation")
    object InputPerangkat : Screen("input_perangkat", "Input Perangkat")
}

@Composable
fun AppNavHost(
    simulasiViewModel: SimulasiViewModel,
    perangkatViewModel: PerangkatViewModel = viewModel()

) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        // HAPUS Column & verticalScroll() untuk menghindari infinite height constraint
        NavHost(
            navController = navController,
            startDestination = Screen.InputPerangkat.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Gunakan padding dari Scaffold
        ) {
            composable(Screen.InputPerangkat.route) {
                InputPerangkatScreen(navController = navController, viewModel = perangkatViewModel)
            }
            composable(Screen.Simulation.route) {
                SimulasiPage(navController = navController, viewModel = simulasiViewModel, perangkatViewModel = perangkatViewModel)
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = Screen.InputPerangkat.title) },
            label = { Text(Screen.InputPerangkat.title) },
            selected = currentRoute?.destination?.route == Screen.InputPerangkat.route,
            onClick = {
                if (currentRoute?.destination?.route != Screen.InputPerangkat.route) {
                    navController.navigate(Screen.InputPerangkat.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = Screen.Simulation.title) },
            label = { Text(Screen.Simulation.title) },
            selected = currentRoute?.destination?.route == Screen.Simulation.route,
            onClick = {
                if (currentRoute?.destination?.route != Screen.Simulation.route) {
                    navController.navigate(Screen.Simulation.route)
                }
            }
        )
    }
}
