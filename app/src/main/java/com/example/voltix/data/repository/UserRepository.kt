package com.example.voltix.data.repository

import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef
import com.example.voltix.data.relations.UserWithPerangkat
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun getUserWithPerangkat(userId: Int): UserWithPerangkat {
        return userDao.getUserWithPerangkat(userId)
    }

    suspend fun insertUserPerangkatCrossRef(ref: UserPerangkatCrossRef) {
        userDao.insertUserPerangkatCrossRef(ref)
    }

    suspend fun getUser(): UserEntity{
        return userDao.getUser()
    }
}