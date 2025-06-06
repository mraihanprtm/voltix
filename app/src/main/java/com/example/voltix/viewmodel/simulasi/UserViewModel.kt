package com.example.voltix.viewmodel // Sesuaikan package

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.remote.dto.ProfileUpdateRequest
import com.example.voltix.data.remote.dto.UserData
import com.example.voltix.data.repository.ListrikRepository
import com.example.voltix.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definisikan ProfileUpdateState di sini atau di file terpisah jika dipakai di banyak tempat
sealed class ProfileUpdateState {
    data object Idle : ProfileUpdateState()
    data object Loading : ProfileUpdateState()
    data class Success(val updatedUserFromBackend: UserData) : ProfileUpdateState()
    data class Error(val message: String?) : ProfileUpdateState()
}

/**
 * UserViewModel bertanggung jawab untuk menyediakan data dan status pengguna ke UI.
 * Ini memproxy data dari UserRepository dan mengelola state terkait profil.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val userRepository: UserRepository,
    private val listrikRepository: ListrikRepository // Untuk mapping jenisListrik ke ID lokal
) : ViewModel() {

    fun logout() {
        authManager.signOut()
    }

    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState.asStateFlow()

    // Langsung ekspos StateFlow dari UserRepository sebagai sumber kebenaran data pengguna
    val currentUserFromBackend: StateFlow<UserData?> = userRepository.currentUserProfile

    // State untuk menandakan proses loading profil pengguna
    private val _isUserProfileLoading = MutableStateFlow(false)
    val isUserProfileLoading: StateFlow<Boolean> = _isUserProfileLoading.asStateFlow()

    init {
        // Saat UserViewModel dibuat, jika currentUserFromBackend masih null (misalnya saat app start
        // dan belum ada data dari sesi sebelumnya), coba refresh profil dari backend.
        if (currentUserFromBackend.value == null) {
            refreshUserProfileFromBackend()
        }
    }

    /**
     * Dipanggil untuk me-refresh data pengguna dari backend.
     * Misalnya saat aplikasi dibuka atau saat diperlukan data terbaru.
     */
    fun refreshUserProfileFromBackend() {
        if (_isUserProfileLoading.value) {
            Log.d("UserViewModel", "refreshUserProfileFromBackend: Already loading, skipping refresh.")
            return
        }
        viewModelScope.launch {
            _isUserProfileLoading.value = true
            Log.d("UserViewModel", "refreshUserProfileFromBackend: Fetching user profile from backend...")
            val result = userRepository.fetchAndSetCurrentUserProfileFromBackend()
            result.fold(
                onSuccess = { userData ->
                    if (userData != null) {
                        Log.i("UserViewModel", "refreshUserProfileFromBackend: Profile refreshed successfully: ${userData.name}")
                        // Setelah profil berhasil diambil dari backend, sinkronkan ke Room lokal.
                        syncBackendUserToLocalRoom(userData)
                    } else {
                        Log.w("UserViewModel", "refreshUserProfileFromBackend: Fetched null user data, possibly logged out or no valid token.")
                    }
                },
                onFailure = { exception ->
                    Log.e("UserViewModel", "refreshUserProfileFromBackend: Failed to refresh profile: ${exception.message}")
                    // Jika gagal refresh, mungkin perlu membersihkan data lokal jika token tidak valid
                    // Ini sudah ditangani di UserRepository jika respons 401/403.
                }
            )
            _isUserProfileLoading.value = false
        }
    }

    /**
     * Menyimpan pilihan onboarding (jenis listrik dan status prabayar) ke backend.
     *
     * @param nilaiDayaListrik Kapasitas daya listrik yang dipilih.
     * @param statusPrabayar Status pembayaran (prabayar/pascabayar).
     */
    fun saveOnboardingChoices(nilaiDayaListrik: Int, statusPrabayar: Boolean) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            Log.d("UserViewModel", "saveOnboardingChoices: Attempting to save onboarding choices to backend: jenisListrik=$nilaiDayaListrik, isPrabayar=$statusPrabayar")

            val request = ProfileUpdateRequest(
                jenisListrik = nilaiDayaListrik,
                isPrabayar = statusPrabayar
            )

            val backendResult = userRepository.updateUserProfileOnBackend(request)

            backendResult.fold(
                onSuccess = { updatedUserDataFromBackend ->
                    Log.i("UserViewModel", "saveOnboardingChoices: Onboarding choices saved to backend successfully. UserData: $updatedUserDataFromBackend")
                    // Setelah update profil berhasil, sinkronkan ke database Room lokal.
                    syncBackendUserToLocalRoom(updatedUserDataFromBackend)
                    _profileUpdateState.value = ProfileUpdateState.Success(updatedUserDataFromBackend)
                },
                onFailure = { exception ->
                    Log.e("UserViewModel", "saveOnboardingChoices: Failed to save onboarding choices to backend: ${exception.message}")
                    _profileUpdateState.value = ProfileUpdateState.Error(exception.message)
                }
            )
        }
    }

    /**
     * Helper function untuk sinkronisasi UserData dari backend ke UserEntity di Room.
     * Ini memastikan data lokal selalu up-to-date dengan server.
     */
    private suspend fun syncBackendUserToLocalRoom(userDataFromBackend: UserData) {
        try {
            Log.d("UserViewModel", "syncBackendUserToLocalRoom: Starting sync for UID: ${userDataFromBackend.firebaseUid}")
            val dayaDariBackend = userDataFromBackend.jenisListrik
            var idGolonganUntukLokal: Int = 0

            // Cari ID golongan listrik lokal berdasarkan daya dari backend
            val semuaGolonganLokal = listrikRepository.getAllGolonganListrik() // Asumsi ini mengembalikan List<GolonganListrikEntity>
            if (dayaDariBackend != null) {
                val golonganCocok = semuaGolonganLokal.find { it.batasDaya == dayaDariBackend }
                idGolonganUntukLokal = golonganCocok?.idGolonganListrik ?: (semuaGolonganLokal.find { it.batasDaya == 2200 }?.idGolonganListrik ?: semuaGolonganLokal.firstOrNull()?.idGolonganListrik ?: 0)
            } else {
                // Jika daya dari backend null, gunakan default lokal
                idGolonganUntukLokal = semuaGolonganLokal.find { it.batasDaya == 2200 }?.idGolonganListrik ?: (semuaGolonganLokal.firstOrNull()?.idGolonganListrik ?: 0)
            }

            // Cek apakah user sudah ada di Room
            val localUserEntity = userRepository.getUserByUid(userDataFromBackend.firebaseUid)

            if (localUserEntity != null) {
                // Update user yang sudah ada
                val updatedLocalUserEntity = localUserEntity.copy(
                    name = userDataFromBackend.name ?: localUserEntity.name,
                    email = userDataFromBackend.email,
                    jenisListrik = idGolonganUntukLokal,
                    isPrabayar = userDataFromBackend.isPrabayar ?: localUserEntity.isPrabayar,
                    // foto_profil = userDataFromBackend.fotoProfil ?: localUserEntity.foto_profil // Uncomment jika ada
                )
                userRepository.updateUser(updatedLocalUserEntity)
                Log.i("UserViewModel", "syncBackendUserToLocalRoom: Local Room DB updated for existing user UID: ${userDataFromBackend.firebaseUid}")
            } else {
                // Insert user baru
                val newLocalUserEntity = UserEntity(
                    uid = userDataFromBackend.firebaseUid,
                    name = userDataFromBackend.name ?: "",
                    email = userDataFromBackend.email,
                    jenisListrik = idGolonganUntukLokal,
                    isPrabayar = userDataFromBackend.isPrabayar ?: false,
                    // foto_profil = userDataFromBackend.fotoProfil ?: "" // Uncomment jika ada
                )
                userRepository.insertUser(newLocalUserEntity)
                Log.i("UserViewModel", "syncBackendUserToLocalRoom: New user entry created in local Room DB for UID: ${userDataFromBackend.firebaseUid}")
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "syncBackendUserToLocalRoom: Exception during local DB sync: ${e.message}", e)
        }
    }

    /**
     * Mereset state update profil ke Idle.
     * Dipanggil setelah UI selesai menangani Success/Error.
     */
    fun resetProfileUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Idle
    }

    // --- Fungsi-fungsi lokal lainnya yang mungkin memproxy dari UserRepository ---
    // Pastikan ini sesuai dengan kebutuhan Anda.

    suspend fun getCurrentUserLocally(): UserEntity? {
        return userRepository.getCurrentUser().getOrNull()
    }
    suspend fun getUserByUidLocally(uid: String): UserEntity? {
        return userRepository.getUserByUid(uid)
    }

    fun insertUserLocally(user: UserEntity) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "insertUserLocally: Inserting User locally (DAO): ${user.name}")
                userRepository.insertUser(user)
            } catch (e: Exception) {
                Log.e("UserViewModel", "insertUserLocally: Error inserting user locally: ${e.message}")
            }
        }
    }

    fun updateUserLocally(user: UserEntity) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "updateUserLocally: Updating User locally (DAO): ${user.name}")
                userRepository.updateUser(user)
            } catch (e: Exception) {
                Log.e("UserViewModel", "updateUserLocally: Error updating user locally: ${e.message}")
            }
        }
    }

    suspend fun getUserTarif(userIdFromLocalDb: Int, usedKWH: Double): Int {
        return userRepository.getUserTarif(userIdFromLocalDb, usedKWH)
    }
}
