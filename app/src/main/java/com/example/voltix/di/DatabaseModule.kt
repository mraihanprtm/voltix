package com.example.voltix.di

import android.content.Context
import androidx.room.Room
import com.example.voltix.data.AppDatabase
import com.example.voltix.data.dao.SimulasiPerangkatDao
import com.example.voltix.data.dao.PerangkatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            "opendatajabar_db"
        ).fallbackToDestructiveMigration() .build()

    }

    @Provides
    fun provideSimulasiDao(db: AppDatabase): SimulasiPerangkatDao = db.simulasiDao()

    @Provides
    fun providePerangkatdao(db: AppDatabase): PerangkatDao = db.perangkatDao()
}