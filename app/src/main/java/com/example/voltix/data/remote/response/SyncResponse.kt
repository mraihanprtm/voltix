package com.example.voltix.data.remote.response

import com.example.voltix.data.entity.LampuEntity
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef

data class SyncResponse(
    val lastSyncTimestamp: Long,
    val perangkat: List<PerangkatEntity>,
    val ruangan: List<RuanganEntity>,
    val lampu: List<LampuEntity>,
    val ruanganPerangkat: List<RuanganPerangkatCrossRef>
)