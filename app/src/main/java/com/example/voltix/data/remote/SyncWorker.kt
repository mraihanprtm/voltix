package com.example.voltix.data.remote // Or your actual package for SyncWorker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
// import com.example.voltix.data.database.AppDatabase // Assuming SyncManager needs this (already in constructor)
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// Assuming SyncManager is in this package or imported correctly.
// import com.example.voltix.data.remote.SyncManager
// import com.example.voltix.data.remote.ApiService // Assuming SyncManager needs this

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager // Inject SyncManager instance
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        // Define a unique name for the work to prevent duplicates if desired
        const val UNIQUE_WORK_NAME = "com.example.voltix.data.remote.SyncWorker.PeriodicSync"

        /**
         * Enqueues the synchronization work using WorkManager.
         * This method provides a static-like way to schedule the SyncWorker.
         *
         * @param context The application context.
         */
        fun enqueueWork(context: Context) {
            val workManager = WorkManager.getInstance(context.applicationContext)

            // Define constraints for the work, e.g., network connectivity
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Create a one-time work request for SyncWorker
            // For periodic sync, you would use PeriodicWorkRequestBuilder
            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                // You can add a tag for querying or cancelling the work
                .addTag(TAG)
                // For important work that needs to run as soon as possible,
                // consider setExpedited(). Requires foreground service permissions on newer Android.
                // .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            // Enqueue the work as unique work to avoid multiple instances running
            // or being scheduled if one is already pending or running.
            // ExistingWorkPolicy.KEEP: If there's an existing pending (uncompleted) work with the
            // same unique name, do nothing. Otherwise, enqueue the new work.
            // Other policies include REPLACE, APPEND, APPEND_OR_REPLACE.
            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP, // Or use REPLACE if you want a new sync to supersede an old one
                syncWorkRequest
            )

            Log.d(TAG, "Sync work enqueued with name: $UNIQUE_WORK_NAME")
        }

        /**
         * Cancels all work tagged with SyncWorker.TAG.
         * @param context The application context.
         */
        fun cancelWork(context: Context) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.cancelAllWorkByTag(TAG)
            Log.d(TAG, "Attempted to cancel all work with tag: $TAG")
        }

        /**
         * Cancels unique work by its name.
         * @param context The application context.
         */
        fun cancelUniqueWork(context: Context) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Attempted to cancel unique work with name: $UNIQUE_WORK_NAME")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Sync process started for $id.") // id is available in CoroutineWorker
        return try {
            // Call the synchronize method on the injected instance of SyncManager
            syncManager.synchronize()
            Log.d(TAG, "doWork: Sync process completed successfully for $id.")
            Result.success()
        } catch (e: Exception) {
            // It's good practice to log the specific error for debugging
            Log.e(TAG, "doWork: Sync process failed for $id.", e)
            // Depending on the exception, you might want to retry
            // For example, if it's a network error: Result.retry()
            // For other errors that shouldn't be retried: Result.failure()
            Result.failure() // Or Result.retry() if appropriate
        }
    }
}