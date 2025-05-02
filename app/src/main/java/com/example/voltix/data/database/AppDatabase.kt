package com.example.voltix.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.voltix.data.dao.PerangkatDAO
import com.example.voltix.data.dao.RuanganDAO
import com.example.voltix.data.dao.RuanganPerangkatCrossRefDAO

import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.dao.UserPerangkatCrossRefDao
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef


import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef

@Database(
    entities = [UserEntity::class, PerangkatEntity::class, RuanganEntity::class, RuanganPerangkatCrossRef::class, UserPerangkatCrossRef::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userPerangkatCrossRefDao(): UserPerangkatCrossRefDao
    abstract fun perangkatDao(): PerangkatDAO
    abstract fun ruanganDao(): RuanganDAO
    abstract fun ruanganPerangkatCrossRefDao(): RuanganPerangkatCrossRefDAO  // Tambahkan ini

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voltix_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}