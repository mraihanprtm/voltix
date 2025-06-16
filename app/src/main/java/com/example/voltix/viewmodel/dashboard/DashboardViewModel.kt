package com.example.voltix.viewmodel.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.remote.dto.UserData
import com.example.voltix.data.remote.SyncManager
import com.example.voltix.data.repository.DashboardData
import com.example.voltix.data.repository.DashboardRepository
import com.example.voltix.data.repository.SimulationRepository
import com.example.voltix.data.repository.UserRepository
import com.example.voltix.viewmodel.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeRange {
    DAILY, MONTHLY, YEARLY
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager
) : ViewModel() {
    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData.asStateFlow()

    private val _timeRange = MutableStateFlow(TimeRange.DAILY)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    // Observasi currentUserProfile dari UserRepository
    val currentUserProfile: StateFlow<UserData?> = userRepository.currentUserProfile

    init {
        Log.d("DashboardViewModel", "Initializing...")
        viewModelScope.launch {
            syncManager.synchronize()
            currentUserProfile.collectLatest { userData ->
                if (userData != null) {
                    Log.d("DashboardViewModel", "User data available in Dashboard: ${userData.name}. Loading dashboard data.")
                    loadDashboardDataBasedOnUser(userData)
                } else {
                    Log.d("DashboardViewModel", "User data not available for dashboard. Clearing dashboard data.")
                    _dashboardData.value = null
                    // Jika user null, mungkin perlu memanggil fetch dari UserRepository sekali di sini
                    // untuk memastikan kita sudah mencoba mengambilnya jika AuthManager belum sempat set.
                    // userRepository.fetchAndSetCurrentUserProfileFromBackend()
                }
            }
        }
        // Pemicu awal untuk fetch user jika belum ada (misalnya saat app start dan user sudah login sebelumnya)
        // Ini bisa juga dilakukan di MainViewModel atau App startup.
        if (userRepository.currentUserProfile.value == null && userRepository.tokenManager.getApiToken() != null) {
            viewModelScope.launch { userRepository.fetchAndSetCurrentUserProfileFromBackend() }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        // Muat ulang data dashboard jika time range berubah DAN jika user sudah ada
        currentUserProfile.value?.let { user ->
            loadDashboardDataBasedOnUser(user)
        }
    }

    private fun loadDashboardDataBasedOnUser(user: UserData) {
        viewModelScope.launch {

            Log.d("DashboardViewModel", "Loading dashboard data for user ID (backend): ${user.id}, Name: ${user.name}")
            // Asumsi: DashboardRepository.getDashboardData() SEKARANG akan diubah
            // untuk menerima userId atau menggunakan konteks user dari API call jika data dashboard dari backend.
            // Untuk saat ini, jika DashboardRepository masih menggunakan DAO lokal tanpa filter user,
            // maka parameter 'user' di sini hanya sebagai penanda bahwa user sudah login.
            // Jika DashboardRepository diubah untuk mengambil data spesifik user:
            // dashboardRepository.getDashboardDataForUser(userId = user.id).collect { data -> ... }
            // Kita akan tetap panggil getDashboardData() yang lama untuk sekarang, tapi ini adalah titik refactor berikutnya.

            // PANGGILAN KE REPOSITORY (nantinya perlu user context)
            dashboardRepository.getDashboardDataForUser(user).collect { data ->
                // Logika penskalaan berdasarkan timeRange Anda yang sudah ada
                val scaledData = when (_timeRange.value) {
                    TimeRange.DAILY -> data
                    TimeRange.MONTHLY -> data.copy(
                        totalPower = data.totalPower * 30.42f, // Ini adalah asumsi rata-rata hari per bulan
                        totalCost = data.totalCost * 30.42f,
                        hourlyPower = data.hourlyPower.map { it * 30.42f } // Ini mungkin tidak akurat untuk hourly
                    )
                    TimeRange.YEARLY -> data.copy(
                        totalPower = data.totalPower * 365.25f,
                        totalCost = data.totalCost * 365.25f,
                        hourlyPower = data.hourlyPower.map { it * 365.25f } // Ini juga mungkin tidak akurat
                    )
                }
                _dashboardData.value = scaledData
                Log.d("DashboardViewModel", "Dashboard data loaded and scaled: $scaledData")
            }
        }
    }
}