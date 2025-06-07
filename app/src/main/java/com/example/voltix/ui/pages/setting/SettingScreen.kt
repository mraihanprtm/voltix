package com.example.voltix.ui.pages.setting

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.GolonganListrikEntity
import com.example.voltix.data.remote.dto.UserData
import com.example.voltix.viewmodel.UserViewModel
import com.example.voltix.viewmodel.ProfileUpdateState
import com.example.voltix.viewmodel.simulasi.GolonganListrikViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
    golonganListrikViewModel: GolonganListrikViewModel = hiltViewModel(),
    onLogOutClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // State observers
    val currentUserDataFromBackend by userViewModel.currentUserFromBackend.collectAsState()
    val profileUpdateState by userViewModel.profileUpdateState.collectAsState()

    // Dialog states
    var showJenisListrikDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var jenisListrikList by remember { mutableStateOf<List<GolonganListrikEntity>>(emptyList()) }

    // UI states
    var selectedGolonganEntity by remember { mutableStateOf<GolonganListrikEntity?>(null) }
    var selectedPembayaranOption by remember { mutableStateOf("Pascabayar") }

    // Animation states
    val animatedFloat by animateFloatAsState(
        targetValue = if (profileUpdateState is ProfileUpdateState.Loading) 1f else 0f,
        animationSpec = tween(300)
    )

    // Data loading effects
    LaunchedEffect(Unit) {
        userViewModel.refreshUserProfileFromBackend()
        jenisListrikList = golonganListrikViewModel.getAllGolonganListrik()
        Log.d("SettingScreen", "Jenis Listrik List (local): $jenisListrikList")
    }

    LaunchedEffect(currentUserDataFromBackend, jenisListrikList) {
        currentUserDataFromBackend?.let { user ->
            Log.d("SettingScreen", "Current User Data from Backend: $user")
            selectedGolonganEntity = jenisListrikList.find { it.batasDaya == user.jenisListrik }
            selectedPembayaranOption = if (user.isPrabayar == true) "Prabayar" else "Pascabayar"
            Log.d("SettingScreen", "UI states updated: selectedGolongan=${selectedGolonganEntity?.batasDaya}, selectedPembayaran=$selectedPembayaranOption")
        }
    }

    // Handle profile update state
    LaunchedEffect(profileUpdateState) {
        when (val state = profileUpdateState) {
            is ProfileUpdateState.Success -> {
                Log.i("SettingScreen", "Profile updated successfully on backend: ${state.updatedUserFromBackend}")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "✅ Pengaturan berhasil diperbarui!",
                        duration = SnackbarDuration.Short
                    )
                    userViewModel.refreshUserProfileFromBackend()
                    userViewModel.resetProfileUpdateState()
                }
            }
            is ProfileUpdateState.Error -> {
                Log.e("SettingScreen", "Failed to update profile: ${state.message}")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "❌ Gagal memperbarui: ${state.message ?: "Error tidak diketahui"}",
                        duration = SnackbarDuration.Long
                    )
                    userViewModel.resetProfileUpdateState()
                }
            }
            is ProfileUpdateState.Loading -> {
                Log.d("SettingScreen", "Profile update is Loading...")
            }
            is ProfileUpdateState.Idle -> {
                // Initial state
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            ),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            if (currentUserDataFromBackend == null && profileUpdateState !is ProfileUpdateState.Loading) {
                // Loading or error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileUpdateState is ProfileUpdateState.Idle) {
                        ModernLoadingIndicator()
                    } else if (profileUpdateState !is ProfileUpdateState.Loading) {
                        ErrorStateComponent {
                            userViewModel.refreshUserProfileFromBackend()
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Section
                    HeaderSection(
                        userData = currentUserDataFromBackend,
                        isLoading = profileUpdateState is ProfileUpdateState.Loading
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Settings Cards
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(500, delayMillis = 200)
                        ) + fadeIn(tween(500, delayMillis = 200))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Electricity Capacity Card
                            ModernSettingCard(
                                icon = Icons.Filled.Build,
                                title = "Kapasitas Listrik",
                                description = selectedGolonganEntity?.let {
                                    "${it.golonganTarif} ${it.batasDaya} VA" + if (it.isRTM) " (RTM)" else ""
                                } ?: (if (jenisListrikList.isEmpty()) "Memuat..." else "Pilih Kapasitas"),
                                onClick = {
                                    if (jenisListrikList.isNotEmpty()) {
                                        showJenisListrikDialog = true
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("⚡ Daftar jenis listrik belum termuat.")
                                        }
                                    }
                                },
                                isLoading = profileUpdateState is ProfileUpdateState.Loading
                            )

                            // Payment Type Card
                            ModernSettingCard(
                                icon = Icons.Filled.ShoppingCart,
                                title = "Jenis Pembayaran",
                                description = selectedPembayaranOption,
                                onClick = { /* Handled by radio buttons */ },
                                isLoading = profileUpdateState is ProfileUpdateState.Loading
                            ) {
                                PaymentTypeSelector(
                                    selectedOption = selectedPembayaranOption,
                                    onOptionSelected = { option ->
                                        if (selectedPembayaranOption != option) {
                                            val newIsPrabayar = option == "Prabayar"
                                            val currentBatasDaya = selectedGolonganEntity?.batasDaya
                                            if (currentBatasDaya != null) {
                                                userViewModel.saveOnboardingChoices(currentBatasDaya, newIsPrabayar)
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("💡 Pilih kapasitas listrik terlebih dahulu.")
                                                }
                                            }
                                        }
                                    },
                                    enabled = profileUpdateState !is ProfileUpdateState.Loading
                                )
                            }

                            // Logout Card
                            ModernSettingCard(
                                icon = Icons.Filled.ExitToApp,
                                title = "Keluar Akun",
                                description = "Keluar dari akun Anda",
                                onClick = { showLogoutDialog = true },
                                isDestructive = true,
                                isLoading = profileUpdateState is ProfileUpdateState.Loading
                            )
                        }
                    }
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = profileUpdateState is ProfileUpdateState.Loading,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    ModernLoadingIndicator()
                }
            }
        }
    }

    // Dialogs
    if (showJenisListrikDialog) {
        ElectricityCapacityDialog(
            jenisListrikList = jenisListrikList,
            onDismiss = { showJenisListrikDialog = false },
            onSelectionChanged = { option ->
                val currentIsPrabayar = selectedPembayaranOption == "Prabayar"
                userViewModel.saveOnboardingChoices(option.batasDaya, currentIsPrabayar)
                showJenisListrikDialog = false
            },
            isLoading = profileUpdateState is ProfileUpdateState.Loading
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogOutClick()
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HeaderSection(
    userData: UserData?,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    3.dp,
                    MaterialTheme.colorScheme.surface,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title with animation
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(tween(300)) with fadeOut(tween(300))
            }
        ) { loading ->
            Text(
                text = if (loading) "Memperbarui..." else "Pengaturan Akun",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // User info
        userData?.let { user ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.email ?: "Email tidak tersedia",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernSettingCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon container
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDestructive) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDestructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Text content
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDestructive) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Arrow or loading indicator
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Additional content
            if (content != {}) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
private fun PaymentTypeSelector(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf("Prabayar", "Pascabayar").forEach { option ->
            val isSelected = selectedOption == option

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { onOptionSelected(option) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onOptionSelected(option) },
                        enabled = enabled,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ElectricityCapacityDialog(
    jenisListrikList: List<GolonganListrikEntity>,
    onDismiss: () -> Unit,
    onSelectionChanged: (GolonganListrikEntity) -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pilih Kapasitas Listrik",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Options list
                if (jenisListrikList.isEmpty()) {
                    ModernLoadingIndicator()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        jenisListrikList.forEach { option ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isLoading) {
                                        onSelectionChanged(option)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Build,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${option.golonganTarif} ${option.batasDaya} VA" +
                                                if (option.isRTM) " (RTM)" else "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal")
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Konfirmasi Keluar",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Apakah Anda yakin ingin keluar dari akun?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Keluar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(44.dp),
                strokeWidth = 3.dp,
                color = Color.Transparent
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Memuat...",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun ErrorStateComponent(
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gagal memuat data",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Silakan coba lagi",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Coba Lagi")
        }
    }
}