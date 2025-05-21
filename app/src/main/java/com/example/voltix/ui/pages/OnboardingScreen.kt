package com.example.voltix.ui.pages

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
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.GolonganListrikEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.util.DataStoreUtil
import com.example.voltix.viewmodel.UserViewModel
import com.example.voltix.viewmodel.simulasi.GolonganListrikViewModel
import com.example.voltix.viewmodel.simulasi.PerangkatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: PerangkatViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    golonganListrikViewModel: GolonganListrikViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPage(
            title = "Selamat Datang di Voltix",
            description = "Aplikasi untuk mengelola kebutuhan listrik Anda dengan cerdas dan efisien.",
            lottieRes = R.raw.welcome
        ),
        OnboardingPage(
            title = "Fitur Unggulan",
            description = "Pindai perangkat elektronik, dapatkan rekomendasi lampu, dan simulasikan konsumsi listrik.",
            lottieRes = R.raw.features
        ),
        OnboardingPage(
            title = "Pilih Jenis Listrik",
            description = "Pilih kapasitas listrik rumah Anda untuk pengalaman yang lebih personal.",
            lottieRes = R.raw.electricity
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var user by remember { mutableStateOf<UserEntity?>(null) }
    var jenisListrikList by remember { mutableStateOf<List<GolonganListrikEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        user = userViewModel.getCurrentUser()
        jenisListrikList = golonganListrikViewModel.getAllGolonganListrik()
        println("jenisListrikList = " + jenisListrikList)
    }

    var selectedJenisListrik by remember {
        mutableStateOf<GolonganListrikEntity?>(null)
    }

// update selected after data is loaded
    LaunchedEffect(jenisListrikList) {
        if (jenisListrikList.isNotEmpty()) {
            selectedJenisListrik = jenisListrikList[0]
        }
    }

    val jenisPembayaran = listOf("Prabayar", "Pascabayar")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(jenisPembayaran[0]) }



    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current
    val screenWidth = with(density) { context.resources.displayMetrics.widthPixels.toDp() }

    if (isLoading) {
        LoadingAnimationSection(isLoading)
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Lottie Animation dengan ukuran responsif
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(pages[page].lottieRes)
                        )
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier
                                .size(width = screenWidth * 0.8f, height = screenWidth * 0.8f) // Ukuran 80% lebar layar
                                .padding(bottom = 24.dp),
                        )

                        // Title
                        Text(
                            text = pages[page].title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Description
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pages[page].description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        // Jenis Listrik Dropdown (hanya di halaman terakhir)
                        if (page == pages.size - 1) {
                            jenisPembayaran.forEach{jenis ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (jenis == selectedOption),
                                            onClick = { onOptionSelected(jenis) },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (jenis == selectedOption),
                                        onClick = null // null recommended for accessibility with screen readers
                                    )
                                    Text(
                                        text = jenis,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = "${selectedJenisListrik!!.golonganTarif} ${selectedJenisListrik!!.batasDaya} VA",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Jenis Listrik") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    jenisListrikList.forEach { option ->
                                        if (option.isRTM) {
                                            DropdownMenuItem(
                                                text = {Text(option.golonganTarif + " " + option.batasDaya + "VA-RTM")},
                                                onClick = {
                                                    selectedJenisListrik = option
                                                    expanded = false
                                                }
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                text = { Text(option.golonganTarif + " " + option.batasDaya + "VA") },
                                                onClick = {
                                                    selectedJenisListrik = option
                                                    expanded = false
                                                }
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }
                }

                // Page Indicator
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { index ->
                        val color = if (pagerState.currentPage == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Navigation Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                DataStoreUtil.setOnboardingCompleted(context, true)
                                onFinish()
                            }
                        }
                    ) {
                        Text(
                            text = "Skip",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage == pages.size - 1) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                                        val existingUser = userViewModel.getUserByUid(userId)

                                        val updatedUser = existingUser?.copy(
                                        jenisListrik = selectedJenisListrik!!.idGolonganListrik,
                                        isPrabayar = selectedOption == "Prabayar"
                                    ) ?: UserEntity(
                                        name = firebaseUser?.displayName ?: "Guest User",
                                        email = firebaseUser?.email ?: "guest@example.com",
                                        jenisListrik = selectedJenisListrik!!.idGolonganListrik,
                                        isPrabayar = selectedOption == "Prabayar",
                                        uid = userId
                                    )

                                        if (existingUser != null) {
                                            userViewModel.updateUser(updatedUser)
                                        } else {
                                            userViewModel.insertUser(updatedUser)
                                        }

                                        viewModel.updateJenisListrik(selectedJenisListrik!!.idGolonganListrik)
                                        var currentuserId = userViewModel.getCurrentUser()!!.id
                                        var biayaListrikuser = userViewModel.getUserBiayaListrik(currentuserId)
                                        println("Biaya Listrik User= $biayaListrikuser")
                                        var tarifUser = userViewModel.getUserTarif(currentuserId, 35.0)
                                        println("TARIF USER = $tarifUser")
                                        DataStoreUtil.setOnboardingCompleted(context, true)
                                        onFinish()
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Gagal menyimpan data: ${e.message}")
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}