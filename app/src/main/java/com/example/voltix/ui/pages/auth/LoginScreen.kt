package com.example.voltix.ui.pages.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.voltix.viewmodel.auth.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Composable untuk layar Login.
 * Menangani input pengguna, interaksi dengan ViewModel, dan navigasi.
 */
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {} // Callback untuk navigasi setelah login sukses
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resetEmail by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val loginState by loginViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Google Sign-In Setup ---
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // web_client_id harus dari Google Cloud Console, tipe "Web application"
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In Activity Result: resultCode=${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account?.idToken != null) {
                    Log.d("LoginScreen", "Google Sign-In: idToken didapatkan, memproses dengan ViewModel.")
                    loginViewModel.processGoogleSignInToken(account.idToken!!)
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Gagal mendapatkan token Google.") }
                    Log.e("LoginScreen", "Google Sign-In: idToken is null after successful result.")
                }
            } catch (e: ApiException) {
                scope.launch { snackbarHostState.showSnackbar("Login Google gagal: ${e.localizedMessage}") }
                Log.e("LoginScreen", "Google Sign-In failed with ApiException: statusCode=${e.statusCode}, message=${e.message}", e)
            }
        } else {
            // resultCode=0 berarti pengguna membatalkan atau ada masalah awal
            Log.w("LoginScreen", "Google Sign-In dibatalkan atau gagal: resultCode=${result.resultCode}")
            if (result.resultCode != Activity.RESULT_CANCELED) { // Tampilkan pesan hanya jika bukan pembatalan eksplisit
                scope.launch { snackbarHostState.showSnackbar("Login Google dibatalkan atau gagal.") }
            }
        }
    }
    // --- Akhir Google Sign-In Setup ---

    // Handle perubahan state login dari ViewModel (untuk navigasi dan error utama)
    LaunchedEffect(loginState) {
        Log.d("LoginScreen", "LaunchedEffect(loginState): Current state is $loginState")
        when (val currentState = loginState) {
            is LoginViewModel.LoginState.Success -> {
                Log.i("LoginScreen", "LoginState.Success: Memanggil onLoginSuccess().")
                onLoginSuccess() // Panggil callback navigasi HANYA untuk login berhasil
            }
            is LoginViewModel.LoginState.Error -> {
                Log.e("LoginScreen", "LoginState.Error: Menampilkan snackbar: ${currentState.message}")
                if (currentState.message != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(currentState.message)
                        loginViewModel.resetLoginState() // Reset state setelah error ditampilkan
                    }
                }
            }
            // Kasus Loading dan Idle tidak perlu navigasi langsung dari sini
            else -> {}
        }
    }

    // Handle UI events/messages dari ViewModel (untuk Snackbar, seperti pesan reset password)
    LaunchedEffect(Unit) {
        loginViewModel.uiEvents.collectLatest { message ->
            Log.d("LoginScreen", "uiEvents: Menampilkan snackbar: $message")
            snackbarHostState.showSnackbar(message)
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
                    if (email.isNotBlank() && password.isNotBlank()) {
                        Log.d("LoginScreen", "Attempting email/password login.")
                        loginViewModel.loginWithEmail(email, password)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Email dan password tidak boleh kosong.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = loginState !is LoginViewModel.LoginState.Loading && email.isNotEmpty() && password.isNotEmpty()
            ) {
                if (loginState is LoginViewModel.LoginState.Loading && (email.isNotEmpty() || password.isNotEmpty())) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign-in", fontWeight = FontWeight.Bold)
                }
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
                onClick = {
                    try {
                        val signInIntent = googleSignInClient.signInIntent
                        Log.d("LoginScreen", "Google SignInIntent berhasil dibuat. Meluncurkan activity Google Sign-In...")
                        Log.d("LoginScreen", "Requesting ID token for server client ID: ${context.getString(R.string.web_client_id)}")
                        googleSignInLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Gagal membuat atau meluncurkan Google SignInIntent.", e)
                        scope.launch {
                            snackbarHostState.showSnackbar("Tidak dapat memulai proses login Google.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = loginState !is LoginViewModel.LoginState.Loading
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
                        if(resetEmail.isNotBlank()){
                            Log.d("LoginScreen", "Sending password reset email to $resetEmail.")
                            loginViewModel.sendPasswordResetEmail(resetEmail)
                        }
                        showResetDialog = false // Tutup dialog setelah memanggil fungsi ViewModel
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
