package com.anirudh.assignment.kredilyassignment.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anirudh.assignment.kredilyassignment.data.AppDatabase
import com.anirudh.assignment.kredilyassignment.data.LocationEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class LocationSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val dao = AppDatabase.getInstance().locationDao()

    private val firestore = Firebase.firestore

    override suspend fun doWork(): Result {
        return try {
            val pending = dao.getPendingLocations()
            Log.d("LocationSyncWorker", "Pending = ${pending.size}")
            for (entity in pending) {
                uploadToFirestore(entity)
                dao.markAsSynced(entity.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun uploadToFirestore(entity: LocationEntity) {
        Log.d("LocationSyncWorker", "Uploading id=${entity.id}")
        val data = hashMapOf(
            "employeeId" to entity.employeeId,
            "latitude" to entity.latitude,
            "longitude" to entity.longitude,
            "accuracy" to entity.accuracy,
            "speed" to entity.speed,
            "timestamp" to entity.timeStamp
        )

        firestore
            .collection("location_logs")
            .add(data)
            .await()
    }
}
