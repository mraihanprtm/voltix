package com.example.voltix.data.remote

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.voltix.data.database.AppDatabase
import java.util.UUID
import javax.inject.Inject
import androidx.core.content.edit
import com.example.voltix.data.remote.response.SyncResponse

class SyncManager @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    suspend fun synchronize() {
        try {
            val lastSync = prefs.getLong("last_sync", 0)
            val deviceId = getDeviceId()
            Log.d("SyncManager", "Starting sync - LastSync: $lastSync, DeviceID: $deviceId")

            // Get changes from server
            Log.d("SyncManager", "Fetching changes from server...")
            val response = apiService.syncData(SyncRequest(lastSync, deviceId))

            if (response.isSuccessful) {
                Log.d("SyncManager", "Server response successful: ${response.body()}")
                response.body()?.data?.let { syncResponse ->
                    database.withTransaction {
                        // Update local database with server changes
                        Log.d("SyncManager", "Updating local database with server changes...")
                        Log.d("SyncManager", "Server data - Perangkat: ${syncResponse.perangkat.size}, " +
                                "Ruangan: ${syncResponse.ruangan.size}, " +
                                "Lampu: ${syncResponse.lampu.size}, " +
                                "RuanganPerangkat: ${syncResponse.ruanganPerangkat.size}")

                        updateLocalDatabase(syncResponse)

                        // Push local changes to server
                        Log.d("SyncManager", "Getting local changes since $lastSync...")
                        val localChanges = getLocalChanges(lastSync)
                        Log.d("SyncManager", "Local changes - Perangkat: ${localChanges.perangkat.size}, " +
                                "Ruangan: ${localChanges.ruangan.size}, " +
                                "Lampu: ${localChanges.lampu.size}, " +
                                "RuanganPerangkat: ${localChanges.ruanganPerangkat.size}, " +
                                "DeletedIds: ${localChanges.deletedIds}")

                        Log.d("SyncManager", "Pushing local changes to server...")
                        val pushResponse = apiService.pushChanges(localChanges)
                        Log.d("SyncManager", "Push response: ${pushResponse.body()}")

                        // Update last sync timestamp
                        val newTimestamp = System.currentTimeMillis()
                        prefs.edit { putLong("last_sync", newTimestamp) }
                        Log.d("SyncManager", "Updated last sync timestamp to $newTimestamp")
                    }
                } ?: Log.e("SyncManager", "Response body or data is null")
            } else {
                Log.e("SyncManager", "Server response unsuccessful: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Sync failed with exception", e)
        }
    }

    private suspend fun updateLocalDatabase(syncResponse: SyncResponse) {
        with(database) {
            perangkatDao().insertAllPerangkat(syncResponse.perangkat)
            ruanganDao().insertAll(syncResponse.ruangan)
            perangkatDao().insertAllLampu(syncResponse.lampu)
            ruanganPerangkatCrossRefDao().insertAll(syncResponse.ruanganPerangkat)
        }
    }

    private suspend fun getLocalChanges(since: Long): LocalChanges {
        return with(database) {
            LocalChanges(
                perangkat = perangkatDao().getChangedSince(since),
                ruangan = ruanganDao().getChangedSince(since),
                lampu = perangkatDao().getLampuChangedSince(since),
                ruanganPerangkat = ruanganPerangkatCrossRefDao().getChangedSince(since),
                deletedIds = getDeletedIds()
            )
        }
    }

    private fun getDeviceId(): String {
        val deviceId = prefs.getString("device_id", null)
        if (deviceId != null) {
            return deviceId
        }

        // Generate new device ID if not exists
        val newDeviceId = UUID.randomUUID().toString()
        prefs.edit() { putString("device_id", newDeviceId) }
        return newDeviceId
    }

    private suspend fun getDeletedIds(): DeletedIds {
        return with(database) {
            DeletedIds(
                perangkatIds = perangkatDao().getDeleted().map { it.id },
                ruanganIds = ruanganDao().getDeleted().map { it.id },
                lampuIds = perangkatDao().getLampuDeleted().map { it.id },
            )
        }
    }
}