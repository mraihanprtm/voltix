package com.example.voltix

import android.app.Application
import com.example.voltix.data.repository.SeederRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VoltixApplication : Application(){
    @Inject lateinit var seederRepository: SeederRepository

    override fun onCreate() {
        super.onCreate()
        println("Seeding Starting")
        CoroutineScope(Dispatchers.IO).launch {
            seederRepository.seedIfNeeded()
//            println("seeding...")
        }
    }
}