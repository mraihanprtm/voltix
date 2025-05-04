package com.example.voltix.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.SimulationDeviceEntity
import com.example.voltix.data.entity.SimulationEntity
import com.example.voltix.data.entity.jenis
import com.example.voltix.data.repository.SimulationRepository
import com.example.voltix.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SimulasiBebasViewModel @Inject constructor(
    private val repository: SimulationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _simulationId = MutableLiveData<Int?>(null)
    val simulationId: LiveData<Int?> = _simulationId

    private val _devices = MutableLiveData<List<SimulationDeviceEntity>>(emptyList())
    val devices: LiveData<List<SimulationDeviceEntity>> = _devices

    private val _simulationList = MutableLiveData<List<SimulationEntity>>(emptyList())
    val simulationList: LiveData<List<SimulationEntity>> = _simulationList

    val ruanganList: LiveData<List<RuanganEntity>> = repository.getAllRuanganLive()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _melebihiDaya = MutableStateFlow(false)
    val melebihiDaya: StateFlow<Boolean> = _melebihiDaya.asStateFlow()
    private var batasDayaPengguna: Int = 2200 // Fallback value

    private val _totalDaya = MutableStateFlow(0.0)
    val totalDaya: StateFlow<Double> = _totalDaya.asStateFlow()

    private val _biayaListrik = MutableStateFlow(0.0)
    val biayaListrik: StateFlow<Double> = _biayaListrik.asStateFlow()

    private val _jenisListrik = MutableStateFlow(0)
    val jenisListrik: StateFlow<Int> = _jenisListrik


    init {
        Log.d("SimulasiBebasViewModel", "ViewModel initialized")
        loadBatasDayaPengguna()
    }

    private fun updateMelebihiDaya(devices: List<SimulationDeviceEntity>) {
        val totalPower = devices.sumOf { it.daya * it.jumlah.toDouble() } // dalam watt
        _totalDaya.value = totalPower

        val totalHours = devices.sumOf {
            val duration = java.time.Duration.between(it.waktuNyala, it.waktuMati).toMinutes().coerceAtLeast(0).toDouble() / 60.0
            duration * it.jumlah
        }

        // Konversi watt ke kWh
        val totalEnergyKWh = (totalPower / 1000.0) * totalHours
        val hargaPerKWh = jenisListrik.value.toDouble() // Misalnya tarif listrik per kWh (PLN R-1/Tarif Dasar Listrik 2023)
        _biayaListrik.value = totalEnergyKWh * hargaPerKWh

        _melebihiDaya.value = totalPower > batasDayaPengguna

        Log.d("SimulasiBebasViewModel", "Total Power: $totalPower W, Total Energy: $totalEnergyKWh kWh, Biaya: ${_biayaListrik.value}, Melebihi: ${_melebihiDaya.value}")
    }


    private fun loadBatasDayaPengguna() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d("SimulasiBebasViewModel", "Current User ID: $userId")
                if (userId != null) {
                    val currentUser = userRepository.getUserByUid(userId)
                    Log.d("SimulasiBebasViewModel", "Current User Data: $currentUser")
                    if (currentUser != null) {
                        batasDayaPengguna = currentUser.jenisListrik
                        _jenisListrik.value = currentUser.jenisListrik
                        Log.d("SimulasiBebasViewModel", "Batas Daya set to: $batasDayaPengguna")
                    } else {
                        Log.w("SimulasiBebasViewModel", "User data not found for ID: $userId")
                        batasDayaPengguna = 2200 // Fallback
                    }
                } else {
                    Log.w("SimulasiBebasViewModel", "No user is currently logged in")
                    batasDayaPengguna = 2200 // Fallback
                }
                updateMelebihiDaya(_devices.value ?: emptyList())
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error loading user data", e)
                batasDayaPengguna = 2200 // Fallback
                updateMelebihiDaya(_devices.value ?: emptyList())
            }
        }
    }

    fun startSimulation(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val id = repository.createSimulation(name)
                _simulationId.value = id
                _devices.value = repository.getSimulationWithDevices(id).firstOrNull()?.devices ?: emptyList()
                updateMelebihiDaya(_devices.value ?: emptyList())
                loadAllSimulations()
                Log.d("SimulasiBebasViewModel", "Started simulation $id with devices: ${_devices.value}")
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error starting simulation", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSimulation(simulationId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _simulationId.value = simulationId
                _devices.value = repository.getSimulationWithDevices(simulationId).firstOrNull()?.devices ?: emptyList()
                updateMelebihiDaya(_devices.value ?: emptyList())
                Log.d("SimulasiBebasViewModel", "Loaded simulation $simulationId with devices: ${_devices.value}")
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error loading simulation", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllSimulations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val simulations = repository.getAllSimulations()
                _simulationList.value = simulations
                Log.d("SimulasiBebasViewModel", "Loaded ${simulations.size} simulations: $simulations")
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error loading simulations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertDevice(nama: String, daya: Int, waktuNyala: LocalTime, waktuMati: LocalTime) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _simulationId.value?.let { simId ->
                    val device = SimulationDeviceEntity(
                        simulationId = simId,
                        nama = nama,
                        daya = daya,
                        jumlah = 1,
                        jenis = jenis.Lainnya,
                        waktuNyala = waktuNyala,
                        waktuMati = waktuMati
                    )
                    repository.insertSimulationDevice(simId, device)
                    _devices.value = repository.getSimulationWithDevices(simId).firstOrNull()?.devices ?: emptyList()
                    updateMelebihiDaya(_devices.value ?: emptyList())
                    Log.d("SimulasiBebasViewModel", "Inserted device: $nama for simId $simId")
                } ?: Log.w("SimulasiBebasViewModel", "No simulation ID set for insertDevice")
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error inserting device", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDevice(
        device: SimulationDeviceEntity,
        nama: String,
        daya: Int,
        waktuNyala: LocalTime,
        waktuMati: LocalTime
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedDevice = device.copy(
                    nama = nama,
                    daya = daya,
                    waktuNyala = waktuNyala,
                    waktuMati = waktuMati
                )
                repository.updateSimulationDevice(updatedDevice)
                _simulationId.value?.let { simId ->
                    _devices.value = repository.getSimulationWithDevices(simId).firstOrNull()?.devices ?: emptyList()
                    updateMelebihiDaya(_devices.value ?: emptyList())
                    Log.d("SimulasiBebasViewModel", "Updated device ${device.deviceId} for simId $simId")
                }
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error updating device", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDevice(deviceId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _simulationId.value?.let { simId ->
                    repository.deleteSimulationDevice(simId, deviceId)
                    _devices.value = repository.getSimulationWithDevices(simId).firstOrNull()?.devices ?: emptyList()
                    updateMelebihiDaya(_devices.value ?: emptyList())
                    Log.d("SimulasiBebasViewModel", "Deleted device $deviceId for simId $simId")
                }
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error deleting device", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRoomDevices(ruanganId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _simulationId.value?.let { simId ->
                    val roomDevices = repository.getPerangkatWithWaktuByRuanganId(ruanganId)
                    val existingDevices = _devices.value ?: emptyList()

                    Log.d("SimulasiBebasViewModel", "Room $ruanganId has ${roomDevices.size} devices: $roomDevices")

                    roomDevices.forEach { device ->
                        val isDuplicate = existingDevices.any { existing ->
                            existing.nama == device.nama &&
                                    existing.waktuNyala == device.waktuNyala &&
                                    existing.waktuMati == device.waktuMati
                        }

                        if (!isDuplicate) {
                            val newDevice = SimulationDeviceEntity(
                                simulationId = simId,
                                nama = device.nama,
                                daya = device.daya,
                                jumlah = device.jumlah,
                                jenis = device.jenis,
                                waktuNyala = device.waktuNyala,
                                waktuMati = device.waktuMati
                            )
                            repository.insertSimulationDevice(simId, newDevice)
                        } else {
                            Log.d("SimulasiBebasViewModel", "Skipped duplicate device: ${device.nama}")
                        }
                    }

                    _devices.value = repository.getSimulationWithDevices(simId).firstOrNull()?.devices ?: emptyList()
                    updateMelebihiDaya(_devices.value ?: emptyList())
                    Log.d("SimulasiBebasViewModel", "Loaded filtered room devices for simId $simId: ${_devices.value}")
                } ?: Log.w("SimulasiBebasViewModel", "No simulation ID set for loadRoomDevices")
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error loading room devices", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}