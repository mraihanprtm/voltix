// AppModule.kt
package com.example.voltix.di

import android.content.Context
import androidx.room.Room
import com.example.voltix.data.database.AppDatabase
import com.example.voltix.data.dao.*
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.repository.*
import com.example.voltix.domain.LampRecommendationCalculator
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

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voltix_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()

    /** DAOs */
    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao =
        db.userDao()

    @Provides @Singleton
    fun providePerangkatDao(db: AppDatabase): PerangkatDAO =
        db.perangkatDao()

    @Provides @Singleton
    fun provideRuanganDao(db: AppDatabase): RuanganDAO =
        db.ruanganDao()

    @Provides @Singleton
    fun provideRuanganPerangkatCrossRefDao(db: AppDatabase): RuanganPerangkatCrossRefDAO =
        db.ruanganPerangkatCrossRefDao()

    @Provides @Singleton
    fun provideSimulationDao(db: AppDatabase): SimulationDAO =
        db.simulationDao()

    @Provides @Singleton
    fun provideUserPerangkatCrossRefDao(db: AppDatabase): UserPerangkatCrossRefDao =
        db.userPerangkatCrossRefDao()

    @Provides @Singleton
    fun provideRekomendasiDao(db: AppDatabase): RekomendasiDao =
        db.rekomendasiDao()

    /** Repositories */
    @Provides @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        userPerangkatCrossRefDao: UserPerangkatCrossRefDao,
        auth: FirebaseAuth
    ): UserRepository = UserRepository(userDao, userPerangkatCrossRefDao, auth)

    @Provides @Singleton
    fun providePerangkatRepository(
        perangkatDao: PerangkatDAO,
        ruanganDao: RuanganDAO,
        crossRefDao: RuanganPerangkatCrossRefDAO
    ): RuanganAndPerangkatRepository =
        RuanganAndPerangkatRepository(perangkatDao, ruanganDao, crossRefDao)

    @Provides @Singleton
    fun provideRuanganRepository(
        ruanganDao: RuanganDAO
    ): RuanganRepository = RuanganRepository(ruanganDao)

    @Provides @Singleton
    fun provideRekomendasiRepository(
        rekomDao: RekomendasiDao
    ): RekomendasiRepository = RekomendasiRepository(rekomDao)

    /** Calculator */
    // Option 1: Provide via module
    @Provides
    fun provideLampRecommendationCalculator(): LampRecommendationCalculator =
        LampRecommendationCalculator()

    // Option 2: (instead of above) annotate LampRecommendationCalculator with @Inject constructor()
    // and remove this provider method.

//    /** Remote / Auth */
    @Provides @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ): AuthManager = AuthManager(context, userRepository)
}