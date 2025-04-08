package com.example.voltix.di

import android.content.Context
import com.example.voltix.data.dao.PerangkatDao
import com.example.voltix.data.dao.SimulasiPerangkatDao
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.database.AppDatabase
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.repository.SearchRepository
import com.example.voltix.data.repository.SimulasiRepository
import com.example.voltix.data.repository.UserRepository
import com.example.voltix.repository.PerangkatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun providePerangkatDao(appDatabase: AppDatabase): PerangkatDao {
        return appDatabase.perangkatDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, auth: FirebaseAuth): UserRepository {
        return UserRepository(userDao, auth)
    }

    @Provides
    @Singleton
    fun providePerangkatRepository(perangkatDao: PerangkatDao): PerangkatRepository {
        return PerangkatRepository(perangkatDao)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(): SearchRepository {
        return SearchRepository()
    }

    @Provides
    @Singleton
    fun provideSimulasiPerangkatDao(appDatabase: AppDatabase): SimulasiPerangkatDao {
        return appDatabase.simulasiDao()
    }

    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ): AuthManager {
        return AuthManager(context, userRepository)
    }


    @Provides
    @Singleton
    fun provideSimulasiRepository(
        dao: SimulasiPerangkatDao,
        @ApplicationContext context: Context
    ): SimulasiRepository {
        return SimulasiRepository(dao, context)
    }
}
