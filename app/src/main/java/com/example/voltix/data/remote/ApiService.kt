package com.example.voltix.data.remote

import com.example.voltix.data.remote.response.ApiResponse
import com.example.voltix.data.entity.LampuEntity
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.remote.response.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Headers

// ApiService.kt
interface ApiService {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/v1/sync")
    suspend fun syncData(
        @Body request: SyncRequest
    ): Response<ApiResponse<SyncResponse>>
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/v1/push-changes")
    suspend fun pushChanges(
        @Body changes: LocalChanges
    ): Response<ApiResponse<Unit>>
}

data class SyncRequest(
    val lastSyncTimestamp: Long,
    val deviceId: String
)

data class LocalChanges(
    val perangkat: List<PerangkatEntity>,
    val ruangan: List<RuanganEntity>,
    val lampu: List<LampuEntity>,
    val ruanganPerangkat: List<RuanganPerangkatCrossRef>,
    val deletedIds: DeletedIds
)

data class DeletedIds(
    val perangkatIds: List<Int>,
    val ruanganIds: List<Int>,
    val lampuIds: List<Int>
)