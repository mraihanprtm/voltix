package com.example.voltix.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        SimulationEntity::class,
        ElectronicsEntity::class,
        ElectronicsSimulation::class
               ],
    version = 1,
    exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun electronicsDao(): ElectronicsDao
    abstract fun simulationDao(): SimulationDao
}