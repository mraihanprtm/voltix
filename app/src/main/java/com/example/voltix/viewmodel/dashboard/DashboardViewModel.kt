package com.example.voltix.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.repository.DashboardData
import com.example.voltix.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeRange {
    DAILY, MONTHLY, YEARLY
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData.asStateFlow()

    private val _timeRange = MutableStateFlow(TimeRange.DAILY)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    init {
        loadDashboardData()
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            repository.getDashboardData().collect { data ->
                val scaledData = when (_timeRange.value) {
                    TimeRange.DAILY -> data
                    TimeRange.MONTHLY -> data.copy(
                        totalPower = data.totalPower * 30.42f,
                        totalCost = data.totalCost * 30.42f,
                        hourlyPower = data.hourlyPower.map { it * 30.42f}
                    )
                    TimeRange.YEARLY -> data.copy(
                        totalPower = data.totalPower * 365.25f, // Wh to kWh
                        totalCost = data.totalCost * 365.25f,
                        hourlyPower = data.hourlyPower.map { it * 365.25f}
                    )
                }
                _dashboardData.value = scaledData
            }
        }
    }
}