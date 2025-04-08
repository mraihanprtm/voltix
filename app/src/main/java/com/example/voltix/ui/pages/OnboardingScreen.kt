package com.example.voltix.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*

import com.example.voltix.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.viewmodel.PerangkatViewModel
import com.example.voltix.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: PerangkatViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage("WELCOME TO VOLTIX APP", "halo halo niece and nephew", R.drawable.ic_launcher)
    )
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val jenisListrikList = listOf(900, 1300, 2200, 3500)
    var selectedJenisListrik by remember { mutableStateOf(jenisListrikList.first()) }
    var expanded by remember { mutableStateOf(false) }

    val perangkatList by viewModel.perangkatList.observeAsState()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // ✅ Panggil onFinish hanya sekali jika data sudah ada
    LaunchedEffect(perangkatList) {
        if (!perangkatList.isNullOrEmpty()) {
            onFinish()
        }
    }

    // ✅ Tampilkan onboarding hanya jika perangkatList kosong
    if (perangkatList.isNullOrEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(pages[page].imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(pages[page].title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(pages[page].description, style = MaterialTheme.typography.bodyMedium)

                    // Tampilkan dropdown di halaman terakhir
                    if (page == pages.size - 1) {
                        Spacer(Modifier.height(16.dp))

                        Text("Pilih Jenis Listrik:", style = MaterialTheme.typography.bodyMedium)

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = "${selectedJenisListrik} VA",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jenis Listrik") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                jenisListrikList.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text("$option VA") },
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(onClick = {
                        scope.launch { pagerState.scrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Text("Back")
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }

                Button(onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        val user = UserEntity(
                            name = "Guest User",
                            email = "guest@example.com",
                            jenisListrik = selectedJenisListrik,
                            foto_profil = "",
                            uid = userId
                        )
                        userViewModel.insertUser(user)
                        onFinish()
                    } else {
                        scope.launch { pagerState.scrollToPage(pagerState.currentPage + 1) }
                    }
                }) {
                    Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next")
                }
            }
        }
    }
}
