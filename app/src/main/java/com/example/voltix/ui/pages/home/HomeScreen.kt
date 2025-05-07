package com.example.voltix.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.viewmodel.home.HomeViewModel

@Composable
fun HomeScreen(
    onSignOut: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName = viewModel.userName.collectAsState().value
    val userUid = viewModel.userUid.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome to Voltix!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        if (userName != null) {
            Text(
                "Hello, $userName!",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                "You are signed in as: ${userUid ?: "Guest"}",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "UID: ${userUid ?: "N/A"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign Out"
            )
            Spacer(Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}