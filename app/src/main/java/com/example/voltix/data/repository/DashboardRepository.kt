package com.example.voltix.data.repository

import com.example.voltix.data.dao.PerangkatDAO
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.entity.RuanganWithPerangkat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject



class DashboardRepository @Inject constructor(
    private val perangkatDao: PerangkatDAO,
) {

}