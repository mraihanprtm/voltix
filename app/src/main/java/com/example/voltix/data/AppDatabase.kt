package com.example.voltix.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.voltix.data.dao.PerangkatDao
import com.example.voltix.data.dao.SimulasiDao
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.entity.PerangkatListrikEntity
import com.example.voltix.data.entity.SimulasiPerangkatCrossRef
import com.example.voltix.data.entity.SimulasiTagihanEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef

@Database(
    entities = [
        UserEntity::class,
        SimulasiTagihanEntity::class,
        PerangkatListrikEntity::class,
        SimulasiPerangkatCrossRef::class,
        UserPerangkatCrossRef::class],
    version = 1,
    exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun perangkatDao(): PerangkatDao
    abstract fun simulasiDao(): SimulasiDao
}