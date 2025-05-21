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

enum class TimeRange {
    DAILY, MONTHLY, YEARLY
}

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

    private var batasDayaPengguna: Int = 2200 // Fallback value (Watts)

    private val _totalDaya = MutableStateFlow(0.0)
    val totalDaya: StateFlow<Double> = _totalDaya.asStateFlow()

    private val _totalKonsumsi = MutableStateFlow(0.0)
    val totalKonsumsi: StateFlow<Double> = _totalKonsumsi.asStateFlow()

    private val _biayaListrik = MutableStateFlow(0.0)
    val biayaListrik: StateFlow<Double> = _biayaListrik.asStateFlow()

    private val _timeRange = MutableStateFlow(TimeRange.DAILY)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    private var hargaPerKWh: Double = 1444.70 // IDR per kWh, default PLN R-1 tariff

    init {
        Log.d("SimulasiBebasViewModel", "ViewModel initialized")
        loadBatasDayaPengguna()
    }

    // New methods
    fun updateSimulationName(simulationId: Int, newName: String) {
        viewModelScope.launch {
            try {
                Log.d("SimulasiBebasViewModel", "Updating simulation $simulationId to name: $newName")
                repository.updateSimulationName(simulationId, newName)
                loadAllSimulations() // Refresh simulation list
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error updating simulation name: ${e.message}")
            }
        }
    }

    fun deleteSimulation(simulationId: Int) {
        viewModelScope.launch {
            try {
                Log.d("SimulasiBebasViewModel", "Deleting simulation $simulationId")
                repository.deleteSimulation(simulationId)
                loadAllSimulations() // Refresh simulation list
                // Clear devices if the deleted simulation is active
                if (_devices.value?.any { it.simulationId == simulationId } == true) {
                    _devices.postValue(emptyList())
                    _totalDaya.value = 0.0
                    _biayaListrik.value = 0.0
                    _melebihiDaya.value = false
                }
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error deleting simulation: ${e.message}")
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        updateMelebihiDaya(_devices.value ?: emptyList())
        Log.d("SimulasiBebasViewModel", "Time range set to: $range")
    }

    private fun loadBatasDayaPengguna() {
        viewModelScope.launch {
            try {
                val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                if (firebaseUid != null) {
                    val currentUser = userRepository.getUserByUid(firebaseUid)
                    if (currentUser != null) {
                        batasDayaPengguna = currentUser.jenisListrik
                        val besaranDaya = repository.getTotalPower().toDouble()
                        val tarifFromUser = userRepository.getUserTarif(currentUser.id, besaranDaya)
                        hargaPerKWh = tarifFromUser.toDouble()

                        Log.d("SimulasiBebasViewModel", "Tarif: $hargaPerKWh")
                    }
                }
                updateMelebihiDaya(_devices.value ?: emptyList())
            } catch (e: Exception) {
                Log.e("SimulasiBebasViewModel", "Error loading user data", e)
                updateMelebihiDaya(_devices.value ?: emptyList())
            }
        }
    }


    private fun updateMelebihiDaya(devices: List<SimulationDeviceEntity>) {
        var totalPower = 0.0 // Wh for daily, kWh for monthly/yearly
        val timeRange = _timeRange.value
        var totalDaya = 0.0

        devices.forEach { device ->
            val powerPerUnit = device.daya * device.jumlah // Watts
            val startHour = device.waktuNyala.toSecondOfDay() / 3600.0
            val endHour = device.waktuMati.toSecondOfDay() / 3600.0
            val hoursActive = if (endHour >= startHour) {
                endHour - startHour
            } else {
                (24.0 - startHour) + endHour
            }
            totalPower += powerPerUnit * hoursActive // Wh per day
            totalDaya += powerPerUnit
            _totalDaya.value = totalDaya
        }

        when (timeRange) {
            TimeRange.DAILY -> {
                // Already in Wh per day
            }
            TimeRange.MONTHLY -> {
                totalPower = totalPower * 30.42f
            }
            TimeRange.YEARLY -> {
                totalPower = totalPower * 365.25f
            }
        }
        _totalDaya.value = totalDaya
        _totalKonsumsi.value = totalPower / 1000f
        _biayaListrik.value = _totalKonsumsi.value * hargaPerKWh

        // Check if power exceeds limit (batasDayaPengguna in Watts for daily comparison)
        _melebihiDaya.value = when (timeRange) {
            TimeRange.DAILY -> _totalDaya.value > batasDayaPengguna
            TimeRange.MONTHLY -> _totalDaya.value > batasDayaPengguna
            TimeRange.YEARLY -> _totalDaya.value > batasDayaPengguna
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