// AppModule.kt
package com.example.voltix.di


import com.example.voltix.data.util.AuthInterceptor
import android.content.Context
import androidx.room.Room // Pastikan import Room ada jika membuat AppDatabase
import com.example.voltix.data.database.AppDatabase // Pastikan import AppDatabase ada
import com.example.voltix.data.dao.* // Import semua DAO Anda
import com.example.voltix.data.remote.ApiService
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.remote.api.AuthApiService
import com.example.voltix.data.remote.SyncManager
import com.example.voltix.data.repository.*
import com.example.voltix.data.util.TokenManager
//import AuthInterceptor
import com.example.voltix.domain.LampRecommendationCalculator
import com.example.voltix.util.LocalTimeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {



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

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.10.130.70:8000/") // Replace with your actual base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun provideSyncManger(
        apiService: ApiService,
        database: AppDatabase,
        @ApplicationContext context: Context
    ): SyncManager {
        return SyncManager(apiService, database, context)
    }
}