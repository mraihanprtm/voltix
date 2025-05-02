package com.example.voltix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.voltix.R

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // pembuatan list yang berpasangan
    val bottomItems = listOf(
        Screen.DaftarRuangan to R.drawable.ic_fa_list,
        Screen.Simulation to R.drawable.ic_fa_tag,
        Screen.ImagePicker to R.drawable.ic_fa_camera
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            // jika onboarding maka navbar tidak ditampilkan
            if (currentRoute != Screen.Onboarding.route) {
                NavigationBar {
                    // Item dalam navigation bar
                    bottomItems.forEach { (screen, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {

                                    // mencegah penumpukan banyak instance layar
                                    popUpTo(navController.graph.startDestinationId)

                                    // mencegah navigasi berulang ke layar yang sama jika klik ikon yang sedang aktif
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
        // mengatur konten screen dengan berdasarkan AppNavHost
        Box(modifier = Modifier.padding(innerPadding)) { //Modifier innerpadding agar screen aktif tidak ditimpa navbar
            AppNavHost(navController)
        }
    }
}
