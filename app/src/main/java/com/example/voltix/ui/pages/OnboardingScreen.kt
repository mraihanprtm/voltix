package com.example.voltix.ui.pages // Pastikan package Anda benar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltix.R
import com.example.voltix.data.entity.GolonganListrikEntity
// UserEntity tidak lagi di-manage langsung di sini untuk update ke backend
// import com.example.voltix.data.entity.UserEntity
import com.example.voltix.util.DataStoreUtil
import com.example.voltix.viewmodel.UserViewModel
import com.example.voltix.viewmodel.ProfileUpdateState // Pastikan ProfileUpdateState di-import dari UserViewModel
import com.example.voltix.viewmodel.simulasi.GolonganListrikViewModel
// PerangkatViewModel sepertinya tidak lagi dibutuhkan di sini setelah perbaikan
// import com.example.voltix.viewmodel.simulasi.PerangkatViewModel
// FirebaseAuth tidak diakses langsung di sini untuk update, user sudah login dan punya token Laravel
// import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// data class OnboardingPage tetap sama
data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    // Hapus viewModel: PerangkatViewModel jika tidak lagi digunakan
    userViewModel: UserViewModel = hiltViewModel(),
    golonganListrikViewModel: GolonganListrikViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Amati state update profil dari UserViewModel
    val profileUpdateState by userViewModel.profileUpdateState.collectAsState()

    val pages = listOf(
        OnboardingPage("Selamat Datang di Voltix", "Aplikasi untuk mengelola kebutuhan listrik Anda dengan cerdas dan efisien.", R.raw.welcome),
        OnboardingPage("Fitur Unggulan", "Pindai perangkat elektronik, dapatkan rekomendasi lampu, dan simulasikan konsumsi listrik.", R.raw.features),
        OnboardingPage("Pilih Jenis Listrik", "Pilih kapasitas listrik rumah Anda untuk pengalaman yang lebih personal.", R.raw.electricity)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    var jenisListrikList by remember { mutableStateOf<List<GolonganListrikEntity>>(emptyList()) }
    var selectedJenisListrik by remember { mutableStateOf<GolonganListrikEntity?>(null) }

    // Mengambil daftar golongan listrik dari ViewModel (yang mengambil dari Room)
    LaunchedEffect(Unit) {
        jenisListrikList = golonganListrikViewModel.getAllGolonganListrik()
        Log.d("OnboardingScreen", "Jenis Listrik List fetched: $jenisListrikList")
    }

    // Set pilihan default untuk jenis listrik
    LaunchedEffect(jenisListrikList) {
        if (jenisListrikList.isNotEmpty() && selectedJenisListrik == null) {
            // Coba default ke 2200 VA, jika tidak ada, ambil item pertama
            selectedJenisListrik = jenisListrikList.find { it.batasDaya == 2200 } ?: jenisListrikList.firstOrNull()
            Log.d("OnboardingScreen", "Default selectedJenisListrik: $selectedJenisListrik")
        }
    }

    val jenisPembayaran = listOf("Prabayar", "Pascabayar")
    val (selectedPembayaranOption, onPembayaranOptionSelected) = remember { mutableStateOf(jenisPembayaran[0]) }
    var isJenisListrikDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current
    val screenWidth = with(density) { context.resources.displayMetrics.widthPixels.toDp() }

    // Handle efek dari perubahan profileUpdateState (hasil panggilan API)
    LaunchedEffect(profileUpdateState) {
        when (val state = profileUpdateState) {
            is ProfileUpdateState.Success -> {
                Log.i("OnboardingScreen", "Pilihan onboarding berhasil disimpan ke backend. User: ${state.updatedUserFromBackend}")
                scope.launch {
                    DataStoreUtil.saveOnboardingCompleted(context, true) // Tandai onboarding selesai
                    userViewModel.resetProfileUpdateState() // Reset state di ViewModel agar tidak trigger lagi
                    onFinish() // Panggil callback untuk navigasi
                }
            }
            is ProfileUpdateState.Error -> {
                Log.e("OnboardingScreen", "Gagal menyimpan pilihan onboarding ke backend: ${state.message}")
                scope.launch {
                    snackbarHostState.showSnackbar("Gagal menyimpan pilihan: ${state.message ?: "Terjadi kesalahan"}")
                    userViewModel.resetProfileUpdateState()
                }
            }
            is ProfileUpdateState.Loading -> {
                Log.d("OnboardingScreen", "Proses penyimpanan onboarding sedang berjalan...")
                // Indikator loading utama bisa ditampilkan di sini atau di tombol
            }
            is ProfileUpdateState.Idle -> {
                // Tidak ada aksi
            }
             else -> {
                // Ini seharusnya tidak pernah terpanggil jika ProfileUpdateState sealed
                // dan semua kasus sudah ditangani. Anda bisa tambahkan log di sini jika ingin tahu.
                Log.w("OnboardingScreen", "Kasus ProfileUpdateState yang tidak terduga: $state")
             }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.background))
        )
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(pages[page].lottieRes))
                    val lottieSize = if (page == 2) screenWidth * 0.4f else screenWidth * 0.6f // Sedikit diperbesar untuk halaman terakhir
                    LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(width = lottieSize, height = lottieSize).padding(bottom = 16.dp))
                    Text(pages[page].title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
                    Text(pages[page].description, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp))

                    if (page == pages.size - 1) { // Form pilihan hanya di halaman terakhir
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pilih Jenis Pembayaran Listrik Anda", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
                        Row(Modifier.padding(vertical = 8.dp)) { // Radio button jenis pembayaran
                            jenisPembayaran.forEach { jenis ->
                                Row(
                                    Modifier.selectable(selected = (jenis == selectedPembayaranOption), onClick = { onPembayaranOptionSelected(jenis) }, role = Role.RadioButton).padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = (jenis == selectedPembayaranOption), onClick = null)
                                    Text(text = jenis, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }

                        Text("Pilih Kapasitas Listrik Terpasang", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
                        ExposedDropdownMenuBox(
                            expanded = isJenisListrikDropdownExpanded,
                            onExpandedChange = { isJenisListrikDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedJenisListrik?.let { "${it.golonganTarif} ${it.batasDaya} VA" + if(it.isRTM) " (RTM)" else "" } ?: "Pilih Kapasitas Listrik",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kapasitas Listrik") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isJenisListrikDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                            )
                            ExposedDropdownMenu(expanded = isJenisListrikDropdownExpanded, onDismissRequest = { isJenisListrikDropdownExpanded = false }) {
                                jenisListrikList.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(if (option.isRTM) "${option.golonganTarif} ${option.batasDaya} VA (RTM)" else "${option.golonganTarif} ${option.batasDaya} VA") },
                                        onClick = { selectedJenisListrik = option; isJenisListrikDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) { // Indikator halaman
                repeat(pages.size) { index ->
                    val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(color))
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Absolute.Right, verticalAlignment = Alignment.CenterVertically) { // Tombol Navigasi
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) { // Jika di halaman terakhir
                            selectedJenisListrik?.let { golonganPilihan ->
                                val nilaiDayaUntukDikirim = golonganPilihan.batasDaya // Ini adalah nilai daya (misal 2200)
                                val statusPrabayar = selectedPembayaranOption == "Prabayar"
                                // Panggil fungsi di UserViewModel untuk update ke backend
                                userViewModel.saveOnboardingChoices(nilaiDayaUntukDikirim, statusPrabayar)
                            } ?: scope.launch {
                                snackbarHostState.showSnackbar("Silakan pilih jenis listrik terlebih dahulu.")
                            }
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    enabled = profileUpdateState !is ProfileUpdateState.Loading, // Disable tombol saat proses update berjalan
                    modifier = Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    // Tampilkan loading di tombol jika ini page terakhir dan sedang loading
                    if (pagerState.currentPage == pages.size - 1 && profileUpdateState is ProfileUpdateState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
    // Hapus var isLoading lokal jika sudah dikelola oleh profileUpdateState
    // if (isLoading) { LoadingAnimationSection(isLoading) } -> ini bisa diganti dengan observasi profileUpdateState
}