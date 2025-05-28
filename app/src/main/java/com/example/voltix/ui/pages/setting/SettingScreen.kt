package com.example.voltix.ui.pages.setting // Pastikan package ini benar

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.GolonganListrikDenganBiaya // Anda menggunakan ini, pastikan ini adalah data yang benar
import com.example.voltix.data.entity.GolonganListrikEntity // Untuk pilihan dropdown
// import com.example.voltix.data.entity.UserEntity // Kita akan pakai UserData dari backend
import com.example.voltix.data.remote.dto.UserData // DTO untuk data user dari backend
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.viewmodel.UserViewModel
import com.example.voltix.viewmodel.ProfileUpdateState // Import sealed class state
import com.example.voltix.viewmodel.simulasi.GolonganListrikViewModel
// FirebaseAuth tidak diakses langsung untuk update, user sudah terautentikasi via token Laravel
// import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(
    navController: NavHostController, // Untuk navigasi jika login diperlukan
    userViewModel: UserViewModel = hiltViewModel(),
    golonganListrikViewModel: GolonganListrikViewModel = hiltViewModel(), // Untuk daftar golongan
    onLogOutClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Untuk Snackbar atau Toast
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Observasi data user termutakhir dari backend
    val currentUserDataFromBackend by userViewModel.currentUserFromBackend.collectAsState()
    // Observasi state update profil
    val profileUpdateState by userViewModel.profileUpdateState.collectAsState()

    var showJenisListrikDialog by remember { mutableStateOf(false) }
    var jenisListrikList by remember { mutableStateOf<List<GolonganListrikEntity>>(emptyList()) }

    // State lokal untuk UI, diinisialisasi dari currentUserDataFromBackend
    var selectedGolonganEntity by remember { mutableStateOf<GolonganListrikEntity?>(null) }
    var selectedPembayaranOption by remember { mutableStateOf("Pascabayar") } // Default

    // Load data awal saat screen pertama kali muncul atau saat currentUserDataFromBackend berubah
    LaunchedEffect(Unit) {
        userViewModel.refreshUserProfileFromBackend() // Ambil data user terbaru dari backend
        // Ambil daftar golongan listrik (ini masih dari lokal, bisa disesuaikan jika dari backend)
        jenisListrikList = golonganListrikViewModel.getAllGolonganListrik()
        Log.d("SettingScreen", "Jenis Listrik List (local): $jenisListrikList")
    }

    // Update UI state lokal ketika data user dari backend berubah atau daftar golongan listrik dimuat
    LaunchedEffect(currentUserDataFromBackend, jenisListrikList) {
        currentUserDataFromBackend?.let { user ->
            Log.d("SettingScreen", "Current User Data from Backend: $user")
            // Cocokkan jenis_listrik (nilai daya) dari backend dengan GolonganListrikEntity
            selectedGolonganEntity = jenisListrikList.find { it.batasDaya == user.jenisListrik }
            selectedPembayaranOption = if (user.isPrabayar == true) "Prabayar" else "Pascabayar"
            Log.d("SettingScreen", "UI states updated: selectedGolongan=${selectedGolonganEntity?.batasDaya}, selectedPembayaran=$selectedPembayaranOption")
        }
    }

    // Handle efek dari perubahan profileUpdateState (hasil panggilan API update)
    LaunchedEffect(profileUpdateState) {
        when (val state = profileUpdateState) {
            is ProfileUpdateState.Success -> {
                Log.i("SettingScreen", "Profile updated successfully on backend: ${state.updatedUserFromBackend}")
                scope.launch {
                    snackbarHostState.showSnackbar("Pengaturan berhasil diperbarui!")
                    // Refresh data user setelah update sukses
                    userViewModel.refreshUserProfileFromBackend()
                    userViewModel.resetProfileUpdateState()
                }
            }
            is ProfileUpdateState.Error -> {
                Log.e("SettingScreen", "Failed to update profile: ${state.message}")
                scope.launch {
                    snackbarHostState.showSnackbar("Gagal memperbarui pengaturan: ${state.message ?: "Error tidak diketahui"}")
                    userViewModel.resetProfileUpdateState()
                }
            }
            is ProfileUpdateState.Loading -> {
                Log.d("SettingScreen", "Profile update is Loading...")
            }
            is ProfileUpdateState.Idle -> {
                // Initial state
            }

            else -> {
                TODO()}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (currentUserDataFromBackend == null && profileUpdateState !is ProfileUpdateState.Loading) {
            // Tampilkan loading atau pesan jika data user belum ada (selain saat proses update)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (profileUpdateState is ProfileUpdateState.Idle) { // Hanya tampilkan loading jika idle dan belum ada data
                    CircularProgressIndicator()
                } else if (profileUpdateState !is ProfileUpdateState.Loading){
                    Text("Gagal memuat data pengguna. Silakan coba lagi.")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    "Pengaturan Akun",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 28.sp),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Jenis Listrik Card
                CardSetting(
                    title = "Kapasitas Listrik",
                    description = selectedGolonganEntity?.let {
                        "${it.golonganTarif} ${it.batasDaya} VA" + if (it.isRTM) " (RTM)" else ""
                    } ?: (if (jenisListrikList.isEmpty()) "Memuat..." else "Pilih Kapasitas"),
                    onClick = {
                        if (jenisListrikList.isNotEmpty()) {
                            showJenisListrikDialog = true
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Daftar jenis listrik belum termuat.")}
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tipe Pembayaran Card
                CardSetting(
                    title = "Jenis Pembayaran",
                    description = selectedPembayaranOption,
                    onClick = { /* Klik pada Card tidak melakukan apa-apa, aksi di RadioButton */ }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 8.dp), // Kurangi padding internal
                        horizontalArrangement = Arrangement.SpaceAround // Sebar merata
                    ) {
                        listOf("Prabayar", "Pascabayar").forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (selectedPembayaranOption != option) {
                                            val newIsPrabayar = option == "Prabayar"
                                            val currentBatasDaya = selectedGolonganEntity?.batasDaya
                                            if (currentBatasDaya != null) {
                                                userViewModel.saveOnboardingChoices(currentBatasDaya, newIsPrabayar)
                                            } else {
                                                scope.launch { snackbarHostState.showSnackbar("Pilih kapasitas listrik terlebih dahulu.") }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedPembayaranOption == option,
                                    onClick = {
                                        if (selectedPembayaranOption != option) {
                                            val newIsPrabayar = option == "Prabayar"
                                            val currentBatasDaya = selectedGolonganEntity?.batasDaya
                                            if (currentBatasDaya != null) {
                                                userViewModel.saveOnboardingChoices(currentBatasDaya, newIsPrabayar)
                                            } else {
                                                scope.launch { snackbarHostState.showSnackbar("Pilih kapasitas listrik terlebih dahulu.") }
                                            }
                                        }
                                    },
                                    enabled = profileUpdateState !is ProfileUpdateState.Loading
                                )
                                Text(text = option, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Log-Out Card
                CardSetting(
                    title = "Log-Out",
                    description = "Keluar dari akun Anda",
                    onClick = {
                        // Panggil fungsi logout dari AuthManager melalui ViewModel jika perlu interaksi backend (misal invalidate token server)
                        // Untuk sekarang, onLogOutClick akan menangani Firebase sign out dan clear local token.
                        onLogOutClick()
                    }
                )

                // Tampilkan loading indicator jika sedang proses update
                if (profileUpdateState is ProfileUpdateState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Dialog untuk memilih Jenis Listrik
    if (showJenisListrikDialog) {
        Dialog(onDismissRequest = { showJenisListrikDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp).clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pilih Kapasitas Listrik", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary), modifier = Modifier.padding(bottom = 16.dp))
                    if (jenisListrikList.isEmpty()) {
                        Text("Memuat pilihan...", modifier = Modifier.padding(bottom = 16.dp))
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                            jenisListrikList.forEach { option ->
                                TextButton(
                                    onClick = {
                                        val currentIsPrabayar = selectedPembayaranOption == "Prabayar"
                                        // Kirim nilai BATAS DAYA ke ViewModel
                                        userViewModel.saveOnboardingChoices(option.batasDaya, currentIsPrabayar)
                                        showJenisListrikDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    enabled = profileUpdateState !is ProfileUpdateState.Loading
                                ) {
                                    Text(
                                        text = "${option.golonganTarif} ${option.batasDaya} VA" + if (option.isRTM) " (RTM)" else "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showJenisListrikDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}

// CardSetting Composable tetap sama seperti yang Anda berikan
@Composable
private fun CardSetting(
    title: String,
    description: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {} // Untuk konten tambahan seperti RadioButton
) {
    // ... implementasi CardSetting Anda ...
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick) // Hanya clickable jika content kosong
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(description, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)), modifier = Modifier.padding(top = 2.dp))
                }
                if (content == {}) { // Tampilkan ikon panah hanya jika tidak ada konten custom
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            if (content != {}) { // Jika ada konten custom, tampilkan di bawah
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}