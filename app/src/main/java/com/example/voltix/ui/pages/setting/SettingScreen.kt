package com.example.voltix.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    onLogOutClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedJenisListrik by remember { mutableStateOf<GolonganListrikDenganBiaya?>(null) }
    var selectedOption by remember { mutableStateOf("Prabayar") }
    var jenisListrikList by remember { mutableStateOf<List<GolonganListrikDenganBiaya>>(emptyList()) }
    var currentUser by remember { mutableStateOf<UserEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Load user and jenisListrikList
    LaunchedEffect(Unit) {
        currentUser = userViewModel.getCurrentUser()
        if (currentUser == null) {
            snackbarHostState.showSnackbar("Silakan login terlebih dahulu")
            navController.navigate("login") { // Replace with Screen.Login.route
                popUpTo(0) { inclusive = true }
            }
        } else {
            jenisListrikList = userViewModel.getAllGolonganListrik()
                .distinctBy { "${it.golonganTarif}-${it.batasDaya}-${it.isRTM}" }
            if (jenisListrikList.isEmpty()) {
                snackbarHostState.showSnackbar("Gagal memuat daftar Jenis Listrik")
            }
            currentUser?.let { user ->
                selectedJenisListrik = jenisListrikList.find { it.idGolonganListrik == user.jenisListrik }
                selectedOption = if (userViewModel.getUserisPrabayar(user.id ?: 0)) "Prabayar" else "Pascabayar"
            }
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            LoadingAnimationSection(isLoading)
        } else if (currentUser == null) {
            // Handled in LaunchedEffect
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pengaturan",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E),
                                fontSize = 28.sp
                            ),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        // Jenis Listrik Selection Card
                        CardSetting(
                            title = "Jenis Listrik",
                            description = selectedJenisListrik?.let {
                                if (it.isRTM) "${it.golonganTarif} ${it.batasDaya} VA-RTM"
                                else "${it.golonganTarif} ${it.batasDaya} VA"
                            } ?: "Pilih Jenis Listrik",
                            onClick = { if (jenisListrikList.isNotEmpty()) showDialog = true }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Tipe Pembayaran Selection Card
                        CardSetting(
                            title = "Tipe Pembayaran",
                            description = selectedOption,
                            onClick = {}
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Prabayar", "Pascabayar").forEach { option ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedOption == option,
                                            onClick = {
                                                selectedOption = option
                                                scope.launch {
                                                    try {
                                                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                                                        val userId = firebaseUser?.uid ?: return@launch
                                                        val existingUser = userViewModel.getUserByUid(userId)
                                                        val updatedUser = existingUser?.copy(
                                                            isPrabayar = option == "Prabayar"
                                                        ) ?: UserEntity(
                                                            name = firebaseUser.displayName ?: "Guest User",
                                                            email = firebaseUser.email ?: "guest@example.com",
                                                            jenisListrik = selectedJenisListrik?.idGolonganListrik ?: 0,
                                                            isPrabayar = option == "Prabayar",
                                                            uid = userId
                                                        )
                                                        if (existingUser != null) {
                                                            userViewModel.updateUser(updatedUser)
                                                        } else {
                                                            userViewModel.insertUser(updatedUser)
                                                        }
                                                        snackbarHostState.showSnackbar("Tipe Pembayaran berhasil diperbarui")
                                                    } catch (e: Exception) {
                                                        snackbarHostState.showSnackbar("Gagal memperbarui Tipe Pembayaran: ${e.message}")
                                                    }
                                                }
                                            }
                                        )
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Log-Out Card
                        CardSetting(
                            title = "Log-Out",
                            description = "Keluar Akun",
                            onClick = onLogOutClick
                        )
                    }
                }
            }
        }
    }

    // Dialog for Jenis Listrik Selection
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pilih Jenis Listrik",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    if (jenisListrikList.isEmpty()) {
                        Text(
                            text = "Tidak ada opsi tersedia",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { showDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tutup")
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            jenisListrikList.forEach { option ->
                                TextButton(
                                    onClick = {
                                        selectedJenisListrik = option
                                        showDialog = false
                                        scope.launch {
                                            try {
                                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                                val userId = firebaseUser?.uid ?: return@launch
                                                val existingUser = userViewModel.getUserByUid(userId)
                                                val updatedUser = existingUser?.copy(
                                                    jenisListrik = option.idGolonganListrik,
                                                    isPrabayar = selectedOption == "Prabayar"
                                                ) ?: UserEntity(
                                                    name = firebaseUser.displayName ?: "Guest User",
                                                    email = firebaseUser.email ?: "guest@example.com",
                                                    jenisListrik = option.idGolonganListrik,
                                                    isPrabayar = selectedOption == "Prabayar",
                                                    uid = userId
                                                )
                                                if (existingUser != null) {
                                                    userViewModel.updateUser(updatedUser)
                                                } else {
                                                    userViewModel.insertUser(updatedUser)
                                                }
                                                snackbarHostState.showSnackbar("Jenis Listrik berhasil diperbarui")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Gagal memperbarui Jenis Listrik: ${e.message}")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (option.isRTM) "${option.golonganTarif} ${option.batasDaya} VA-RTM"
                                        else "${option.golonganTarif} ${option.batasDaya} VA",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF1A237E)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Batal")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardSetting(
    title: String,
    description: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        ),
        border = BorderStroke(2.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A237E)
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF424242)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(24.dp)
                )
            }
            content()
        }
    }
}