package com.anirudh.assignment.kredilyassignment.domain

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.anirudh.assignment.kredilyassignment.data.LocationDao
import com.anirudh.assignment.kredilyassignment.data.LocationEntity

class LocationRepository(
    private val dao: LocationDao,
    private val workManager: WorkManager
) {

    suspend fun saveLocation(entity: LocationEntity) {
        // ✅ ALWAYS save locally first
        dao.insert(entity)
        enqueueSyncWorker()
    }

    private fun enqueueSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request =
            OneTimeWorkRequestBuilder<LocationSyncWorker>()
                .setConstraints(constraints)
                .addTag("location_sync")
                .build()

        workManager.enqueueUniqueWork(
            "location_sync",
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
