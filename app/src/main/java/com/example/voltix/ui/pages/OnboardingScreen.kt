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
        println("jenisListrikList = $jenisListrikList")
    }

    var selectedJenisListrik by remember { mutableStateOf<GolonganListrikEntity?>(null) }

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
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Lottie Animation with controlled size (smaller on page 3)
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(pages[page].lottieRes)
                        )
                        val lottieSize = if (page == 2) screenWidth * 0.3f else screenWidth * 0.6f // Reduced to 30% on page 3
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier
                                .size(width = lottieSize, height = lottieSize)
                                .padding(bottom = 16.dp),
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
                        Text(
                            text = pages[page].description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Jenis Listrik Dropdown and Radio Buttons (only on the last page)
                        if (page == pages.size - 1) {
                            Text(
                                text = "Pilih Jenis Pembayaran",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            jenisPembayaran.forEach { jenis ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(35.dp)
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
                                        onClick = null
                                    )
                                    Text(
                                        text = jenis,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Pilih Jenis Listrik",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedJenisListrik?.let { "${it.golonganTarif} ${it.batasDaya} VA" } ?: "",
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
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (option.isRTM) "${option.golonganTarif} ${option.batasDaya} VA-RTM"
                                                    else "${option.golonganTarif} ${option.batasDaya} VA"
                                                )
                                            },
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
                                DataStoreUtil.saveOnboardingCompleted(context, true)
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
                                        val currentUserId = userViewModel.getCurrentUser()?.id
                                        if (currentUserId != null) {
                                            val biayaListrikUser = userViewModel.getUserBiayaListrik(currentUserId)
                                            println("Biaya Listrik User= $biayaListrikUser")
                                            val tarifUser = userViewModel.getUserTarif(currentUserId, 35.0)
                                            println("TARIF USER = $tarifUser")
                                        }
                                        DataStoreUtil.saveOnboardingCompleted(context, true)
                                        onFinish()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Gagal menyimpan data: ${e.message}")
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