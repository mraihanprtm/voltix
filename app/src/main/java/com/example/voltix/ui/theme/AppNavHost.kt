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
import com.example.voltix.ui.simulasi.SimulasiPage
import com.example.voltix.viewmodel.ImageRecognitionViewModel

sealed class Screen(val route: String, val title: String) {
    object Simulation : Screen("simulation", "Simulation")
    object ImagePicker : Screen("image_picker", "Image Picker")
}

@Composable
fun AppNavHost(
    simulasiViewModel: SimulasiViewModel,
    imageRecognitionViewModel: ImageRecognitionViewModel = viewModel()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        // HAPUS Column & verticalScroll() untuk menghindari infinite height constraint
        NavHost(
            navController = navController,
            startDestination = Screen.Simulation.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Gunakan padding dari Scaffold
        ) {
            composable(Screen.Simulation.route) {
                SimulasiPage(navController = navController, viewModel = simulasiViewModel)
            }
            composable(Screen.ImagePicker.route) {
                ImagePickerScreen(navController = navController, viewModel = imageRecognitionViewModel)
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    NavigationBar {
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
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = Screen.ImagePicker.title) },
            label = { Text(Screen.ImagePicker.title) },
            selected = currentRoute?.destination?.route == Screen.ImagePicker.route,
            onClick = {
                if (currentRoute?.destination?.route != Screen.ImagePicker.route) {
                    navController.navigate(Screen.ImagePicker.route)
                }
            }
        )
    }
}
