package com.example.voltix.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PerangkatEntity::class, SimulasiPerangkatEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
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
