package com.example.voltix.data.repository

import android.util.Log
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.dao.UserPerangkatCrossRefDao
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef
import com.example.voltix.data.relations.UserWithPerangkat
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userPerangkatCrossRefDao: UserPerangkatCrossRefDao,
    val auth: FirebaseAuth
) {
    suspend fun createUser(name: String, email: String, jenisListrik: Int = 2200, fotoProfil: String = ""): Result<Int> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

            // Check if user already exists
            val existingUser = getUserByUid(firebaseUser.uid)
            if (existingUser != null) {
                Log.d(TAG, "User already exists: $existingUser")
                return Result.success(existingUser.id)
            }

            val userEntity = UserEntity(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                jenisListrik = jenisListrik,
                foto_profil = fotoProfil
            )

            Log.d(TAG, "Creating new user: $userEntity")
            val userId = userDao.insertUser(userEntity).toInt()

            // Verify creation
            val createdUser = userDao.getUserById(userId)
            Log.d(TAG, "Created user verification: $createdUser")

            Result.success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user", e)
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            val user = userDao.getUserByEmail(email)
            Log.d(TAG, "Retrieved user by email $email: $user")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by email", e)
            null
        }
    }

    suspend fun getUserByUid(uid: String): UserEntity? {
        return try {
            val user = userDao.getUserByUid(uid)
            Log.d(TAG, "Retrieved user by UID $uid: $user")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by UID", e)
            null
        }
    }

    suspend fun insertUser(user: UserEntity): Long {
        return try {
            Log.d(TAG, "Inserting user: $user")
            val id = userDao.insertUser(user)

            // Verify insertion
            val insertedUser = userDao.getUserById(id.toInt())
            Log.d(TAG, "Inserted user verification: $insertedUser")

            id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user", e)
            throw e
        }
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> {
        return try {
            Log.d(TAG, "Updating user: $user")
            userDao.updateUser(user)

            // Verify update
            val updatedUser = userDao.getUserById(user.id)
            Log.d(TAG, "Updated user verification: $updatedUser")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            Result.failure(e)
        }
    }

    suspend fun updateJenisListrik(userId: Int, jenisListrik: Int): Result<Unit> {
        return try {
            Log.d(TAG, "Updating jenisListrik for userId $userId to $jenisListrik")

            val user = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))

            val updatedUser = user.copy(jenisListrik = jenisListrik)
            userDao.updateUser(updatedUser)

            // Verify update
            val verifiedUser = userDao.getUserById(userId)
            Log.d(TAG, "JenisListrik update verification: $verifiedUser")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating jenisListrik", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserEntity?> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                Log.d(TAG, "No Firebase user logged in")
                return Result.success(null)
            }

            val user = getUserByUid(firebaseUser.uid)
            Log.d(TAG, "Current user: $user")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            Result.failure(e)
        }
    }

    // Remaining functions with added logging
    suspend fun getUserWithPerangkat(userId: Int): UserWithPerangkat {
        return try {
            val userWithPerangkat = userDao.getUserWithPerangkat(userId)
            Log.d(TAG, "Retrieved UserWithPerangkat for userId $userId: $userWithPerangkat")
            userWithPerangkat
        } catch (e: Exception) {
            Log.e(TAG, "Error getting UserWithPerangkat", e)
            throw e
        }
    }

    suspend fun insertUserPerangkatCrossRef(crossRef: UserPerangkatCrossRef) {
        try {
            Log.d(TAG, "Inserting UserPerangkatCrossRef: $crossRef")
            userPerangkatCrossRefDao.insert(crossRef)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting UserPerangkatCrossRef", e)
            throw e
        }
    }

    suspend fun deleteUser(user: UserEntity): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting user: $user")
            userDao.deleteUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
            Result.failure(e)
        }
    }
}