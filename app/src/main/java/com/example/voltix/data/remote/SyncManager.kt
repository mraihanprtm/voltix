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
            val initialLastSync = prefs.getLong("last_sync", 0)
            Log.d("SyncManager", "Starting sync cycle - InitialLastSync: $initialLastSync")

            // ===================================
            // PHASE 1: PULL CHANGES FROM SERVER
            // ===================================
            Log.d("SyncManager", "PHASE 1: Fetching changes from server...")
            val pullResponse = apiService.syncData(SyncRequest(initialLastSync, getDeviceId()))

            if (!pullResponse.isSuccessful) {
                Log.e("SyncManager", "Pull from server failed: ${pullResponse.code()} - ${pullResponse.message()}")
                return // Exit if pull fails
            }

            val syncResponse = pullResponse.body()?.data
            if (syncResponse == null) {
                Log.e("SyncManager", "Pull response body or data is null")
                return // Exit if data is missing
            }

            // Save server changes to the local database
            updateLocalDatabase(syncResponse)

            // **CRITICAL STEP**: Update the sync timestamp to the value from the server BEFORE pushing.
            val newLastSyncFromServer = syncResponse.lastSyncTimestamp
            prefs.edit { putLong("last_sync", newLastSyncFromServer) }
            Log.d("SyncManager", "PHASE 1: Pull complete. Updated last_sync to $newLastSyncFromServer")


            // ==================================
            // PHASE 2: PUSH LOCAL CHANGES
            // ==================================
//            Log.d("SyncManager", "PHASE 2: Getting local changes made since $initialLastSync...")
//            // We still get changes since the *initial* timestamp of this cycle
//            val localChanges = getLocalChanges(initialLastSync)
//
//            // A helper function to check if there are any changes to push
//            if (localChanges.isEmpty()) {
//                Log.d("SyncManager", "PHASE 2: No local changes to push. Sync cycle complete.")
//                return
//            }
//
//            Log.d("SyncManager", "PHASE 2: Pushing local changes to server...")
//            val pushResponse = apiService.pushChanges(localChanges)
//
//            if (pushResponse.isSuccessful) {
//                Log.d("SyncManager", "PHASE 2: Push successful. Sync cycle complete.")
//            } else {
//                // If push fails, we throw an exception. The timestamp from the PULL phase is already saved,
//                // which is okay. The local changes that failed to push will be picked up on the next sync.
//                Log.e("SyncManager", "PHASE 2: Push failed: ${pushResponse.code()} - ${pushResponse.message()}")
//                throw java.io.IOException("Push to server failed with code ${pushResponse.code()}")
//            }

        } catch (e: Exception) {
            Log.e("SyncManager", "Sync cycle failed with exception", e)
        }
    }

    // You'll need to add this helper extension function inside your SyncManager class
    private fun LocalChanges.isEmpty(): Boolean {
        return perangkat.isEmpty() && ruangan.isEmpty() && lampu.isEmpty() &&
                ruanganPerangkat.isEmpty() && deletedIds.isEmpty()
    }

    // And this one for the DeletedIds data class
    private fun DeletedIds.isEmpty(): Boolean {
        return perangkatIds.isEmpty() && ruanganIds.isEmpty() && lampuIds.isEmpty()
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
                ruanganPerangkat = ruanganPerangkatCrossRefDao().getChanges(since),
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