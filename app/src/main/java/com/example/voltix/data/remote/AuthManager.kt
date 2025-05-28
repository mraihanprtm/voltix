package com.example.voltix.data.remote

import android.content.Context
import android.util.Log
import com.example.voltix.data.remote.api.AuthApiService
import com.example.voltix.data.remote.dto.FirebaseLoginRequest
import com.example.voltix.data.remote.dto.UserData
import com.example.voltix.data.remote.response.AuthResponse
import com.example.voltix.data.repository.UserRepository
import com.example.voltix.data.util.TokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthManager bertanggung jawab untuk semua operasi autentikasi (Firebase dan sinkronisasi backend).
 * Ini adalah jembatan antara Firebase SDK, API backend, dan UserRepository.
 */
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val authApiService: AuthApiService,
    private val userRepository: UserRepository, // Di-inject untuk mengupdate profil pengguna
    private val tokenManager: TokenManager
) {

    /**
     * Melakukan sinkronisasi token Firebase ID dengan backend Laravel.
     * Backend akan memverifikasi token dan mengembalikan API token Laravel serta data pengguna.
     *
     * @param firebaseIdToken Token ID dari Firebase yang baru didapat.
     * @param providedName Nama pengguna (opsional, biasanya untuk registrasi baru).
     * @return AuthResponse yang menunjukkan sukses (dengan UserData) atau error.
     */
    private suspend fun syncWithBackend(
        firebaseIdToken: String,
        providedName: String? = null
    ): AuthResponse {
        return try {
            val request = FirebaseLoginRequest(firebaseIdToken = firebaseIdToken, name = providedName)
            Log.d("AuthManager", "syncWithBackend: Attempting to sync with backend. Request: $request")
            val response = authApiService.loginOrRegisterWithFirebaseToken(request)
            Log.d("AuthManager", "syncWithBackend: Backend response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val backendResponse = response.body()!! // Asumsi ini adalah DTO utama dari backend
                Log.d("AuthManager", "syncWithBackend: Backend response successful. Body: $backendResponse")

                // Simpan token Laravel dari respons backend
                backendResponse.apiToken?.let {
                    tokenManager.saveApiToken(it)
                    Log.d("AuthManager", "syncWithBackend: Laravel API token saved.")
                }

                // Ambil UserData dari backend (pastikan struktur DTO backendResponse sesuai)
                val userDataFromApi = backendResponse.user // Ganti 'user' jika nama fieldnya berbeda
                if (userDataFromApi != null) {
                    // === PERUBAHAN KUNCI: UPDATE USER PROFILE DI REPOSITORY ===
                    userRepository.setCurrentUserProfile(userDataFromApi)
                    Log.i("AuthManager", "syncWithBackend: UserRepository._currentUserProfile updated with UserData from backend: ${userDataFromApi.name}")
                    return AuthResponse.Success(userDataFromApi) // Kembalikan UserData
                } else {
                    Log.e("AuthManager", "syncWithBackend: UserData from backend is null in successful response body.")
                    return AuthResponse.Error("Data pengguna tidak ditemukan dari server meskipun login berhasil.")
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Gagal sinkronisasi dengan server (Code: ${response.code()})"
                Log.e("AuthManager", "syncWithBackend: GAGAL: $errorMsg")
                if (response.code() == 401) { // Jika token tidak valid, bersihkan
                    tokenManager.clearApiToken()
                    userRepository.setCurrentUserProfile(null) // Bersihkan profil juga
                }
                return AuthResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "syncWithBackend: Exception: ${e.message}", e)
            return AuthResponse.Error("Error koneksi: ${e.message}")
        }
    }

    /**
     * Mendaftarkan pengguna baru dengan email dan password menggunakan Firebase.
     * Setelah berhasil, akan mengirim email verifikasi dan menyinkronkan dengan backend.
     *
     * @param email Email pengguna.
     * @param password Password pengguna.
     * @param name Nama pengguna.
     * @return Flow<AuthResponse> yang menunjukkan status operasi.
     */
    fun registerWithEmail(email: String, password: String, name: String): Flow<AuthResponse> = callbackFlow {
        trySend(AuthResponse.Loading)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = task.result?.user
                    if (firebaseUser != null) {
                        // Kirim email verifikasi setelah akun dibuat
                        firebaseUser.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Log.d("AuthManager", "registerWithEmail: Email verifikasi berhasil dikirim ke ${firebaseUser.email}")
                                } else {
                                    Log.e("AuthManager", "registerWithEmail: Gagal mengirim email verifikasi.", verificationTask.exception)
                                }
                                // Lanjutkan dengan mendapatkan Firebase ID token dan sinkronisasi ke backend
                                firebaseUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                                    if (tokenTask.isSuccessful) {
                                        val idToken = tokenTask.result?.token
                                        if (idToken != null) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val backendResult = syncWithBackend(idToken, name)
                                                // Opsional: Jika ingin paksa user verifikasi sebelum bisa login pertama kali:
                                                // if (backendResult is AuthResponse.Success) {
                                                //     firebaseAuth.signOut()
                                                //     userRepository.setCurrentUserProfile(null) // Bersihkan profil lokal
                                                //     tokenManager.clearApiToken() // Bersihkan token Laravel
                                                //     Log.i("AuthManager", "User signed out after registration for email verification.")
                                                // }
                                                trySend(backendResult)
                                                close()
                                            }
                                        } else {
                                            trySend(AuthResponse.Error("Gagal mendapatkan Firebase token setelah registrasi."))
                                            close()
                                        }
                                    } else {
                                        trySend(AuthResponse.Error(tokenTask.exception?.message ?: "Gagal task Firebase token (registrasi)."))
                                        close()
                                    }
                                }
                            }
                    } else {
                        trySend(AuthResponse.Error("Pengguna Firebase null setelah registrasi Firebase berhasil."))
                        close()
                    }
                } else {
                    trySend(AuthResponse.Error(task.exception?.message ?: "Registrasi Firebase gagal."))
                    close()
                }
            }
        awaitClose { /* cleanup jika ada listener yang perlu di-remove */ }
    }

    /**
     * Melakukan login pengguna dengan email dan password menggunakan Firebase.
     * Akan memeriksa apakah email sudah diverifikasi sebelum melanjutkan.
     *
     * @param email Email pengguna.
     * @param password Password pengguna.
     * @return Flow<AuthResponse> yang menunjukkan status operasi.
     */
    fun loginWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow {
        trySend(AuthResponse.Loading)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = task.result?.user
                    if (firebaseUser != null) {
                        // === PERUBAHAN KUNCI: PENGECEKAN VERIFIKASI EMAIL ===
                        if (firebaseUser.isEmailVerified) {
                            // Email sudah diverifikasi, lanjutkan proses login normal
                            firebaseUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val idToken = tokenTask.result?.token
                                    if (idToken != null) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val backendResult = syncWithBackend(idToken, null) // null untuk nama karena ini login
                                            trySend(backendResult)
                                            close()
                                        }
                                    } else {
                                        trySend(AuthResponse.Error("Gagal mendapatkan Firebase token setelah login (email terverifikasi)."))
                                        close()
                                    }
                                } else {
                                    trySend(AuthResponse.Error(tokenTask.exception?.message ?: "Gagal task Firebase token (email terverifikasi)."))
                                    close()
                                }
                            }
                        } else {
                            // Email belum diverifikasi
                            Log.w("AuthManager", "loginWithEmail: Login attempt untuk email yang belum diverifikasi: ${firebaseUser.email}")
                            // Opsional: signOut pengguna dari Firebase agar sesi tidak aktif jika email belum diverifikasi
                            // firebaseAuth.signOut()
                            // userRepository.setCurrentUserProfile(null) // Bersihkan profil
                            // tokenManager.clearApiToken() // Bersihkan token
                            trySend(AuthResponse.Error("Email Anda belum diverifikasi. Silakan cek inbox email Anda untuk link verifikasi."))
                            close()
                        }
                    } else {
                        trySend(AuthResponse.Error("Pengguna Firebase null meskipun login Firebase berhasil."))
                        close()
                    }
                } else {
                    trySend(AuthResponse.Error(task.exception?.message ?: "Login Firebase gagal."))
                    close()
                }
            }
        awaitClose { /* cleanup */ }
    }

    /**
     * Mendapatkan GoogleSignInClient untuk memulai alur Google Sign-In di UI.
     *
     * @return GoogleSignInClient yang sudah dikonfigurasi.
     */
    private fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.example.voltix.R.string.web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Melakukan sign-in ke Firebase menggunakan kredensial Google (idToken).
     * Setelah berhasil, akan menyinkronkan dengan backend.
     *
     * @param idTokenGoogle ID Token dari Google Sign-In SDK.
     * @return Flow<AuthResponse> yang menunjukkan status operasi.
     */
    fun signInWithGoogleCredential(idTokenGoogle: String): Flow<AuthResponse> = callbackFlow {
        trySend(AuthResponse.Loading)
        val credential = GoogleAuthProvider.getCredential(idTokenGoogle, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    // Untuk Google Sign-In, Firebase biasanya otomatis menandai email sebagai terverifikasi
                    // jika akun Google-nya sudah terverifikasi. Pengecekan isEmailVerified di sini opsional.
                    if (firebaseUser != null) {
                        firebaseUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idTokenFirebase = tokenTask.result?.token
                                if (idTokenFirebase != null) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val backendResult = syncWithBackend(idTokenFirebase, firebaseUser.displayName)
                                        trySend(backendResult)
                                        close()
                                    }
                                } else {
                                    trySend(AuthResponse.Error("Gagal mendapatkan Firebase token setelah Google Sign-In."))
                                    close()
                                }
                            } else {
                                trySend(AuthResponse.Error(tokenTask.exception?.message ?: "Gagal task Firebase token dari Google Sign-In."))
                                close()
                            }
                        }
                    } else {
                        trySend(AuthResponse.Error("Pengguna Firebase null setelah Google Sign-In berhasil."))
                        close()
                    }
                } else {
                    Log.e("AuthManager", "signInWithGoogleCredential: Firebase Google Sign-In GAGAL", task.exception)
                    trySend(AuthResponse.Error(task.exception?.message ?: "Firebase Google Sign-In gagal."))
                    close()
                }
            }
        awaitClose { /* cleanup */ }
    }

    /**
     * Mengirim email reset password ke alamat email yang diberikan.
     *
     * @param email Alamat email tujuan pengiriman link reset.
     * @return Flow<AuthResponse> yang menunjukkan status operasi.
     */
    fun sendPasswordResetEmail(email: String): Flow<AuthResponse> = callbackFlow {
        trySend(AuthResponse.Loading)
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success(null)) // Sukses mengirim email, tidak ada UserData
                } else {
                    trySend(AuthResponse.Error(task.exception?.message ?: "Gagal mengirim email reset."))
                }
                close()
            }
        awaitClose { /* cleanup */ }
    }

    /**
     * Melakukan sign out pengguna dari Firebase dan membersihkan token lokal.
     */
    fun signOut() {
        firebaseAuth.signOut()
        tokenManager.clearApiToken()
        userRepository.setCurrentUserProfile(null) // BERSIHKAN JUGA PROFIL DI REPOSITORY
        Log.d("AuthManager", "User signed out, Laravel token & user profile cleared.")
    }
}
