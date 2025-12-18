package com.anirudh.assignment.kredilyassignment.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Query("""
        SELECT * FROM location_logs
        WHERE synced = 0
        ORDER BY timeStamp ASC
    """)
    suspend fun getPendingLocations(): List<LocationEntity>

    @Query("UPDATE location_logs SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)
}
