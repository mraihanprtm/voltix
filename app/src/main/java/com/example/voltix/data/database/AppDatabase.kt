package com.example.voltix.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration // 1. Make sure Migration is imported
import androidx.sqlite.db.SupportSQLiteDatabase // 2. Make sure this is imported
import com.example.voltix.data.dao.GolonganListrikDao
import com.example.voltix.data.dao.PerangkatDAO
import com.example.voltix.data.dao.RekomendasiDao
import com.example.voltix.data.dao.RuanganDAO
import com.example.voltix.data.dao.RuanganPerangkatCrossRefDAO
import com.example.voltix.data.dao.SimulationDAO
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.dao.UserPerangkatCrossRefDao
import com.example.voltix.data.entity.BiayaPemakaianEntity
import com.example.voltix.data.entity.GolonganListrikEntity
import com.example.voltix.data.entity.LampuEntity
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RekomendasiPenghematanLampuEntity
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.entity.SimulationDeviceEntity
import com.example.voltix.data.entity.SimulationEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef

@Database(
    entities = [
        UserEntity::class,
        PerangkatEntity::class,
        RuanganEntity::class,
        LampuEntity::class,
        RuanganPerangkatCrossRef::class,
        UserPerangkatCrossRef::class,
        SimulationEntity::class,
        SimulationDeviceEntity::class,
        GolonganListrikEntity::class,
        BiayaPemakaianEntity::class,
        RekomendasiPenghematanLampuEntity::class
    ],
    version = 3, // This is correct
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userPerangkatCrossRefDao(): UserPerangkatCrossRefDao
    abstract fun perangkatDao(): PerangkatDAO
    abstract fun ruanganDao(): RuanganDAO
    abstract fun ruanganPerangkatCrossRefDao(): RuanganPerangkatCrossRefDAO
    abstract fun simulationDao(): SimulationDAO
    abstract fun rekomendasiDao(): RekomendasiDao
    abstract fun golonganListrikDao(): GolonganListrikDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 3. Define the Migration object
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the 'uuid' column to each table that needs to be synced.
                // The DEFAULT value will automatically populate existing rows.
                database.execSQL("ALTER TABLE perangkat ADD COLUMN uuid TEXT NOT NULL DEFAULT(hex(randomblob(16)))")
                database.execSQL("ALTER TABLE ruangan ADD COLUMN uuid TEXT NOT NULL DEFAULT(hex(randomblob(16)))")
                database.execSQL("ALTER TABLE lampu ADD COLUMN uuid TEXT NOT NULL DEFAULT(hex(randomblob(16)))")

                // Create an index on the new UUID columns for faster lookups
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_perangkat_uuid ON perangkat(uuid)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_ruangan_uuid ON ruangan(uuid)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_lampu_uuid ON lampu(uuid)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voltix_database"
                )
                    // 4. Remove destructive migration and add your safe migration
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}