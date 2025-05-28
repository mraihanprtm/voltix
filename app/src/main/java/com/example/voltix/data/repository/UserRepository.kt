package com.example.voltix.data.repository

import android.util.Log
import com.example.voltix.data.dao.GolonganListrikDao
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.dao.UserPerangkatCrossRefDao
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.GolonganListrikEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef
import com.example.voltix.data.relations.UserWithPerangkat
import com.example.voltix.data.remote.api.AuthApiService
import com.example.voltix.data.remote.dto.ProfileUpdateRequest
import com.example.voltix.data.remote.dto.UserData
import com.example.voltix.data.util.TokenManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

/**
 * UserRepository bertanggung jawab untuk mengelola data pengguna, baik dari backend (API)
 * maupun dari database lokal (Room). Ini adalah Single Source of Truth untuk data pengguna.
 */
@Singleton
class UserRepository @Inject constructor(
    private val authApiService: AuthApiService,
    val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao, // DAO untuk operasi Room UserEntity
    private val userPerangkatCrossRefDao: UserPerangkatCrossRefDao, // DAO untuk relasi perangkat
    private val golonganListrikDao: GolonganListrikDao // DAO untuk golongan listrik
) {

    // StateFlow ini adalah sumber kebenaran utama untuk data profil pengguna yang sedang login.
    // Semua ViewModel yang membutuhkan data pengguna harus mengobservasi ini.
    private val _currentUserProfile = MutableStateFlow<UserData?>(value = null)
    val currentUserProfile: StateFlow<UserData?> = _currentUserProfile.asStateFlow()

    /**
     * Mengatur atau memperbarui data profil pengguna saat ini di StateFlow.
     * Dipanggil setelah login/registrasi/update profil berhasil dari AuthManager.
     *
     * @param userData Data pengguna yang akan diatur. Jika null, profil akan dibersihkan.
     */
    fun setCurrentUserProfile(userData: UserData?) {
        _currentUserProfile.value = userData
        if (userData != null) {
            Log.i(TAG, "Current user profile SET in UserRepository: ${userData.name}, Email: ${userData.email}, JenisListrik: ${userData.jenisListrik}")
            // Opsional: Panggil sinkronisasi ke Room di sini jika setiap update dari backend
            // harus segera tercermin di Room.
            // syncBackendUserToLocalRoom(userData) // Membutuhkan CoroutineScope dan fungsi sync
        } else {
            Log.i(TAG, "Current user profile CLEARED in UserRepository.")
        }
    }

    /**
     * Mengupdate profil pengguna (misalnya jenis_listrik, isPrabayar) ke backend Laravel.
     * Setelah berhasil, akan memperbarui _currentUserProfile.
     *
     * @param profileUpdateRequest Objek request yang berisi data profil yang akan diupdate.
     * @return Result<UserData> yang berisi data pengguna yang diperbarui jika sukses.
     */
    suspend fun updateUserProfileOnBackend(profileUpdateRequest: ProfileUpdateRequest): Result<UserData> {
        val token = tokenManager.getApiToken()
        if (token.isNullOrBlank()) {
            Log.e(TAG, "updateUserProfileOnBackend: API Token not found, cannot update profile.")
            return Result.failure(Exception("API Token not found"))
        }

        return try {
            Log.d(TAG, "updateUserProfileOnBackend: Requesting update with $profileUpdateRequest")
            val response = authApiService.updateUserProfile("Bearer $token", profileUpdateRequest)
            if (response.isSuccessful && response.body() != null && response.body()!!.success == true) {
                val updatedUser = response.body()!!.user // Asumsi respons memiliki field 'success' dan 'user'
                _currentUserProfile.value = updatedUser // Update StateFlow setelah berhasil update
                Log.i(TAG, "Profil berhasil diupdate di backend. User: $updatedUser")
                Result.success(updatedUser)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Gagal update profil (Code: ${response.code()})"
                Log.e(TAG, "updateUserProfileOnBackend: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUserProfileOnBackend: Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Mengambil data profil pengguna yang sedang terautentikasi dari backend Laravel.
     * Ini adalah cara utama untuk mendapatkan data user terkini dari server.
     * Setelah berhasil, akan memperbarui _currentUserProfile.
     *
     * @return Result<UserData?> yang berisi data pengguna jika sukses, atau null jika tidak ada.
     */
    suspend fun fetchAndSetCurrentUserProfileFromBackend(): Result<UserData?> {
        val token = tokenManager.getApiToken()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "fetchAndSetCurrentUserProfileFromBackend: API Token tidak ditemukan. Membersihkan profil.")
            _currentUserProfile.value = null // Pastikan state di-clear jika token tidak ada
            return Result.success(null)
        }
        Log.d(TAG, "fetchAndSetCurrentUserProfileFromBackend: Attempting to fetch profile.")
        return try {
            val response = authApiService.getCurrentUserProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val userData = response.body()!! // Ini adalah UserData langsung dari respons
                _currentUserProfile.value = userData // Update StateFlow
                Log.i(TAG, "Profil pengguna berhasil diambil dari backend dan di-set: ${userData.name}")
                Result.success(userData)
            } else {
                val errorMsg = "Gagal ambil profil dari backend: ${response.message()} (Code: ${response.code()})"
                Log.e(TAG, errorMsg)
                _currentUserProfile.value = null // Clear state jika fetch gagal
                if (response.code() == 401 || response.code() == 403) { // Token tidak valid/kedaluwarsa
                    tokenManager.clearApiToken()
                    Log.w(TAG, "Token API tidak valid, token dibersihkan.")
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMessage = "Exception saat mengambil profil pengguna dari backend: ${e.message}"
            Log.e(TAG, errorMessage, e)
            _currentUserProfile.value = null // Clear state jika exception
            Result.failure(Exception(errorMessage))
        }
    }

    // --- Fungsi-fungsi Room DAO untuk data lokal ---
    // Pastikan fungsi-fungsi ini konsisten dan tidak menyebabkan konflik dengan data backend.
    // Biasanya, data dari backend adalah "source of truth" setelah login.

    suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            val user = userDao.getUserByEmail(email)
            Log.d(TAG, "Retrieved user by email $email from Room: $user")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by email from Room", e)
            null
        }
    }

    suspend fun getUserByUid(uid: String): UserEntity? {
        return try {
            val user = userDao.getUserByUid(uid)
            Log.d(TAG, "Retrieved user by UID $uid from Room: $user")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by UID from Room", e)
            null
        }
    }

    suspend fun insertUser(user: UserEntity): Long {
        return try {
            Log.d(TAG, "Inserting user to Room: $user")
            val id = userDao.insertUser(user)

            // Verify insertion
            val insertedUser = userDao.getUserById(id.toInt())
            Log.d(TAG, "Inserted user verification in Room: $insertedUser")

            id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user to Room", e)
            throw e // Re-throw untuk ditangani di layer atas jika perlu
        }
    }

    suspend fun updateUser(user: UserEntity) {
        try {
            Log.d(TAG, "Updating user in Room: $user")
            userDao.updateUser(user)

            // Verify update
            val updatedUser = userDao.getUserById(user.id)
            Log.d(TAG, "Updated user verification in Room: $updatedUser")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating user in Room", e)
            // Tidak melempar exception karena ini adalah update lokal, bisa ditangani internal
        }
    }

    suspend fun updateJenisListrik(userId: Int, jenisListrik: Int): Result<Unit> {
        return try {
            Log.d(TAG, "Updating jenisListrik for userId $userId to $jenisListrik in Room")

            val user = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found in Room"))

            val updatedUser = user.copy(jenisListrik = jenisListrik)
            userDao.updateUser(updatedUser)

            // Verify update
            val verifiedUser = userDao.getUserById(userId)
            Log.d(TAG, "JenisListrik update verification in Room: $verifiedUser")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating jenisListrik in Room", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserEntity?> { // Mengembalikan Result agar konsisten
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                Log.d(TAG, "getCurrentUser: Firebase user is null.")
                Result.success(null)
            } else {
                val localUser = userDao.getUserByUid(firebaseUser.uid)
                Log.d(TAG, "getCurrentUser: Retrieved local user by UID ${firebaseUser.uid}: $localUser")
                Result.success(localUser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user from Room", e)
            Result.failure(e)
        }
    }

    suspend fun getUserWithPerangkat(userId: Int): UserWithPerangkat {
        return try {
            val userWithPerangkat = userDao.getUserWithPerangkat(userId)
            Log.d(TAG, "Retrieved UserWithPerangkat for userId $userId from Room: $userWithPerangkat")
            userWithPerangkat
        } catch (e: Exception) {
            Log.e(TAG, "Error getting UserWithPerangkat from Room", e)
            throw e
        }
    }

    suspend fun insertUserPerangkatCrossRef(crossRef: UserPerangkatCrossRef) {
        try {
            Log.d(TAG, "Inserting UserPerangkatCrossRef to Room: $crossRef")
            userPerangkatCrossRefDao.insert(crossRef)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting UserPerangkatCrossRef to Room", e)
            throw e
        }
    }

    suspend fun deleteUser(user: UserEntity): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting user from Room: $user")
            userDao.deleteUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user from Room", e)
            Result.failure(e)
        }
    }

    suspend fun getUserGolonganListrik(id: Int): Int {
        return userDao.getUserGolonganListrik(id)
    }

    suspend fun getUserisPrabayar(id: Int): Boolean{
        return userDao.getUserisPrabayar(id)
    }

    suspend fun getUserBiayaListrik(id: Int): List<GolonganListrikDenganBiaya> {
        val userGolonganListrik = getUserGolonganListrik(id)
        return golonganListrikDao.getBiayaTarifListrik(userGolonganListrik)
    }

    suspend fun getUserTarif(id: Int, usedKWH: Double): Int {
        val isPrabayar = getUserisPrabayar(id)
        var biayaListrik = getUserBiayaListrik(id)
        biayaListrik = biayaListrik.asReversed() // Pastikan ini sesuai logika Anda
        val golonganTarif = biayaListrik.firstOrNull { usedKWH >= it.minKWH }

        if (golonganTarif == null) {
            Log.e(TAG, "Tidak ada golongan tarif yang sesuai untuk KWH: $usedKWH")
            throw IllegalStateException("Tidak ditemukan golongan tarif yang sesuai.")
        }

        return if (isPrabayar) {
            Log.d(TAG, "Biaya Tarif Listrik (Prabayar) = ${golonganTarif.biayaPrabayar}")
            golonganTarif.biayaPrabayar
        } else {
            Log.d(TAG, "Biaya Tarif Listrik (Reguler) = ${golonganTarif.biayaReguler}")
            golonganTarif.biayaReguler
        }
    }

    suspend fun getUserBatasDaya(id: Int): Int{
        val userGolonganListrik = getUserBiayaListrik(id)
        var batasDaya = userGolonganListrik.firstOrNull()!!.batasDaya
        return batasDaya
    }

    suspend fun getAllGolonganListrik(): Result<List<GolonganListrikDenganBiaya>> {
        return try {
            val allGolongan = golonganListrikDao.getAllGolonganListrikwithBiaya()
            Log.d(TAG, "Retrieved all golongan listrik from Room: $allGolongan")
            Result.success(allGolongan)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all golongan listrik from Room", e)
            Result.failure(e)
        }
    }
}
