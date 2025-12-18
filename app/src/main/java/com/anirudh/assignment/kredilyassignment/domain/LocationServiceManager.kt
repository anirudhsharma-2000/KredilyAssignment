package com.anirudh.assignment.kredilyassignment.domain

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import com.anirudh.assignment.kredilyassignment.appContext
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

object LocationSettingsManager {

    fun isLocationEnabled(): Boolean {
        val manager =
            appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestEnableLocation(
        activity: Activity,
        onEnabled: (Boolean) -> Unit
    ) {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        ).build()

        val settingsRequest =
            LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .setAlwaysShow(true)
                .build()

        LocationServices.getSettingsClient(activity)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                onEnabled(true)
            }
            .addOnFailureListener { e ->
                onEnabled(false)
                if (e is ResolvableApiException) {
                    e.startResolutionForResult(
                        activity,
                        LOCATION_ENABLE_REQUEST
                    )
                }
            }
    }

    const val LOCATION_ENABLE_REQUEST = 1001
}