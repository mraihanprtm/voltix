package com.example.voltix.di

import android.content.Context
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.repository.SearchRepository
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
    fun provideAuthManager(
        @ApplicationContext context: Context
    ): AuthManager {
        return AuthManager(context)
    }

    @Singleton
    fun provideSearchRepository(): SearchRepository {
        return SearchRepository()
    }
}
