package com.anirudh.assignment.kredilyassignment.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "location_logs")
@Serializable
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @SerialName("employeeId")
    val employeeId: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("accuracy")
    val accuracy: Float,
    @SerialName("timestamp")
    val timeStamp: Long,
    @SerialName("speed")
    val speed: Float,
    val synced: Boolean = false
)
