package com.example.voltix.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.voltix.R
import com.example.voltix.data.remote.response.AuthResponse
import com.example.voltix.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

class AuthManager @Inject constructor(
    private val context: Context,
    private val userRepository: UserRepository
) {
    private val auth = Firebase.auth

    // Fungsi untuk menyimpan user ke Room setelah autentikasi berhasil
    private suspend fun saveUserToRoom(email: String, name: String = ""): Result<Int> {
        val displayName = name.ifEmpty { auth.currentUser?.displayName ?: email.substringBefore('@') }
        val photoUrl = auth.currentUser?.photoUrl?.toString() ?: ""

        return userRepository.createUser(
            name = displayName,
            email = email,
            fotoProfil = photoUrl,
        )
    }

    fun createAccountWithEmail(email: String, password: String, name: String = ""): Flow<AuthResponse> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = saveUserToRoom(email, name)
                        if (result.isSuccess) {
                            trySend(AuthResponse.Success)
                        } else {
                            trySend(AuthResponse.Success) // atau bisa juga dengan pesan warning
                        }
                    }
                } else {
                    trySend(AuthResponse.Error(message = task.exception?.message ?: "Unknown Error"))
                }
            }
        awaitClose()
    }

    fun loginWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow {
        CoroutineScope(Dispatchers.IO).launch {
            val localUser = userRepository.getUserByEmail(email)

            if (localUser == null) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.IO).launch {
                                saveUserToRoom(email)
                                trySend(AuthResponse.Success)
                            }
                        } else {
                            trySend(AuthResponse.Error(message = task.exception?.message ?: "Unknown Error"))
                        }
                    }
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(AuthResponse.Success)
                        } else {
                            trySend(AuthResponse.Error(message = task.exception?.message ?: "Unknown Error"))
                        }
                    }
            }
        }
        awaitClose()
    }

    fun sendPasswordResetEmail(email: String): Flow<AuthResponse> = callbackFlow {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                } else {
                    trySend(AuthResponse.Error(message = task.exception?.message ?: "Failed to send reset email"))
                }
            }
        awaitClose()
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    fun signInWithGoogle(): Flow<AuthResponse> = callbackFlow {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential) {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val firebaseCredential = GoogleAuthProvider
                            .getCredential(
                                googleIdTokenCredential.idToken,
                                null
                            )

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val email = auth.currentUser?.email ?: ""
                                        val displayName = auth.currentUser?.displayName ?: ""

                                        if (email.isNotEmpty()) {
                                            val localUser = userRepository.getUserByEmail(email)
                                            if (localUser == null) {
                                                saveUserToRoom(email, displayName)
                                            }
                                            trySend(AuthResponse.Success)
                                        } else {
                                            trySend(AuthResponse.Error(message = "No email found from Google account"))
                                        }
                                    }
                                } else {
                                    trySend(AuthResponse.Error(message = it.exception?.message ?: ""))
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        trySend(AuthResponse.Error(message = e.message ?: ""))
                    }
                }
            }
        } catch (e: Exception) {
            trySend(AuthResponse.Error(message = e.message ?: ""))
        }
        awaitClose()
    }
}