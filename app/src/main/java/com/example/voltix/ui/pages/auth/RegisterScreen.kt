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
import androidx.compose.ui.platform.LocalContext // LocalContext tidak dipakai, bisa dihapus jika DataStoreUtil tidak dipanggil
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// DataStoreUtil tidak perlu diimport di sini lagi
// import com.example.voltix.util.DataStoreUtil
import com.example.voltix.viewmodel.auth.RegisterViewModel
import kotlinx.coroutines.flow.collectLatest // Import collectLatest jika belum ada
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    navigateToLogin: () -> Unit
) {
    // val context = LocalContext.current // Tidak dipakai lagi di sini
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by registerViewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // LaunchedEffect untuk menangani state registrasi (navigasi dan error utama)
    LaunchedEffect(registerState) {
        when (val currentState = registerState) {
            is RegisterViewModel.RegisterState.Success -> {
                // Navigasi onRegisterSuccess akan dipanggil.
                // Pesan sukses spesifik (termasuk info email verifikasi) akan ditangani oleh uiEvents.
                onRegisterSuccess()
            }
            is RegisterViewModel.RegisterState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(currentState.message ?: "Registrasi gagal.")
                    registerViewModel.resetRegisterState()
                }
            }
            // Kasus Loading dan Idle tidak memerlukan aksi di sini
            else -> {}
        }
    }

    // === TAMBAHKAN LaunchedEffect INI untuk MENGAMATI uiEvents ===
    LaunchedEffect(Unit) { // Key Unit agar hanya dijalankan sekali untuk setup koleksi
        registerViewModel.uiEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    // === AKHIR PENAMBAHAN ===

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

            OutlinedTextField( value = name, onValueChange = {name = it}, placeholder = { Text("Full Name") }, leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null)}, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField( value = email, onValueChange = {email = it}, placeholder = { Text("Email") }, leadingIcon = { Icon(
                Icons.Rounded.Email, contentDescription = null)}, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField( value = password, onValueChange = {password = it}, placeholder = { Text("Password") }, leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null)}, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField( value = confirmPassword, onValueChange = {confirmPassword = it}, placeholder = { Text("Confirm Password") }, leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null)}, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    if (password == confirmPassword) {
                        if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            registerViewModel.registerWithEmail(email, password, name)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Semua field harus diisi.")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Password tidak cocok.") // Pesan lebih jelas
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = registerState !is RegisterViewModel.RegisterState.Loading && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                if (registerState is RegisterViewModel.RegisterState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Register", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = navigateToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? Sign in")
            }

            // Pesan error jika password tidak cocok bisa ditampilkan lebih konsisten
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                Spacer(modifier = Modifier.height(8.dp)) // Beri sedikit jarak
                Text(
                    "Password tidak cocok.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start) // Atau CenterHorizontally
                )
            }
        }
    }
}