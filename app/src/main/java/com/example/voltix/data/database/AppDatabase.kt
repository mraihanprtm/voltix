package com.example.voltix.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.voltix.data.dao.PerangkatDao
import com.example.voltix.data.dao.SimulasiPerangkatDao
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.entity.PerangkatListrikEntity
import com.example.voltix.data.entity.SimulasiPerangkatEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef

@Database(
    entities = [UserEntity::class, SimulasiPerangkatEntity::class, PerangkatListrikEntity::class, UserPerangkatCrossRef::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun perangkatDao(): PerangkatDao
    abstract fun simulasiDao(): SimulasiPerangkatDao

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