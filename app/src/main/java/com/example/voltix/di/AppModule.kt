package com.example.voltix.di

import android.content.Context
import com.example.voltix.data.dao.UserDao
import com.example.voltix.data.database.AppDatabase
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.repository.SearchRepository
import com.example.voltix.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(): FirebaseAuth {
//        return Firebase.auth
//    }

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
    fun provideUserRepository(userDao: UserDao, auth: FirebaseAuth): UserRepository {
        return UserRepository(userDao, auth)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(): SearchRepository {
        return SearchRepository()
    }
}
