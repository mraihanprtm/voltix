package com.example.voltix.ui.pages.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.data.remote.response.AuthResponse
import com.example.voltix.util.DataStoreUtil
import com.example.voltix.viewmodel.auth.RegisterViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val registerState by registerViewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        if (registerState is AuthResponse.Success) {
            scope.launch {
                DataStoreUtil.saveOnboardingCompleted(context, false) // Fixed function name
                onRegisterSuccess()
            }
        } else if (registerState is AuthResponse.Error) {
            snackbarHostState.showSnackbar((registerState as AuthResponse.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Text("Please fill the form to register", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password == confirmPassword) {
                        registerViewModel.registerWithEmail(email, password, name)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                Text("Register", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = navigateToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? Sign in")
            }

            if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}