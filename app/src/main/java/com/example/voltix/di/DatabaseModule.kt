package com.example.voltix.di

import android.content.Context
import androidx.room.Room
import com.example.voltix.data.AppDatabase
import com.example.voltix.data.dao.SimulasiPerangkatDao
import com.example.voltix.data.dao.PerangkatDao
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.repository.SimulasiRepository
import com.example.voltix.data.repository.UserRepository
import com.example.voltix.repository.PerangkatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // membuat instansi Room Database
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voltix_database"
        ).fallbackToDestructiveMigration() .build()

    }

    @Provides
    fun providePerangkatdao(db: AppDatabase): PerangkatDao = db.perangkatDao()

    @Provides
    fun provideSimulasiDao(db: AppDatabase): SimulasiPerangkatDao = db.simulasiDao()

    @Provides
    fun provideUserDao(db:AppDatabase): UserDao = db.userDao()

    // ✅ TAMBAHKAN INI:
    @Provides
    fun providePerangkatRepository(perangkatDao: PerangkatDao): PerangkatRepository {
        return PerangkatRepository(perangkatDao)
    }

    @Provides
    fun provideUserRepository(userDao: UserDao): UserRepository{
        return UserRepository(userDao)
    }
}