package com.example.voltix.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.SimulationWithDevices
import com.example.voltix.data.repository.SimulationRepository
import com.example.voltix.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

data class ComparisonResult(
    val simulationName: String,
    val powerUsage: Double,
    val cost: Double,
    val powerSavings: Double, // Difference from the best (lowest) power usage
    val costSavings: Double   // Difference from the best (lowest) cost
)

@HiltViewModel
class SimulationComparisonViewModel @Inject constructor(
    private val repository: SimulationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _simulations = MutableLiveData<List<SimulationWithDevices>>(emptyList())
    val simulations: LiveData<List<SimulationWithDevices>> = _simulations

    private val _comparisonResults = MutableLiveData<List<ComparisonResult>>(emptyList())
    val comparisonResults: LiveData<List<ComparisonResult>> = _comparisonResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var hargaPerKWh: Double = 1444.70 // Default fallback value (IDR per kWh)

    init {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d("SimulationComparisonViewModel", "Current User ID: $userId")

                if (userId != null) {
                    val currentUser = userRepository.getUserByUid(userId)
                    Log.d("SimulationComparisonViewModel", "Current User Data: $currentUser")

                    if (currentUser != null) {
                        updateHargaPerKWh(currentUser.id, 0.0) // Initial total power is 0
                        Log.d("SimulationComparisonViewModel", "Harga per kWh set to: $hargaPerKWh")
                    } else {
                        Log.w("SimulationComparisonViewModel", "User data not found for ID: $userId")
                    }
                } else {
                    Log.w("SimulationComparisonViewModel", "No user is currently logged in")
                }
            } catch (e: Exception) {
                Log.e("SimulationComparisonViewModel", "Error loading user data", e)
            }
        }
    }

    private suspend fun updateHargaPerKWh(userId: Int, besaranDaya: Double) {
        try {
            hargaPerKWh = userRepository.getUserTarif(userId, besaranDaya).toDouble()
            Log.d("SimulationComparisonViewModel", "Updated harga per kWh: $hargaPerKWh for besaranDaya: $besaranDaya")
        } catch (e: Exception) {
            Log.e("SimulationComparisonViewModel", "Error updating harga per kWh", e)
            hargaPerKWh = 1444.70 // Fallback to default
        }
    }

    fun loadAllSimulations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val simulationList = repository.getAllSimulationsWithDevices()
                _simulations.value = simulationList
                Log.d("SimulationComparisonViewModel", "Loaded ${simulationList.size} simulations")
            } catch (e: Exception) {
                Log.e("SimulationComparisonViewModel", "Error loading simulations: ${e.message}")
                _simulations.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun compareSimulations(selectedSimulations: List<SimulationWithDevices>) {
        viewModelScope.launch {
            try {
                // Calculate total power for tariff determination
                val totalPower = selectedSimulations.sumOf { simulation ->
                    simulation.devices.sumOf { device ->
                        (device.daya * device.jumlah).toDouble()
                    }
                }

                // Update hargaPerKWh based on total power
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val currentUser = userRepository.getUserByUid(userId)
                    if (currentUser != null) {
                        updateHargaPerKWh(currentUser.id, totalPower)
                    }
                }

                val results = selectedSimulations.map { simulation ->
                    val powerUsage = simulation.calculatePowerUsage()
                    val cost = simulation.calculateCost()
                    ComparisonResult(
                        simulationName = simulation.simulation.name,
                        powerUsage = powerUsage,
                        cost = cost,
                        powerSavings = 0.0, // Will be updated below
                        costSavings = 0.0   // Will be updated below
                    )
                }

                // Find the best (lowest) power usage and cost
                val minPowerUsage = results.minOfOrNull { it.powerUsage } ?: 0.0
                val minCost = results.minOfOrNull { it.cost } ?: 0.0

                // Calculate savings relative to the best
                val updatedResults = results.map { result ->
                    result.copy(
                        powerSavings = result.powerUsage - minPowerUsage,
                        costSavings = result.cost - minCost
                    )
                }

                _comparisonResults.value = updatedResults
                Log.d("SimulationComparisonViewModel", "Compared ${selectedSimulations.size} simulations with hargaPerKWh: $hargaPerKWh")
            } catch (e: Exception) {
                Log.e("SimulationComparisonViewModel", "Error comparing simulations: ${e.message}")
                _comparisonResults.value = emptyList()
            }
        }
    }

    private fun SimulationWithDevices.calculatePowerUsage(): Double {
        return devices.sumOf { device ->
            val durationHours = if (device.waktuMati.isAfter(device.waktuNyala)) {
                Duration.between(device.waktuNyala, device.waktuMati).toHours().toDouble()
            } else {
                // Handle overnight usage
                Duration.between(device.waktuNyala, LocalTime.MAX).toHours().toDouble() +
                        Duration.between(LocalTime.MIN, device.waktuMati).toHours().toDouble()
            }
            (device.daya * device.jumlah * durationHours) / 1000.0 // Convert Wh to kWh
        }
    }

    private fun SimulationWithDevices.calculateCost(): Double {
        return calculatePowerUsage() * hargaPerKWh
    }
}