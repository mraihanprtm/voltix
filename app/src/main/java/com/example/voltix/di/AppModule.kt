package com.example.voltix.di

import android.content.Context
import androidx.room.Room
import com.example.voltix.data.database.AppDatabase
import com.example.voltix.data.dao.*
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.repository.*
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voltix_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun providePerangkatDao(database: AppDatabase): PerangkatDAO {
        return database.perangkatDao()
    }

    @Provides
    @Singleton
    fun provideRuanganDao(database: AppDatabase): RuanganDAO {
        return database.ruanganDao()
    }

    // Tambahkan provider untuk RuanganPerangkatCrossRefDAO
    @Provides
    @Singleton
    fun provideRuanganPerangkatCrossRefDao(database: AppDatabase): RuanganPerangkatCrossRefDAO {
        return database.ruanganPerangkatCrossRefDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        userPerangkatCrossRefDao: UserPerangkatCrossRefDao,
        @ApplicationContext context: Context,  // Jika diperlukan
        auth: FirebaseAuth  // Inject FirebaseAuth
    ): UserRepository {
        return UserRepository(userDao, userPerangkatCrossRefDao, auth)
    }

    @Provides
    @Singleton
    fun providePerangkatRepository(
        perangkatDao: PerangkatDAO,
        ruanganDao: RuanganDAO,
        ruanganPerangkatCrossRefDao: RuanganPerangkatCrossRefDAO  // Tambahkan parameter ini
    ): RuanganAndPerangkatRepository {
        return RuanganAndPerangkatRepository(perangkatDao, ruanganDao, ruanganPerangkatCrossRefDao)
    }

    @Provides
    @Singleton
    fun provideRuanganRepository(
        ruanganDao: RuanganDAO
    ): RuanganRepository {
        return RuanganRepository(ruanganDao)
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
    fun provideUserPerangkatCrossRefDao(database: AppDatabase): UserPerangkatCrossRefDao {
        return database.userPerangkatCrossRefDao()
    }
}