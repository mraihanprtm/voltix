// AppModule.kt
package com.example.voltix.di

import android.content.Context
import androidx.room.Room // Pastikan import Room ada jika membuat AppDatabase
import com.example.voltix.data.database.AppDatabase // Pastikan import AppDatabase ada
import com.example.voltix.data.dao.* // Import semua DAO Anda
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.remote.api.AuthApiService
import com.example.voltix.data.repository.*
import com.example.voltix.data.util.TokenManager
import com.example.voltix.domain.LampRecommendationCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ... (provider untuk OkHttpClient)
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // ... (provider untuk Retrofit)
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.10.192.187:8000/") // PASTIKAN BASE URL INI BENAR
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    // INI FUNGSI YANG PENTING UNTUK ERROR ANDA:
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voltix_database"
        )
            .fallbackToDestructiveMigration(false) // Pertimbangkan strategi migrasi Anda
            .build()

    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun providePerangkatDao(db: AppDatabase): PerangkatDAO = db.perangkatDao()

    @Provides @Singleton
    fun provideRuanganDao(db: AppDatabase): RuanganDAO = db.ruanganDao()

    @Provides @Singleton
    fun provideRuanganPerangkatCrossRefDao(db: AppDatabase): RuanganPerangkatCrossRefDAO =
        db.ruanganPerangkatCrossRefDao()

    @Provides @Singleton
    fun provideSimulationDao(db: AppDatabase): SimulationDAO = db.simulationDao()

    @Provides @Singleton
    fun provideUserPerangkatCrossRefDao(db: AppDatabase): UserPerangkatCrossRefDao =
        db.userPerangkatCrossRefDao()

    @Provides @Singleton
    fun provideRekomendasiDao(db: AppDatabase): RekomendasiDao = db.rekomendasiDao()

    @Provides @Singleton // Pastikan @Singleton jika GolonganListrikDao juga singleton
    fun provideGolonganListrikDao(database: AppDatabase): GolonganListrikDao { // Saya tambahkan @Singleton
        return database.golonganListrikDao()
    }
    // --- Akhir bagian Room & DAO ---


    /** Repositories */
    // Pastikan constructor UserRepository sekarang sudah benar dan dependensinya di-provide
    @Provides
    @Singleton
    fun provideUserRepository(
        authApiService: AuthApiService,         // Sesuai parameter ke-1 di constructor UserRepository
        tokenManager: TokenManager,           // Sesuai parameter ke-2
        firebaseAuth: FirebaseAuth,           // Sesuai parameter ke-3
        userDao: UserDao,                     // Sesuai parameter ke-4
        userPerangkatCrossRefDao: UserPerangkatCrossRefDao, // Sesuai parameter ke-5
        golonganListrikDao: GolonganListrikDao  // Sesuai parameter ke-6
    ): UserRepository {
        // Panggil constructor UserRepository dengan urutan yang BENAR
        return UserRepository(
            authApiService,
            tokenManager,
            firebaseAuth, // Pastikan nama variabel ini (firebaseAuth) adalah yang di-inject Hilt
            userDao,
            userPerangkatCrossRefDao,
            golonganListrikDao
        )
    }

    // ... (Provider untuk Repository lain yang masih pakai DAO biarkan dulu) ...
    // Contoh:
    @Provides @Singleton
    fun provideRuanganAndPerangkatRepository( // Nama fungsi provider ini sebelumnya providePerangkatRepository
        perangkatDao: PerangkatDAO,
        ruanganDao: RuanganDAO,
        crossRefDao: RuanganPerangkatCrossRefDAO
    ): RuanganAndPerangkatRepository =
        RuanganAndPerangkatRepository(perangkatDao, ruanganDao, crossRefDao)

    // ... provider lain ...

    /** Calculator */
    @Provides
    fun provideLampRecommendationCalculator(): LampRecommendationCalculator =
        LampRecommendationCalculator()

    /** Token Manager */
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    /** Remote / Auth */
    @Provides @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        authApiService: AuthApiService,
        userRepository: UserRepository, // <-- TAMBAHKAN UserRepository
        tokenManager: TokenManager
    ): AuthManager {
        return AuthManager(context, firebaseAuth, authApiService, userRepository, tokenManager)
    }
}