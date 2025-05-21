package com.example.voltix.ui.pages.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.R
import com.example.voltix.util.DataStoreUtil
import com.example.voltix.viewmodel.auth.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {} // Callback for successful login
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resetEmail by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val loginState by loginViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle login state changes
    LaunchedEffect(loginState) {
        // Adjust this logic based on the actual structure of LoginViewModel.loginState
        // This is a placeholder to check for success or error states
        when (loginState) {
            // Replace with the actual success state type from LoginViewModel
            is LoginViewModel.LoginState.Success -> {
                scope.launch {
                    DataStoreUtil.saveOnboardingCompleted(context, false)
                    onLoginSuccess()
                }
            }
            // Replace with the actual error state type from LoginViewModel
            is LoginViewModel.LoginState.Error -> {
                scope.launch {
                    val errorMessage = (loginState as LoginViewModel.LoginState.Error).message
                    snackbarHostState.showSnackbar(errorMessage.toString())
                }
            }
            else -> {
                // Handle other states like Loading if applicable
            }
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
            Text("Sign-in", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Text("Please fill the form to continue", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter both email and password")
                        }
                    } else {
                        loginViewModel.loginWithEmail(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotEmpty() && password.isNotEmpty() // Disable button if fields are empty
            ) {
                Text("Sign-in", fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("or continue with")
            }

            OutlinedButton(
                onClick = { loginViewModel.loginWithGoogle() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(painter = painterResource(id = R.drawable.google), contentDescription = null, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign-in with Google", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = navigateToRegister,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Don't have an account? Register")
            }

            // Removed the direct error text here since it's now handled via snackbar
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email to receive a password reset link.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("Email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        loginViewModel.sendPasswordResetEmail(resetEmail)
                        showResetDialog = false
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}