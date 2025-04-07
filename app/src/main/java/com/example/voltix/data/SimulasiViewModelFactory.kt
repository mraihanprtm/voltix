package com.example.voltix.data

import SimulasiViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voltix.data.repository.SimulasiRepository

class SimulasiViewModelFactory(
    private val repository: SimulasiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimulasiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimulasiViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


