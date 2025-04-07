package com.example.voltix.data.repository

import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    val auth: FirebaseAuth
) {
    suspend fun createUser(name: String, email: String, jenisListrik: String = "", fotoProfil: String = ""): Result<Int> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val userEntity = UserEntity(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                jenisListrik = jenisListrik,
                foto_profil = fotoProfil
            )
            val userId = userDao.insertUser(userEntity).toInt()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserByUid(uid: String): UserEntity? {
        return userDao.getUserByUid(uid)
    }
}