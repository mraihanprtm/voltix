package com.example.voltix.di // Pastikan package ini sesuai dengan lokasi file Anda

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Menyediakan dependensi selama lifecycle aplikasi (singleton)
object FirebaseModule {

    @Provides
    @Singleton // Membuat instance FirebaseAuth sebagai singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance() // Cara standar untuk mendapatkan instance FirebaseAuth
    }
}